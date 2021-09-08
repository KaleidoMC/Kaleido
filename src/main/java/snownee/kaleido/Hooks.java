package snownee.kaleido;

import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockDisplayReader;
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

}
