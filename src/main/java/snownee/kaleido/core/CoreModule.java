package snownee.kaleido.core;

import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import snownee.kaleido.Hooks;
import snownee.kaleido.Kaleido;
import snownee.kaleido.brush.network.CConfigureBrushPacket;
import snownee.kaleido.carpentry.network.CRedeemPacket;
import snownee.kaleido.carpentry.network.SUnlockModelsPacket;
import snownee.kaleido.chisel.network.CChiselPickPacket;
import snownee.kaleido.core.action.Action;
import snownee.kaleido.core.action.CommandAction;
import snownee.kaleido.core.action.SitAction;
import snownee.kaleido.core.action.TransformAction;
import snownee.kaleido.core.action.seat.EmptyEntityRenderer;
import snownee.kaleido.core.action.seat.SeatEntity;
import snownee.kaleido.core.behavior.Behavior;
import snownee.kaleido.core.behavior.EventBehavior;
import snownee.kaleido.core.behavior.FoodBehavior;
import snownee.kaleido.core.behavior.ItemStorageBehavior;
import snownee.kaleido.core.block.KDirectionalBlock;
import snownee.kaleido.core.block.KHorizontalBlock;
import snownee.kaleido.core.block.KLeavesBlock;
import snownee.kaleido.core.block.KPlantBlock;
import snownee.kaleido.core.block.KRotatedPillarBlock;
import snownee.kaleido.core.block.KStairsBlock;
import snownee.kaleido.core.block.KaleidoBlock;
import snownee.kaleido.core.block.entity.MasterBlockEntity;
import snownee.kaleido.core.client.KaleidoClient;
import snownee.kaleido.core.client.model.KaleidoModel;
import snownee.kaleido.core.data.KaleidoIngredient;
import snownee.kaleido.core.data.KaleidoLootFunction;
import snownee.kaleido.core.definition.BlockDefinition;
import snownee.kaleido.core.definition.DynamicBlockDefinition;
import snownee.kaleido.core.definition.KaleidoBlockDefinition;
import snownee.kaleido.core.definition.SimpleBlockDefinition;
import snownee.kaleido.core.item.StuffItem;
import snownee.kaleido.core.network.SSyncBehaviorsPacket;
import snownee.kaleido.core.network.SSyncModelsPacket;
import snownee.kaleido.core.network.SSyncShapesPacket;
import snownee.kaleido.core.template.KaleidoTemplate;
import snownee.kaleido.datagen.KaleidoBlockLoot;
import snownee.kaleido.scope.network.CConfigureScopePacket;
import snownee.kaleido.scope.network.CCreateScopePacket;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.KiwiModule.Subscriber.Bus;
import snownee.kiwi.Name;
import snownee.kiwi.NoItem;
import snownee.kiwi.datagen.provider.KiwiLootTableProvider;
import snownee.kiwi.item.ModBlockItem;
import snownee.kiwi.network.NetworkChannel;

@KiwiModule
@KiwiModule.Subscriber(Bus.MOD)
public class CoreModule extends AbstractModule {

	@NoItem
	public static final KHorizontalBlock HORIZONTAL = new KHorizontalBlock(blockProp(Material.WOOD).strength(0.5F));

	@NoItem
	public static final KHorizontalBlock STUFF = new KHorizontalBlock(blockProp(HORIZONTAL).noOcclusion().dynamicShape());

	@NoItem
	public static final KDirectionalBlock DIRECTIONAL = new KDirectionalBlock(blockProp(HORIZONTAL));

	@NoItem
	public static final KRotatedPillarBlock PILLAR = new KRotatedPillarBlock(blockProp(HORIZONTAL));

	@NoItem
	public static final KStairsBlock STAIRS = new KStairsBlock(blockProp(HORIZONTAL));

	@NoItem
	public static final KLeavesBlock LEAVES = new KLeavesBlock(blockProp(Material.LEAVES).strength(0.2F).sound(SoundType.GRASS).noOcclusion().dynamicShape().isValidSpawn(CoreModule::ocelotOrParrot).isSuffocating(CoreModule::never).isViewBlocking(CoreModule::never));

	@NoItem
	public static final KPlantBlock PLANT = new KPlantBlock(blockProp(Material.PLANT).strength(0.2F).sound(SoundType.GRASS).noOcclusion().dynamicShape());

	public static final Set<Block> MASTER_BLOCKS = Sets.newHashSet();
	public static final TileEntityType<MasterBlockEntity> MASTER = new TileEntityType<>(MasterBlockEntity::new, MASTER_BLOCKS, null);

	public static final EntityType<?> SEAT = EntityType.Builder.createNothing(EntityClassification.MISC).setCustomClientFactory((spawnEntity, world) -> new SeatEntity(world)).sized(0.0001F, 0.0001F).setTrackingRange(16).setUpdateInterval(20).build("kaleido.seat");

	@Name("stuff")
	public static final StuffItem STUFF_ITEM = new StuffItem(STUFF, itemProp());

	public static final INamedTag<Item> CLOTH_TAG = itemTag(Kaleido.MODID, "cloth");

	public static final LootFunctionType LOOT_FUNCTION_TYPE = Registry.register(Registry.LOOT_FUNCTION_TYPE, new ResourceLocation(Kaleido.MODID, "data"), new LootFunctionType(new KaleidoLootFunction.Serializer()));

	public CoreModule() {
		KaleidoDataManager.INSTANCE.hashCode();
		KaleidoTemplate.NONE.hashCode();
	}

	private static Boolean ocelotOrParrot(BlockState p_235441_0_, IBlockReader p_235441_1_, BlockPos p_235441_2_, EntityType<?> p_235441_3_) {
		return p_235441_3_ == EntityType.OCELOT || p_235441_3_ == EntityType.PARROT;
	}

	private static boolean never(BlockState p_235436_0_, IBlockReader p_235436_1_, BlockPos p_235436_2_) {
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected void clientInit(FMLClientSetupEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(SEAT, EmptyEntityRenderer::new);

		RenderTypeLookup.setRenderLayer(STUFF, KaleidoClient.blockRenderTypes::contains);
		RenderTypeLookup.setRenderLayer(LEAVES, KaleidoClient.blockRenderTypes::contains);
		RenderTypeLookup.setRenderLayer(PLANT, KaleidoClient.blockRenderTypes::contains);

		KaleidoClient.init();
		ModBlockItem.INSTANT_UPDATE_TILES.add(MASTER);
	}

	@Override
	protected void init(FMLCommonSetupEvent event) {
		Hooks.coreEnabled = true;

		Behavior.registerFactory("itemStorage", ItemStorageBehavior::new);
		Behavior.registerFactory("food", FoodBehavior::new);
		Behavior.registerFactory("event.useOnBlock", EventBehavior::new);
		Behavior.registerFactory("event.attackBlock", EventBehavior::new);
		Behavior.registerFactory("event.projectileHit", EventBehavior::new);
		Behavior.registerFactory("event.redstoneOn", EventBehavior::new);
		Behavior.registerFactory("event.redstoneOff", EventBehavior::new);

		Action.registerFactory("transform", TransformAction::new);
		Action.registerFactory("command", CommandAction::new);
		Action.registerFactory("sit", SitAction::new);

		BlockDefinition.registerFactory(SimpleBlockDefinition.Factory.INSTANCE);
		BlockDefinition.registerFactory(DynamicBlockDefinition.Factory.INSTANCE);
		BlockDefinition.registerFactory(KaleidoBlockDefinition.Factory.INSTANCE);

		CraftingHelper.register(new ResourceLocation(Kaleido.MODID), KaleidoIngredient.Serializer.INSTANCE);
	}

	@Override
	protected void preInit() {
		NetworkChannel.register(SSyncModelsPacket.class, new SSyncModelsPacket.Handler());
		NetworkChannel.register(SUnlockModelsPacket.class, new SUnlockModelsPacket.Handler());
		NetworkChannel.register(SSyncShapesPacket.class, new SSyncShapesPacket.Handler(KaleidoDataManager.INSTANCE.shapeCache));
		NetworkChannel.register(SSyncBehaviorsPacket.class, new SSyncBehaviorsPacket.Handler());

		NetworkChannel.register(CRedeemPacket.class, new CRedeemPacket.Handler());
		NetworkChannel.register(CChiselPickPacket.class, new CChiselPickPacket.Handler());
		NetworkChannel.register(CCreateScopePacket.class, new CCreateScopePacket.Handler());
		NetworkChannel.register(CConfigureScopePacket.class, new CConfigureScopePacket.Handler());
		NetworkChannel.register(CConfigureBrushPacket.class, new CConfigureBrushPacket.Handler());
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
				TileEntity blockEntity = level.getBlockEntity(pos);
				if (blockEntity instanceof MasterBlockEntity) {
					return ((MasterBlockEntity) blockEntity).getColor(state, level, pos, i);
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
			ModelInfo info = KaleidoBlock.getInfo(stack);
			if (info != null) {
				return info.getItemColor(stack, i);
			}
			return -1;
		}, STUFF_ITEM);
	}

}
