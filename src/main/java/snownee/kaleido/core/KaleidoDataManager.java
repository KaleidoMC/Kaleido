package snownee.kaleido.core;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.netty.buffer.Unpooled;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.advancements.criterion.ImpossibleTrigger;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.SimpleReloadableResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import snownee.kaleido.Hooks;
import snownee.kaleido.Kaleido;
import snownee.kaleido.KaleidoCommonConfig;
import snownee.kaleido.carpentry.network.SUnlockModelsPacket;
import snownee.kaleido.compat.worldedit.WorldEditModule;
import snownee.kaleido.core.behavior.Behavior;
import snownee.kaleido.core.network.SSyncBehaviorsPacket;
import snownee.kaleido.core.network.SSyncModelsPacket;
import snownee.kaleido.core.network.SSyncShapesPacket;
import snownee.kaleido.util.data.RotatedShapeCache;
import snownee.kaleido.util.data.ShapeSerializer;
import snownee.kiwi.util.Util;

public class KaleidoDataManager extends JsonReloadListener {

	/* off */
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .enableComplexMapKeySerialization()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .create();
    /* on */

	public static final KaleidoDataManager INSTANCE = new KaleidoDataManager();
	public final Map<ResourceLocation, ModelInfo> allInfos = Maps.newLinkedHashMap();
	public final Map<ResourceLocation, ModelGroup> allGroups = Maps.newHashMap();

	public final Map<String, ModelPack> allPacks = Maps.newLinkedHashMap();
	public final Multimap<PlayerEntity, ResourceLocation> deferredIds = Multimaps.synchronizedListMultimap(ArrayListMultimap.create());
	public final ShapeSerializer shapeSerializer;
	public final RotatedShapeCache shapeCache;
	private boolean skip;
	private int clientBehaviorModelsCount;
	public boolean isClientSide = true;

	private KaleidoDataManager() {
		super(GSON, "kaleido");
		MinecraftForge.EVENT_BUS.addListener(this::onAdvancement);
		MinecraftForge.EVENT_BUS.addListener(this::tick);
		MinecraftForge.EVENT_BUS.addListener(this::serverInit);
		MinecraftForge.EVENT_BUS.addListener(this::onSyncDatapack);
		shapeCache = new RotatedShapeCache(Hashing.md5());
		shapeSerializer = new ShapeSerializer(shapeCache);
	}

	public void add(ModelInfo info) {
		allInfos.put(info.id, info);
		ModelPack pack = getPack(info.id.getNamespace());
		pack.add(info);
		if (!isClientSide && !info.behaviors.isEmpty()) {
			for (Behavior behavior : info.behaviors.values()) {
				if (behavior.syncClient()) {
					++clientBehaviorModelsCount;
					break;
				}
			}
		}
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objectIn, IResourceManager resourceManagerIn, IProfiler profilerIn) {
		if (skip) {
			Kaleido.logger.info("Skip loading Kaleido data");
			skip = false;
			return;
		}
		isClientSide = false;
		Stopwatch stopWatch = Stopwatch.createStarted();
		invalidate();
		for (Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet()) {
			if (KaleidoCommonConfig.ignoredNamespaces.contains(entry.getKey().getNamespace())) {
				continue;
			}
			if ("_pack".equals(entry.getKey().getPath())) {
				throw new NotImplementedException("Reserved word");
			}
			try {
				JsonObject json = GSON.fromJson(entry.getValue(), JsonObject.class);
				ModelInfo info = ModelInfo.fromJson(json);
				if (info != null) {
					info.id = entry.getKey();
					add(info);
				}
			} catch (Exception e) {
				Kaleido.logger.catching(e);
			}
		}
		sort();
		if (!KaleidoCommonConfig.disableAdvancements) {
			if (resourceManagerIn instanceof SimpleReloadableResourceManager) {
				for (IFutureReloadListener listener : ((SimpleReloadableResourceManager) resourceManagerIn).listeners) {
					if (listener instanceof AdvancementManager) {
						makeAdvancements(((AdvancementManager) listener).advancements);
						break;
					}
				}
			}
		}
		Kaleido.logger.info("Loading {} Kaleido instances took {}", allInfos.size(), stopWatch.stop());
		//        if (!firstTime && server.getServerOwner() != null) {
		//            ServerPlayerEntity owner = server.getPlayerList().getPlayerByUsername(server.getServerOwner());
		//            if (server.isServerOwner(owner.getGameProfile())) {
		//                syncAllLockInfo(owner);
		//            }
		//        }

		if (FMLEnvironment.dist.isClient() && !FMLEnvironment.production) {
			SSyncModelsPacket packet = new SSyncModelsPacket(allInfos.values());
			SSyncModelsPacket.Handler handler = new SSyncModelsPacket.Handler();
			PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
			handler.encode(packet, buf);
			int size = buf.writerIndex();
			Kaleido.logger.info("Packet size: " + size);
			packet = handler.decode(buf);
			Preconditions.checkArgument(packet.infos.size() == allInfos.size());
		}

		if (ModList.get().isLoaded("worldedit"))
			WorldEditModule.generateMappings(ServerLifecycleHooks.getCurrentServer());
	}

	private void sort() {
		allPacks.values().parallelStream().forEach(ModelPack::sort);
		allGroups.values().parallelStream().forEach($ -> Collections.sort($.infos));
	}

	public static ModelInfo get(ResourceLocation id) {
		return INSTANCE.allInfos.get(id);
	}

	public static ModelPack getPack(String namespace) {
		return INSTANCE.allPacks.computeIfAbsent(namespace, ModelPack::new);
	}

	public static ModelGroup getGroup(ResourceLocation id) {
		return INSTANCE.allGroups.computeIfAbsent(id, ModelGroup::new);
	}

	public void makeAdvancement(Map<ResourceLocation, Builder> map, Advancement parent, ModelInfo info) {
		Advancement.Builder builder = Advancement.Builder.advancement();
		builder.parent(parent);
		builder.addCriterion("_", new ImpossibleTrigger.Instance());
		map.put(info.getAdvancementId(), builder);
	}

	public void makeAdvancements(AdvancementList advancements) {
		Map<ResourceLocation, Builder> map = Maps.newLinkedHashMap();
		Advancement parent = advancements.get(new ResourceLocation(Kaleido.MODID, "root"));
		allInfos.values().forEach(info -> makeAdvancement(map, parent, info));
		advancements.add(map);
	}

	public void onAdvancement(AdvancementEvent event) {
		if (!Hooks.carpentryEnabled)
			return;
		ResourceLocation id = event.getAdvancement().getId();
		if (Kaleido.MODID.equals(id.getNamespace())) {
			ResourceLocation realId = Util.RL(id.getPath().replace('/', ':'));
			if (realId == null) {
				return;
			}
			ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
			deferredIds.put(player, realId);
			ModelPack pack = getPack(realId.getNamespace());
			if (pack != null && pack.normalInfos.stream().allMatch($ -> $.id.equals(realId) || !$.isLockedServer(player))) {
				pack.rewardInfos.forEach($ -> $.grant(player));
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void read(Collection<ModelInfo> infos) {
		invalidate();
		infos.forEach(this::add);
		sort();
	}

	private void serverInit(AddReloadListenerEvent event) {
		event.addListener(this);
	}

	private void invalidate() {
		isClientSide = true;
		clientBehaviorModelsCount = 0;
		shapeCache.clear();
		ModelInfo.cache.invalidateAll();
		allInfos.values().forEach($ -> $.expired = true);
		allInfos.clear();
		allGroups.clear();
		allPacks.clear();
	}

	public void syncAllLockInfo(ServerPlayerEntity player) {
		if (KaleidoCommonConfig.autoUnlock())
			return;
		/* off */
        List<ResourceLocation> list = allInfos.values().stream()
                .filter($ -> $.isAdvancementDone(player))
                .map($ -> $.id)
                .collect(Collectors.toList());
        /* on */
		if (!list.isEmpty()) {
			new SUnlockModelsPacket(list, false).send(player);
		}
	}

	private void tick(TickEvent.ServerTickEvent event) {
		if (isClientSide || event.phase == Phase.START || deferredIds.isEmpty()) {
			return;
		}
		for (Entry<PlayerEntity, Collection<ResourceLocation>> entry : deferredIds.asMap().entrySet()) {
			new SUnlockModelsPacket(entry.getValue(), true).send((ServerPlayerEntity) entry.getKey());
		}
		deferredIds.clear();
	}

	private void onSyncDatapack(OnDatapackSyncEvent event) {
		MinecraftServer server = event.getPlayerList().getServer();
		ServerPlayerEntity player = event.getPlayer();
		if (player != null) {
			sync(player, server);
		} else {
			for (ServerPlayerEntity p : event.getPlayerList().getPlayers()) {
				sync(p, server);
			}
		}
	}

	private static void sync(ServerPlayerEntity player, MinecraftServer server) {
		if (!player.level.isClientSide && server != null && !INSTANCE.allInfos.isEmpty()) {
			if (server.isSingleplayerOwner(player.getGameProfile())) {
				INSTANCE.syncAllLockInfo(player);
			} else {
				new SSyncShapesPacket().send(player);
				new SSyncModelsPacket(INSTANCE.allInfos.values()).setPlayer(player).send();
				new SSyncBehaviorsPacket(INSTANCE.clientBehaviorModelsCount, INSTANCE.allInfos.values()).setPlayer(player).send();
			}
		}
	}

	public void skipOnce() {
		skip = true;
	}

}
