package snownee.kaleido;

import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.block.entity.MasterBlockEntity;
import snownee.kaleido.core.client.model.KaleidoModel;

public final class Hooks {

	@OnlyIn(Dist.CLIENT)
	private static final ResourceLocation DEFAULT_PARENT = new ResourceLocation("block/block");

	@OnlyIn(Dist.CLIENT)
	public static IBakedModel replaceKaleidoModel(IBlockDisplayReader worldIn, IBakedModel modelIn, BlockState stateIn, BlockPos posIn, MatrixStack matrixIn, IVertexBuilder buffer, boolean checkSides, Random randomIn, long rand, int combinedOverlayIn, IModelData modelData) {
		ModelInfo info = modelData.getData(MasterBlockEntity.MODEL);
		if (info != null && info.offset != AbstractBlock.OffsetType.NONE) {
			Vector3d offset = info.getOffset(posIn);
			matrixIn.translate(offset.x, offset.y, offset.z);
		}
		return KaleidoModel.getModel(info, stateIn);
	}

	@OnlyIn(Dist.CLIENT)
	public static boolean skipRender(BlockState state, IBlockReader level, BlockPos pos, Direction direction) {
		BlockPos blockpos = pos.relative(direction);
		BlockState blockstate = level.getBlockState(blockpos);
		if (state.is(blockstate.getBlock())) {
			ModelInfo info1 = ModelInfo.get(level, pos);
			ModelInfo info2 = ModelInfo.get(level, blockpos);
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
			if (iunbakedmodel == null) {
				return;
			}

			if (set.contains(iunbakedmodel)) {
				return;
			}

			if (!(iunbakedmodel instanceof BlockModel)) {
				return;
			}

			blockModel = (BlockModel) iunbakedmodel;
		}

		if (blockModel.transforms == ItemCameraTransforms.NO_TRANSFORMS) {
			blockModel.parentLocation = DEFAULT_PARENT;
		}
	}
}
