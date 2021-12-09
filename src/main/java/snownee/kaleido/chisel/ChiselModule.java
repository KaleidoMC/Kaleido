package snownee.kaleido.chisel;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import snownee.kaleido.Hooks;
import snownee.kaleido.chisel.block.ChiseledFenceBlock;
import snownee.kaleido.chisel.block.ChiseledFenceGateBlock;
import snownee.kaleido.chisel.block.ChiseledLayersBlock;
import snownee.kaleido.chisel.block.ChiseledSideBlock;
import snownee.kaleido.chisel.block.ChiseledSlabBlock;
import snownee.kaleido.chisel.block.ChiseledStairsBlock;
import snownee.kaleido.chisel.block.ChiseledWallBlock;
import snownee.kaleido.chisel.block.entity.ChiseledBlockEntity;
import snownee.kaleido.chisel.block.entity.RetextureBlockEntity;
import snownee.kaleido.chisel.client.model.RetextureModel;
import snownee.kaleido.chisel.item.ChiselItem;
import snownee.kaleido.core.client.KaleidoClient;
import snownee.kaleido.core.definition.BlockDefinition;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.KiwiModule.Subscriber.Bus;
import snownee.kiwi.item.ModBlockItem;
import snownee.kiwi.util.Util;

@KiwiModule("chisel")
@KiwiModule.Optional
@KiwiModule.Subscriber(Bus.MOD)
public class ChiselModule extends AbstractModule {

	public static final Set<Block> CHISELED_BLOCKS = Sets.newHashSet();

	public static final ChiselItem CHISEL = new ChiselItem(itemProp());

	public static final ChiseledStairsBlock CHISELED_STAIRS = chisel(new ChiseledStairsBlock());

	public static final ChiseledWallBlock CHISELED_WALL = chisel(new ChiseledWallBlock());

	public static final ChiseledFenceBlock CHISELED_FENCE = chisel(new ChiseledFenceBlock());

	public static final ChiseledFenceGateBlock CHISELED_FENCE_GATE = chisel(new ChiseledFenceGateBlock());

	public static final ChiseledSlabBlock CHISELED_SLAB = chisel(new ChiseledSlabBlock());

	public static final ChiseledSideBlock CHISELED_VSLAB = chisel(new ChiseledSideBlock(Block.box(0, 0, 8, 16, 16, 16), blockProp(Blocks.STONE_SLAB)));

	public static final ChiseledLayersBlock CHISELED_LAYERS = chisel(new ChiseledLayersBlock());

	public static final ChiseledSideBlock CHISELED_THIN_POST = chisel(new ChiseledSideBlock(Block.box(6, 0, 12, 10, 16, 16), blockProp(Blocks.STONE_SLAB)));

	public static final TileEntityType<ChiseledBlockEntity> CHISELED = new TileEntityType.Builder<>(ChiseledBlockEntity::new, CHISELED_BLOCKS).build(null);

	public static <T extends Block> T chisel(T t) {
		CHISELED_BLOCKS.add(t);
		return t;
	}

	@Override
	protected void init(FMLCommonSetupEvent event) {
		Hooks.chiselEnabled = true;
		ChiselPalette.init();
		MinecraftForge.EVENT_BUS.register(ChiselItem.class);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected void clientInit(FMLClientSetupEvent event) {
		ModBlockItem.INSTANT_UPDATE_TILES.add(CHISELED);
		for (Block block : CHISELED_BLOCKS)
			RenderTypeLookup.setRenderLayer(block, KaleidoClient.blockRenderTypes::contains);
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void registerModelLoader(ModelRegistryEvent event) {
		ModelLoaderRegistry.registerLoader(Util.RL("kiwi:retexture"), RetextureModel.Loader.INSTANCE);
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void blockColors(ColorHandlerEvent.Block event) {
		BlockColors blockColors = event.getBlockColors();
		blockColors.register((state, level, pos, i) -> {
			if (level != null && pos != null) {
				TileEntity blockEntity = level.getBlockEntity(pos);
				if (blockEntity instanceof RetextureBlockEntity) {
					return ((RetextureBlockEntity) blockEntity).getColor(level, i);
				}
			}
			return -1;
		}, CHISELED_BLOCKS.toArray(new Block[0]));
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void itemColors(ColorHandlerEvent.Item event) {
		ItemColors itemColors = event.getItemColors();
		itemColors.register((stack, i) -> {
			Map<String, BlockDefinition> textures = RetextureModel.OverrideList.overridesFromItem(stack);
			return RetextureModel.getColor(textures, null, Minecraft.getInstance().level, null, i);
		}, CHISELED_BLOCKS.toArray(new Block[0]));
	}
}
