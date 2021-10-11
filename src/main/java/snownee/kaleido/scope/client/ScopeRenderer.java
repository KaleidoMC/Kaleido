package snownee.kaleido.scope.client;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kaleido.core.util.SimulationBlockReader;
import snownee.kaleido.scope.ScopeStack;
import snownee.kaleido.scope.block.ScopeBlockEntity;

@OnlyIn(Dist.CLIENT)
public class ScopeRenderer extends TileEntityRenderer<ScopeBlockEntity> {

	private final SimulationBlockReader blockReader = new SimulationBlockReader();

	public ScopeRenderer(TileEntityRendererDispatcher p_i226006_1_) {
		super(p_i226006_1_);
	}

	@Override
	public void render(ScopeBlockEntity entity, float p_225616_2_, MatrixStack matrixStack, IRenderTypeBuffer buffer, int p_225616_5_, int p_225616_6_) {
		blockReader.setLevel(Minecraft.getInstance().level);
		BlockModelRenderer.clearCache();
		for (ScopeStack stack : entity.stacks) {
			stack.render(matrixStack, buffer, blockReader, entity.getBlockPos());
		}
		BlockModelRenderer.enableCaching();
	}

}
