package snownee.kaleido;

import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.data.IModelData;
import snownee.kaleido.core.client.model.KaleidoModel;

public final class Hooks {

	public static IBakedModel onRenderKaleidoBlock(IBlockDisplayReader worldIn, IBakedModel modelIn, BlockState stateIn, BlockPos posIn, MatrixStack matrixIn, IVertexBuilder buffer, boolean checkSides, Random randomIn, long rand, int combinedOverlayIn, IModelData modelData) {
		Direction direction = Direction.NORTH;
		if (stateIn != null && stateIn.hasProperty(HorizontalBlock.FACING)) {
			direction = stateIn.getValue(HorizontalBlock.FACING);
		}
		return KaleidoModel.getModel(modelData, direction);
	}

}
