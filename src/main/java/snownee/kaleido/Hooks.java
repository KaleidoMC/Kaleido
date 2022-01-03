package snownee.kaleido;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractBlock.AbstractBlockState;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IPackNameDecorator;
import net.minecraft.resources.ResourcePackList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.loading.FMLEnvironment;
import snownee.kaleido.chisel.ChiselModule;
import snownee.kaleido.chisel.block.entity.ChiseledBlockEntity;
import snownee.kaleido.chisel.item.ChiselItem;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.block.KaleidoBlock;
import snownee.kaleido.core.client.KaleidoClient;
import snownee.kaleido.core.client.model.KaleidoModel;
import snownee.kaleido.core.definition.BlockDefinition;
import snownee.kaleido.resources.JarPackFinder;
import snownee.kaleido.resources.RequiredFolderPackFinder;
import snownee.kaleido.scope.ScopeStack;
import snownee.kaleido.scope.client.model.ScopeModel;
import snownee.kaleido.util.SimulationBlockReader;

public final class Hooks {

	public static boolean carpentryEnabled;
	public static boolean chiselEnabled;
	public static boolean scopeEnabled;
	public static boolean brushEnabled;
	public static boolean hubEnabled;
	public static boolean coreEnabled;

	@OnlyIn(Dist.CLIENT)
	private static ResourceLocation DEFAULT_PARENT;

	@OnlyIn(Dist.CLIENT)
	private static SimulationBlockReader blockReader;

	private static final MethodHandle GET_STATE_FOR_PLACEMENT;

	static {
		if (FMLEnvironment.dist.isClient()) {
			DEFAULT_PARENT = new ResourceLocation("block/block");
			blockReader = new SimulationBlockReader();
		}

		MethodHandle m = null;
		try {
			m = MethodHandles.lookup().unreflect(ObfuscationReflectionHelper.findMethod(BlockItem.class, "func_195945_b", BlockItemUseContext.class));
		} catch (Exception e) {
			throw new RuntimeException("Report this to author", e);
		}
		GET_STATE_FOR_PLACEMENT = m;
	}

	@Nullable
	public static BlockState getStateForPlacement(BlockItem blockItem, BlockItemUseContext context) {
		try {
			return (BlockState) GET_STATE_FOR_PLACEMENT.invokeExact(blockItem, context);
		} catch (Throwable e) {
			return null;
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static IBakedModel replaceKaleidoModel(IBlockDisplayReader worldIn, IBakedModel modelIn, BlockState stateIn, BlockPos posIn, MatrixStack matrixIn, IVertexBuilder buffer, boolean checkSides, Random randomIn, long rand, int combinedOverlayIn, IModelData modelData) {
		ModelInfo info = modelData.getData(KaleidoModel.MODEL);
		if (info != null && info.offset != AbstractBlock.OffsetType.NONE) {
			Vector3d offset = info.getOffset(posIn);
			matrixIn.translate(offset.x, offset.y, offset.z);
		}
		return KaleidoModel.getModel(info, stateIn, true);
	}

	@OnlyIn(Dist.CLIENT)
	public static void renderScopeModel(IBlockDisplayReader worldIn, IBakedModel modelIn, BlockState stateIn, BlockPos posIn, MatrixStack matrixIn, IVertexBuilder buffer, boolean checkSides, Random randomIn, long rand, int combinedOverlayIn, IModelData modelData) {
		List<ScopeStack> stacks = modelData.getData(ScopeModel.STACKS);
		if (stacks == null || stacks.isEmpty())
			return;
		BlockModelRenderer.clearCache();
		RenderType layer = MinecraftForgeClient.getRenderLayer();
		blockReader.setLevel(worldIn);
		Direction facing = stateIn.getValue(HorizontalBlock.FACING);
		for (ScopeStack stack : stacks) {
			if (layer == null || stack.blockDefinition.canRenderInLayer(layer))
				stack.render(matrixIn, buffer, blockReader, posIn, combinedOverlayIn, randomIn, rand, checkSides, facing.toYRot());
		}
		BlockModelRenderer.enableCaching();
	}

	@OnlyIn(Dist.CLIENT)
	public static boolean skipRender(BlockState state, IBlockReader level, BlockPos pos, Direction direction) {
		BlockPos blockpos = pos.relative(direction);
		BlockState blockstate = level.getBlockState(blockpos);
		if (state.is(blockstate.getBlock())) {
			ModelInfo info1 = KaleidoBlock.getInfo(level, pos);
			ModelInfo info2 = KaleidoBlock.getInfo(level, blockpos);
			if (info1 != null && info1 == info2 && info1.glass) {
				return true;
			}
		}
		return false;
	}

	@OnlyIn(Dist.CLIENT)
	public static void forceTransforms(Function<ResourceLocation, IUnbakedModel> modelGetter, BlockModel blockModel) {
		Set<IUnbakedModel> set = Sets.newLinkedHashSet();
		ResourceLocation location;
		while (blockModel.parentLocation != null) {
			if (blockModel.transforms != ItemCameraTransforms.NO_TRANSFORMS) {
				return;
			}
			set.add(blockModel);
			location = blockModel.parentLocation;
			IUnbakedModel iunbakedmodel = modelGetter.apply(location);
			if ((iunbakedmodel == null) || set.contains(iunbakedmodel) || !(iunbakedmodel instanceof BlockModel)) {
				return;
			}

			blockModel = (BlockModel) iunbakedmodel;
		}

		if (blockModel.transforms == ItemCameraTransforms.NO_TRANSFORMS) {
			blockModel.parentLocation = DEFAULT_PARENT;
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static boolean shouldRenderFaceChiseled(BlockState state, IBlockReader level, BlockPos pos, Direction direction) {
		if (!chiselEnabled || !state.canOcclude()) {
			return false;
		}
		pos = pos.relative(direction);
		state = level.getBlockState(pos);
		if (!state.hasTileEntity() || !ChiselModule.CHISELED_BLOCKS.contains(state.getBlock())) {
			return false;
		}
		TileEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof ChiseledBlockEntity) {
			if (!((ChiseledBlockEntity) blockEntity).canOcclude())
				return true;
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	@OnlyIn(Dist.CLIENT)
	public static void renderChiselOverlay(ItemStack stack, int xPosition, int yPosition) {
		ItemStack icon = ChiselItem.palette(stack).icon();
		if (icon.isEmpty())
			return;
		Minecraft mc = Minecraft.getInstance();
		RenderSystem.pushMatrix();
		RenderSystem.translatef(xPosition + 7, yPosition + 7, 200);
		RenderSystem.scalef(0.55F, 0.55F, 0.55F);
		mc.getItemRenderer().renderGuiItem(icon, 0, 0);
		RenderSystem.popMatrix();
	}

	public static MaterialColor getMapColor(AbstractBlockState state, IBlockReader pLevel, BlockPos pPos) {
		TileEntity blockEntity = pLevel.getBlockEntity(pPos);
		if (blockEntity instanceof ChiseledBlockEntity) {
			BlockDefinition definition = ((ChiseledBlockEntity) blockEntity).getTexture();
			if (definition != null) {
				BlockState blockState = definition.getBlockState();
				if (!ChiselModule.CHISELED_BLOCKS.contains(blockState.getBlock())) {
					return blockState.getMapColor(pLevel, pPos);
				}
			}
		}
		return MaterialColor.NONE;
	}

	public static void loadResourcePacks(ResourcePackList resourcePacks) {
		if (FMLEnvironment.dist.isClient()) {
			loadResourcePacksClient(resourcePacks);
		} else {
			addDataPackFinder(resourcePacks);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static void loadResourcePacksClient(ResourcePackList resourcePacks) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.getResourcePackRepository() == resourcePacks) {
			resourcePacks.addPackFinder(new JarPackFinder(Minecraft.getInstance().getResourcePackDirectory(), IPackNameDecorator.DEFAULT));
			File folder = new File("kaleido-loader");
			if (!folder.isDirectory()) {
				folder.mkdirs();
			}
		} else {
			addDataPackFinder(resourcePacks);
		}
	}

	public static void addDataPackFinder(ResourcePackList resourcePacks) {
		resourcePacks.addPackFinder(new RequiredFolderPackFinder(new File("kaleido-loader"), IPackNameDecorator.DEFAULT));
	}

	@OnlyIn(Dist.CLIENT)
	public static void resizeDisplay() {
		if (coreEnabled)
			KaleidoClient.CANVAS.resize();
	}
}
