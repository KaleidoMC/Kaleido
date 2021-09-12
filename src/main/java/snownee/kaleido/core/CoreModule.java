package snownee.kaleido.core;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import snownee.kaleido.Kaleido;
import snownee.kaleido.core.action.ActionDeserializer;
import snownee.kaleido.core.action.TransformAction;
import snownee.kaleido.core.behavior.Behavior;
import snownee.kaleido.core.behavior.ItemStorageBehavior;
import snownee.kaleido.core.behavior.LightBehavior;
import snownee.kaleido.core.behavior.OnUseBlockBehavior;
import snownee.kaleido.core.behavior.SeatBehavior;
import snownee.kaleido.core.behavior.seat.EmptyEntityRenderer;
import snownee.kaleido.core.behavior.seat.SeatEntity;
import snownee.kaleido.core.block.KDirectionalBlock;
import snownee.kaleido.core.block.KHorizontalBlock;
import snownee.kaleido.core.block.KRotatedPillarBlock;
import snownee.kaleido.core.block.entity.MasterBlockEntity;
import snownee.kaleido.core.client.model.KaleidoModel;
import snownee.kaleido.core.item.LuckyBoxItem;
import snownee.kaleido.core.item.StuffItem;
import snownee.kaleido.core.network.CRedeemPacket;
import snownee.kaleido.core.network.SSyncModelsPacket;
import snownee.kaleido.core.network.SSyncShapesPacket;
import snownee.kaleido.core.network.SUnlockModelsPacket;
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
	public static final KHorizontalBlock HORIZONTAL = new KHorizontalBlock(blockProp(Material.STONE).strength(0.5F));

	@NoItem
	public static final KHorizontalBlock STUFF = new KHorizontalBlock(blockProp(HORIZONTAL).noOcclusion().dynamicShape());

	@NoItem
	public static final KDirectionalBlock DIRECTIONAL = new KDirectionalBlock(blockProp(HORIZONTAL));

	@NoItem
	public static final KRotatedPillarBlock PILLAR = new KRotatedPillarBlock(blockProp(HORIZONTAL));

	public static final LuckyBoxItem LUCKY_BOX = new LuckyBoxItem(itemProp());

	public static final Set<Block> ALL_MASTER_BLOCKS = Sets.newHashSet();
	public static final TileEntityType<MasterBlockEntity> MASTER = new TileEntityType<>(MasterBlockEntity::new, ALL_MASTER_BLOCKS, null);

	public static final EntityType<?> SEAT = EntityType.Builder.createNothing(EntityClassification.MISC).setCustomClientFactory((spawnEntity, world) -> new SeatEntity(world)).sized(0.0001F, 0.0001F).setTrackingRange(16).setUpdateInterval(20).build("kaleido.seat");

	@Name("stuff")
	public static final StuffItem STUFF_ITEM = new StuffItem(STUFF, itemProp());

	public static final INamedTag<Item> CLOTH_TAG = itemTag(Kaleido.MODID, "cloth");

	public static final LootFunctionType LOOT_FUNCTION_TYPE = Registry.register(Registry.LOOT_FUNCTION_TYPE, new ResourceLocation(Kaleido.MODID, "data"), new LootFunctionType(new KaleidoLootFunction.Serializer()));

	public CoreModule() {
		KaleidoDataManager.INSTANCE.hashCode();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected void clientInit(FMLClientSetupEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(SEAT, EmptyEntityRenderer::new);

		Set<RenderType> set = ImmutableSet.of(RenderType.solid(), RenderType.cutout(), RenderType.cutoutMipped(), RenderType.translucent());
		RenderTypeLookup.setRenderLayer(STUFF, set::contains);
	}

	@Override
	protected void init(FMLCommonSetupEvent event) {
		Behavior.Deserializer.registerFactory("seat", SeatBehavior::create);
		Behavior.Deserializer.registerFactory("itemStorage", ItemStorageBehavior::create);
		Behavior.Deserializer.registerFactory("light", LightBehavior::create);
		Behavior.Deserializer.registerFactory("onUseBlock", OnUseBlockBehavior::create);

		ActionDeserializer.registerFactory("transform", TransformAction::create);
	}

	@Override
	protected void preInit() {
		NetworkChannel.register(SSyncModelsPacket.class, new SSyncModelsPacket.Handler());
		NetworkChannel.register(SUnlockModelsPacket.class, new SUnlockModelsPacket.Handler());
		NetworkChannel.register(SSyncShapesPacket.class, new SSyncShapesPacket.Handler(KaleidoDataManager.INSTANCE.shapeCache));

		NetworkChannel.register(CRedeemPacket.class, new CRedeemPacket.Handler());
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void registerModelLoader(ModelRegistryEvent event) {
		ModelLoaderRegistry.registerLoader(RL("dynamic"), KaleidoModel.Loader.INSTANCE);
	}

}
