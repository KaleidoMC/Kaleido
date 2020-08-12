package snownee.kaleido.core;

import java.util.Collections;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.Tag;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import snownee.kaleido.Kaleido;
import snownee.kaleido.KaleidoClientConfig;
import snownee.kaleido.KaleidoCommonConfig;
import snownee.kaleido.core.behavior.Behavior;
import snownee.kaleido.core.behavior.ItemStorageBehavior;
import snownee.kaleido.core.behavior.LightBehavior;
import snownee.kaleido.core.behavior.SeatBehavior;
import snownee.kaleido.core.behavior.seat.EmptyEntityRenderer;
import snownee.kaleido.core.behavior.seat.SeatEntity;
import snownee.kaleido.core.block.MasterBlock;
import snownee.kaleido.core.client.model.KaleidoModel;
import snownee.kaleido.core.item.LuckyBoxItem;
import snownee.kaleido.core.item.StuffItem;
import snownee.kaleido.core.network.CRedeemPacket;
import snownee.kaleido.core.network.SSyncModelsPacket;
import snownee.kaleido.core.network.SUnlockModelsPacket;
import snownee.kaleido.core.tile.MasterTile;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.Name;
import snownee.kiwi.NoItem;
import snownee.kiwi.network.NetworkChannel;

@KiwiModule
@KiwiModule.Group("decorations")
@KiwiModule.Subscriber
public class CoreModule extends AbstractModule {

    @NoItem
    public static final MasterBlock STUFF = init(new MasterBlock(blockProp(Blocks.STONE).notSolid()));

    public static final LuckyBoxItem LUCKY_BOX = new LuckyBoxItem(itemProp());

    public static final TileEntityType<MasterTile> MASTER = new TileEntityType<MasterTile>(MasterTile::new, Collections.singleton(STUFF), null);

    public static final EntityType<?> SEAT = EntityType.Builder.create(EntityClassification.MISC).setCustomClientFactory((spawnEntity, world) -> new SeatEntity(world)).size(0.0001F, 0.0001F).setTrackingRange(16).setUpdateInterval(20).build("kaleido.seat");

    @Name("stuff")
    public static final StuffItem STUFF_ITEM = new StuffItem(STUFF, itemProp());

    public static final Tag<Item> CLOTH_TAG = itemTag(Kaleido.MODID, "cloth");

    public CoreModule() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        //        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, KaleidoCommonConfig.spec);
        //        modEventBus.register(KaleidoCommonConfig.class);
        if (FMLEnvironment.dist.isClient()) {
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, KaleidoClientConfig.spec);
            modEventBus.register(KaleidoClientConfig.class);
        }
        //MinecraftForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
        modEventBus.addListener(this::addToDataListener);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void clientInit(FMLClientSetupEvent event) {
        KaleidoClientConfig.refresh();
        ModelLoaderRegistry.registerLoader(RL("dynamic"), KaleidoModel.Loader.INSTANCE);
        RenderingRegistry.registerEntityRenderingHandler(SEAT, EmptyEntityRenderer::new);
    }

    @Override
    protected void init(FMLCommonSetupEvent event) {
        KaleidoCommonConfig.refresh();

        Behavior.Deserializer.registerFactory("seat", SeatBehavior::create);
        Behavior.Deserializer.registerFactory("item_storage", ItemStorageBehavior::create);
        Behavior.Deserializer.registerFactory("light", LightBehavior::create);
    }

    protected void addToDataListener(FMLServerAboutToStartEvent event) {
        event.getServer().getResourceManager().addReloadListener(KaleidoDataManager.INSTANCE);
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        MinecraftServer server = Kiwi.getServer();
        if (!event.getEntity().world.isRemote && server != null && !KaleidoDataManager.INSTANCE.allInfos.isEmpty()) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
            if (server.isServerOwner(player.getGameProfile())) {
                KaleidoDataManager.INSTANCE.syncAllLockInfo(player);
            } else {
                new SSyncModelsPacket(KaleidoDataManager.INSTANCE.allInfos.values()).setPlayer(player).send();
            }
        }
    }

    @Override
    protected void preInit() {
        NetworkChannel.register(SSyncModelsPacket.class, new SSyncModelsPacket.Handler());
        NetworkChannel.register(SUnlockModelsPacket.class, new SUnlockModelsPacket.Handler());
        NetworkChannel.register(CRedeemPacket.class, new CRedeemPacket.Handler());
    }
}
