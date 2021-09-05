package snownee.kaleido.mixin;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.data.IModelData;
import snownee.kaleido.core.client.model.KaleidoModel;

@Mixin(BlockModelRenderer.class)
public abstract class MixinBlockModelRenderer {

	@Inject(at = @At("HEAD"), method = "renderModel", remap = false, cancellable = true)
	private void kaleido_renderModel(IBlockDisplayReader worldIn, IBakedModel modelIn, BlockState stateIn, BlockPos posIn, MatrixStack matrixIn, IVertexBuilder buffer, boolean checkSides, Random randomIn, long rand, int combinedOverlayIn, IModelData modelData, CallbackInfoReturnable<Boolean> ci) {
		if (modelIn.getClass() == KaleidoModel.class) {
			Direction direction = Direction.NORTH;
			if (stateIn != null && stateIn.hasProperty(HorizontalBlock.FACING)) {
				direction = stateIn.getValue(HorizontalBlock.FACING);
			}
			modelIn = KaleidoModel.getModel(modelData, direction);

			ci.setReturnValue(renderModel(worldIn, modelIn, stateIn, posIn, matrixIn, buffer, checkSides, randomIn, rand, combinedOverlayIn, modelData));
		}
	}

	@Shadow(remap = false)
	abstract boolean renderModel(IBlockDisplayReader worldIn, IBakedModel modelIn, BlockState stateIn, BlockPos posIn, MatrixStack matrixIn, IVertexBuilder buffer, boolean checkSides, Random randomIn, long rand, int combinedOverlayIn, IModelData modelData);
}
