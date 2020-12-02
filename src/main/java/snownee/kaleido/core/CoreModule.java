package snownee.kaleido.core;

import java.util.Collections;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import snownee.kaleido.Kaleido;
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
import snownee.kiwi.KiwiModule;
import snownee.kiwi.KiwiModule.Subscriber.Bus;
import snownee.kiwi.Name;
import snownee.kiwi.NoItem;
import snownee.kiwi.network.NetworkChannel;

@KiwiModule
@KiwiModule.Group("decorations")
@KiwiModule.Subscriber(Bus.MOD)
public class CoreModule extends AbstractModule {

    @NoItem
    public static final MasterBlock STUFF = new MasterBlock(blockProp(Blocks.STONE).notSolid());

    public static final LuckyBoxItem LUCKY_BOX = new LuckyBoxItem(itemProp());

    public static final TileEntityType<MasterTile> MASTER = new TileEntityType<MasterTile>(MasterTile::new, Collections.singleton(STUFF), null);

    public static final EntityType<?> SEAT = EntityType.Builder.create(EntityClassification.MISC).setCustomClientFactory((spawnEntity, world) -> new SeatEntity(world)).size(0.0001F, 0.0001F).setTrackingRange(16).setUpdateInterval(20).build("kaleido.seat");

    @Name("stuff")
    public static final StuffItem STUFF_ITEM = new StuffItem(STUFF, itemProp());

    public static final INamedTag<Item> CLOTH_TAG = itemTag(Kaleido.MODID, "cloth");

    public CoreModule() {
        KaleidoDataManager.INSTANCE.hashCode();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void clientInit(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(SEAT, EmptyEntityRenderer::new);
    }

    @Override
    protected void init(FMLCommonSetupEvent event) {
        Behavior.Deserializer.registerFactory("seat", SeatBehavior::create);
        Behavior.Deserializer.registerFactory("item_storage", ItemStorageBehavior::create);
        Behavior.Deserializer.registerFactory("light", LightBehavior::create);
    }

    //    @SubscribeEvent
    //    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
    //        MinecraftServer server = Kiwi.getServer();
    //        if (!event.getEntity().world.isRemote && server != null && !KaleidoDataManager.INSTANCE.allInfos.isEmpty()) {
    //            ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
    //            if (server.isServerOwner(player.getGameProfile())) {
    //                KaleidoDataManager.INSTANCE.syncAllLockInfo(player);
    //            } else {
    //                new SSyncModelsPacket(KaleidoDataManager.INSTANCE.allInfos.values()).setPlayer(player).send();
    //            }
    //        }
    //    }

    @Override
    protected void preInit() {
        NetworkChannel.register(SSyncModelsPacket.class, new SSyncModelsPacket.Handler());
        NetworkChannel.register(SUnlockModelsPacket.class, new SUnlockModelsPacket.Handler());
        NetworkChannel.register(CRedeemPacket.class, new CRedeemPacket.Handler());
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void registerModelLoader(ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(RL("dynamic"), KaleidoModel.Loader.INSTANCE);
    }
}
