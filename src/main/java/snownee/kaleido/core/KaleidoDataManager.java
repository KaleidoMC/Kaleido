package snownee.kaleido.core;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.Collectors;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.criterion.ImpossibleTrigger;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import snownee.kaleido.Kaleido;
import snownee.kaleido.core.behavior.Behavior;
import snownee.kaleido.core.network.SUnlockModelsPacket;
import snownee.kiwi.Kiwi;
import snownee.kiwi.util.Util;

public class KaleidoDataManager extends JsonReloadListener {

    /* off */
    private static final Gson GSON = new GsonBuilder()
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

    public final Map<String, ModelPack> allPacks = Maps.newLinkedHashMap();
    public final Multimap<PlayerEntity, ResourceLocation> deferredIds = Multimaps.synchronizedListMultimap(ArrayListMultimap.create());

    public KaleidoDataManager() {
        super(GSON, "kaleido");
        MinecraftForge.EVENT_BUS.addListener(this::onAdvancement);
        MinecraftForge.EVENT_BUS.addListener(this::serverInit);
        MinecraftForge.EVENT_BUS.addListener(this::tick);
    }

    public void add(ModelInfo info) {
        allInfos.put(info.id, info);
        ModelPack pack = allPacks.computeIfAbsent(info.id.getNamespace(), $ -> new ModelPack());
        pack.add(info);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonObject> objectIn, IResourceManager resourceManagerIn, IProfiler profilerIn) {
        boolean firstTime = allInfos.isEmpty();
        allInfos.clear();
        allPacks.clear();
        for (Entry<ResourceLocation, JsonObject> entry : objectIn.entrySet()) {
            try {
                ModelInfo info = GSON.fromJson(entry.getValue(), ModelInfo.class);
                if (info != null) {
                    info.id = entry.getKey();
                    add(info);
                }
            } catch (JsonSyntaxException | NullPointerException e) {
                Kaleido.logger.catching(e);
            }
        }
        MinecraftServer server = Kiwi.getServer();
        makeAdvancements(server.getAdvancementManager().advancementList);
        if (!firstTime && server.getServerOwner() != null) {
            ServerPlayerEntity owner = server.getPlayerList().getPlayerByUsername(server.getServerOwner());
            if (server.isServerOwner(owner.getGameProfile())) {
                syncAllLockInfo(owner);
            }
        }
    }

    public ModelInfo get(ResourceLocation id) {
        return allInfos.get(id);
    }

    public void makeAdvancement(Map<ResourceLocation, Builder> map, Advancement parent, ModelInfo info) {
        Advancement.Builder builder = Advancement.Builder.builder();
        builder.withParent(parent);
        builder.withCriterion("_", new ImpossibleTrigger.Instance());
        map.put(info.getAdvancementId(), builder);
    }

    public void makeAdvancements(AdvancementList advancements) {
        Map<ResourceLocation, Builder> map = Maps.newLinkedHashMap();
        Advancement parent = advancements.getAdvancement(new ResourceLocation(Kaleido.MODID, "root"));
        allInfos.values().forEach(info -> makeAdvancement(map, parent, info));
        advancements.loadAdvancements(map);
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
            ModelPack pack = allPacks.get(realId.getNamespace());
            if (pack != null && pack.normalInfos.stream().allMatch($ -> $.id.equals(realId) || !$.isLockedServer(player))) {
                pack.rewardInfos.forEach($ -> $.grant(player));
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void read(Collection<ModelInfo> infos) {
        allInfos.clear();
        allPacks.clear();
        infos.forEach(this::add);
    }

    private void serverInit(FMLServerAboutToStartEvent event) {
        allInfos.clear();
        allPacks.clear();
        event.getServer().getResourceManager().addReloadListener(this);
    }

    public void syncAllLockInfo(ServerPlayerEntity player) {
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

    public void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == Phase.START || deferredIds.isEmpty()) {
            return;
        }
        for (Entry<PlayerEntity, Collection<ResourceLocation>> entry : deferredIds.asMap().entrySet()) {
            new SUnlockModelsPacket(entry.getValue(), true).send((ServerPlayerEntity) entry.getKey());
        }
        deferredIds.clear();
    }

    public ModelInfo getRandomUnlocked(ServerPlayerEntity player, Random rand) {
        List<ModelInfo> list = allInfos.values().stream().filter(ModelInfo::isLocked).filter($ -> !$.reward).collect(Collectors.toList());
        return list.isEmpty() ? null : list.get(rand.nextInt(list.size()));
    }

}
