package snownee.kaleido;

import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
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

	public static IBakedModel replaceKaleidoModel(IBlockDisplayReader worldIn, IBakedModel modelIn, BlockState stateIn, BlockPos posIn, MatrixStack matrixIn, IVertexBuilder buffer, boolean checkSides, Random randomIn, long rand, int combinedOverlayIn, IModelData modelData) {
		ModelInfo info = modelData.getData(MasterBlockEntity.MODEL);
		if (info != null && info.offset != AbstractBlock.OffsetType.NONE) {
			long i = MathHelper.getSeed(posIn.getX(), 0, posIn.getZ());
			Vector3d offset = new Vector3d(((i & 15L) / 15.0F - 0.5D) * 0.5D, info.offset == AbstractBlock.OffsetType.XYZ ? ((i >> 4 & 15L) / 15.0F - 1.0D) * 0.2D : 0.0D, ((i >> 8 & 15L) / 15.0F - 0.5D) * 0.5D);
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
}
