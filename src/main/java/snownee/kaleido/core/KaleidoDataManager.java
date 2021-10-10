package snownee.kaleido.core;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.Collectors;

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

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.advancements.criterion.ImpossibleTrigger;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
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
import snownee.kaleido.Kaleido;
import snownee.kaleido.KaleidoCommonConfig;
import snownee.kaleido.compat.worldedit.WorldEditModule;
import snownee.kaleido.core.behavior.Behavior;
import snownee.kaleido.core.network.SSyncModelsPacket;
import snownee.kaleido.core.network.SSyncShapesPacket;
import snownee.kaleido.core.network.SUnlockModelsPacket;
import snownee.kaleido.util.ShapeCache;
import snownee.kaleido.util.ShapeSerializer;
import snownee.kiwi.Kiwi;
import snownee.kiwi.util.Util;

public class KaleidoDataManager extends JsonReloadListener {

	/* off */
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .enableComplexMapKeySerialization()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .registerTypeAdapter(Behavior.class, Behavior.Deserializer.INSTANCE)
            .create();
    /* on */

	public static final KaleidoDataManager INSTANCE = new KaleidoDataManager();
	public final Map<ResourceLocation, ModelInfo> allInfos = Maps.newLinkedHashMap();
	public final Map<ResourceLocation, ModelGroup> allGroups = Maps.newHashMap();

	public final Map<String, ModelPack> allPacks = Maps.newLinkedHashMap();
	public final Multimap<PlayerEntity, ResourceLocation> deferredIds = Multimaps.synchronizedListMultimap(ArrayListMultimap.create());
	public final ShapeSerializer shapeSerializer;
	public final ShapeCache shapeCache;
	private boolean skip;

	private KaleidoDataManager() {
		super(GSON, "kaleido");
		MinecraftForge.EVENT_BUS.addListener(this::onAdvancement);
		MinecraftForge.EVENT_BUS.addListener(this::tick);
		MinecraftForge.EVENT_BUS.addListener(this::serverInit);
		MinecraftForge.EVENT_BUS.addListener(this::onSyncDatapack);
		shapeCache = new ShapeCache(Hashing.md5());
		shapeSerializer = new ShapeSerializer(shapeCache);
	}

	public void add(ModelInfo info) {
		allInfos.put(info.id, info);
		ModelPack pack = getPack(info.id.getNamespace());
		pack.add(info);
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objectIn, IResourceManager resourceManagerIn, IProfiler profilerIn) {
		if (skip) {
			Kaleido.logger.info("Skip loading Kaleido data");
			skip = false;
			return;
		}
		Stopwatch stopWatch = Stopwatch.createStarted();
		invalidate();
		for (Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet()) {
			if (KaleidoCommonConfig.ignoredNamespaces.contains(entry.getKey().getNamespace())) {
				continue;
			}
			if (entry.getKey().getPath().equals("pack")) {
				continue;
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
		allPacks.values().parallelStream().forEach(ModelPack::sort);
		allGroups.values().parallelStream().forEach($ -> Collections.sort($.infos));
		if (KaleidoCommonConfig.generateAdvancements()) {
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

		if (ModList.get().isLoaded("worldedit"))
			WorldEditModule.generateMappings(Kiwi.getServer());
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
		ResourceLocation id = event.getAdvancement().getId();
		if (id.getNamespace().equals(Kaleido.MODID) && !id.getPath().equals("root")) {
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
	}

	private void serverInit(AddReloadListenerEvent event) {
		event.addListener(this);
	}

	private void invalidate() {
		shapeCache.getMap().clear();
		ModelInfo.cache.invalidateAll();
		allInfos.values().forEach($ -> $.expired = true);
		allInfos.clear();
		allGroups.clear();
		allPacks.clear();
	}

	public void syncAllLockInfo(ServerPlayerEntity player) {
		if (KaleidoCommonConfig.autoUnlock)
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
		if (event.phase == Phase.START || deferredIds.isEmpty()) {
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
				KaleidoDataManager.INSTANCE.syncAllLockInfo(player);
			} else {
				new SSyncShapesPacket().send(player);
				new SSyncModelsPacket(KaleidoDataManager.INSTANCE.allInfos.values()).setPlayer(player).send();
			}
		}
	}

	public ModelInfo getRandomLocked(ServerPlayerEntity player, Random rand) {
		List<ModelInfo> list = allInfos.values().stream().filter($ -> !$.reward).filter($ -> $.isLockedServer(player)).collect(Collectors.toList());
		return list.isEmpty() ? null : list.get(rand.nextInt(list.size()));
	}

	public void skipOnce() {
		skip = true;
	}

}
