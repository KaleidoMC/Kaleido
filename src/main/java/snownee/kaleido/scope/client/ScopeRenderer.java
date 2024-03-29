package snownee.kaleido.scope.client;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kaleido.scope.ScopeStack;
import snownee.kaleido.scope.block.ScopeBlockEntity;
import snownee.kaleido.util.SimulationBlockReader;

@OnlyIn(Dist.CLIENT)
public class ScopeRenderer extends TileEntityRenderer<ScopeBlockEntity> {

	private final SimulationBlockReader blockReader = new SimulationBlockReader();

	public ScopeRenderer(TileEntityRendererDispatcher p_i226006_1_) {
		super(p_i226006_1_);
	}

	@Override
	public void render(ScopeBlockEntity scope, float pPartialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int pCombinedLight, int pCombinedOverlay) {
		blockReader.setLevel(Minecraft.getInstance().level);
		BlockModelRenderer.clearCache();
		Direction facing = scope.getBlockState().getValue(HorizontalBlock.FACING);
		for (ScopeStack stack : scope.stacks) {
			stack.render(matrixStack, buffer, blockReader, scope.getBlockPos(), pCombinedOverlay, scope.hasLevel(), facing.toYRot());
		}
		BlockModelRenderer.enableCaching();
	}

}
