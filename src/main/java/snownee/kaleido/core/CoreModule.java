package snownee.kaleido.core;

import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.data.DataGenerator;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import snownee.kaleido.Kaleido;
import snownee.kaleido.chisel.network.CChiselPickPacket;
import snownee.kaleido.core.action.ActionDeserializer;
import snownee.kaleido.core.action.CommandAction;
import snownee.kaleido.core.action.TransformAction;
import snownee.kaleido.core.behavior.Behavior;
import snownee.kaleido.core.behavior.ItemStorageBehavior;
import snownee.kaleido.core.behavior.LightBehavior;
import snownee.kaleido.core.behavior.OnAttackBlockBehavior;
import snownee.kaleido.core.behavior.OnProjectileHitBehavior;
import snownee.kaleido.core.behavior.OnUseBlockBehavior;
import snownee.kaleido.core.behavior.SeatBehavior;
import snownee.kaleido.core.behavior.seat.EmptyEntityRenderer;
import snownee.kaleido.core.behavior.seat.SeatEntity;
import snownee.kaleido.core.block.KDirectionalBlock;
import snownee.kaleido.core.block.KHorizontalBlock;
import snownee.kaleido.core.block.KRotatedPillarBlock;
import snownee.kaleido.core.block.KaleidoBlocks;
import snownee.kaleido.core.block.entity.MasterBlockEntity;
import snownee.kaleido.core.client.KaleidoClient;
import snownee.kaleido.core.client.model.KaleidoModel;
import snownee.kaleido.core.definition.BlockDefinition;
import snownee.kaleido.core.definition.DynamicBlockDefinition;
import snownee.kaleido.core.definition.KaleidoBlockDefinition;
import snownee.kaleido.core.definition.SimpleBlockDefinition;
import snownee.kaleido.core.item.LuckyBoxItem;
import snownee.kaleido.core.item.StuffItem;
import snownee.kaleido.core.network.CRedeemPacket;
import snownee.kaleido.core.network.SSyncModelsPacket;
import snownee.kaleido.core.network.SSyncShapesPacket;
import snownee.kaleido.core.network.SUnlockModelsPacket;
import snownee.kaleido.core.util.KaleidoTemplate;
import snownee.kaleido.data.KaleidoBlockLoot;
import snownee.kaleido.scope.network.CConfigureScopePacket;
import snownee.kaleido.scope.network.CCreateScopePacket;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.KiwiModule.Subscriber.Bus;
import snownee.kiwi.Name;
import snownee.kiwi.NoItem;
import snownee.kiwi.data.provider.KiwiLootTableProvider;
import snownee.kiwi.item.ModBlockItem;
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

	public static final Set<Block> MASTER_BLOCKS = Sets.newHashSet();
	public static final TileEntityType<MasterBlockEntity> MASTER = new TileEntityType<>(MasterBlockEntity::new, MASTER_BLOCKS, null);

	public static final EntityType<?> SEAT = EntityType.Builder.createNothing(EntityClassification.MISC).setCustomClientFactory((spawnEntity, world) -> new SeatEntity(world)).sized(0.0001F, 0.0001F).setTrackingRange(16).setUpdateInterval(20).build("kaleido.seat");

	@Name("stuff")
	public static final StuffItem STUFF_ITEM = new StuffItem(STUFF, itemProp());

	public static final INamedTag<Item> CLOTH_TAG = itemTag(Kaleido.MODID, "cloth");

	public static final LootFunctionType LOOT_FUNCTION_TYPE = Registry.register(Registry.LOOT_FUNCTION_TYPE, new ResourceLocation(Kaleido.MODID, "data"), new LootFunctionType(new KaleidoLootFunction.Serializer()));

	public CoreModule() {
		KaleidoDataManager.INSTANCE.hashCode();
		KaleidoTemplate.none.hashCode();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected void clientInit(FMLClientSetupEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(SEAT, EmptyEntityRenderer::new);

		RenderTypeLookup.setRenderLayer(STUFF, KaleidoClient.blockRenderTypes::contains);

		KaleidoClient.init();
		ModBlockItem.INSTANT_UPDATE_TILES.add(MASTER);
	}

	@Override
	protected void init(FMLCommonSetupEvent event) {
		Behavior.Deserializer.registerFactory("seat", SeatBehavior::create);
		Behavior.Deserializer.registerFactory("itemStorage", ItemStorageBehavior::create);
		Behavior.Deserializer.registerFactory("light", LightBehavior::create);
		Behavior.Deserializer.registerFactory("onUseBlock", OnUseBlockBehavior::create);
		Behavior.Deserializer.registerFactory("onAttackBlock", OnAttackBlockBehavior::create);
		Behavior.Deserializer.registerFactory("onProjectileHit", OnProjectileHitBehavior::create);

		ActionDeserializer.registerFactory("transform", TransformAction::create);
		ActionDeserializer.registerFactory("command", CommandAction::create);

		BlockDefinition.registerFactory(SimpleBlockDefinition.Factory.INSTANCE);
		BlockDefinition.registerFactory(DynamicBlockDefinition.Factory.INSTANCE);
		BlockDefinition.registerFactory(KaleidoBlockDefinition.Factory.INSTANCE);
	}

	@Override
	protected void preInit() {
		NetworkChannel.register(SSyncModelsPacket.class, new SSyncModelsPacket.Handler());
		NetworkChannel.register(SUnlockModelsPacket.class, new SUnlockModelsPacket.Handler());
		NetworkChannel.register(SSyncShapesPacket.class, new SSyncShapesPacket.Handler(KaleidoDataManager.INSTANCE.shapeCache));

		NetworkChannel.register(CRedeemPacket.class, new CRedeemPacket.Handler());
		NetworkChannel.register(CChiselPickPacket.class, new CChiselPickPacket.Handler());
		NetworkChannel.register(CCreateScopePacket.class, new CCreateScopePacket.Handler());
		NetworkChannel.register(CConfigureScopePacket.class, new CConfigureScopePacket.Handler());
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void registerModelLoader(ModelRegistryEvent event) {
		ModelLoaderRegistry.registerLoader(RL("dynamic"), KaleidoModel.Loader.INSTANCE);
	}

	@SubscribeEvent
	public void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		if (event.includeServer()) {
			generator.addProvider(new KiwiLootTableProvider(generator).add(KaleidoBlockLoot::new, LootParameterSets.BLOCK));
		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void blockColors(ColorHandlerEvent.Block event) {
		BlockColors blockColors = event.getBlockColors();
		blockColors.register((state, level, pos, i) -> {
			if (level != null && pos != null) {
				ModelInfo info = KaleidoBlocks.getInfo(level, pos);
				if (info != null) {
					return info.getBlockColor(state, level, pos, i);
				}
			}
			return -1;
		}, MASTER_BLOCKS.toArray(new Block[0]));
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void itemColors(ColorHandlerEvent.Item event) {
		ItemColors itemColors = event.getItemColors();
		itemColors.register((stack, i) -> {
			ModelInfo info = KaleidoBlocks.getInfo(stack);
			if (info != null) {
				return info.getItemColor(stack, i);
			}
			return -1;
		}, STUFF_ITEM);
	}

}
