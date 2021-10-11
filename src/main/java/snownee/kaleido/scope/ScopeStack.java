package snownee.kaleido.scope;

import java.util.Random;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import snownee.kaleido.core.client.KaleidoClient;
import snownee.kaleido.core.definition.BlockDefinition;
import snownee.kaleido.core.definition.DynamicBlockDefinition;
import snownee.kaleido.core.util.SimulationBlockReader;

public class ScopeStack {

	@OnlyIn(Dist.CLIENT)
	private static BlockModelRenderer modelRenderer;
	private BlockDefinition blockDefinition;
	private Vector3d translation = Vector3d.ZERO;

	public ScopeStack(BlockDefinition blockDefinition) {
		this.blockDefinition = blockDefinition;
	}

	@OnlyIn(Dist.CLIENT)
	public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, SimulationBlockReader world, BlockPos pos) {
		for (RenderType renderType : KaleidoClient.blockRenderTypes) {
			if (!blockDefinition.canRenderInLayer(renderType)) {
				continue;
			}
			if (modelRenderer == null) {
				modelRenderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
			}
			ForgeHooksClient.setRenderLayer(renderType);
			IVertexBuilder vertexBuilder = buffer.getBuffer(renderType);
			//BlockModelRenderer.enableCaching();

			translation = new Vector3d(-0.5, 0.5, -0.5);
			boolean transformed = isTransformed();
			if (transformed) {
				matrixStack.pushPose();
				matrixStack.translate(translation.x, translation.y, translation.z);
				matrixStack.translate(0.5, 0.5, 0.5);
				Minecraft mc = Minecraft.getInstance();
				matrixStack.mulPose(Vector3f.YN.rotation((mc.level.getGameTime() + mc.getFrameTime()) * 0.2F));
				matrixStack.translate(-0.5, -0.5, -0.5);
			}
			world.useSelfLight(transformed);

			world.setPos(pos);
			if (blockDefinition.getClass() == DynamicBlockDefinition.class) {
				world.setBlockEntity(((DynamicBlockDefinition) blockDefinition).blockEntity);
			} else {
				world.setBlockEntity(null);
			}
			BlockState blockState = blockDefinition.getBlockState();
			modelRenderer.renderModel(world, blockDefinition.model(), blockState, pos, matrixStack, vertexBuilder, !transformed, new Random(), blockState.getSeed(pos), OverlayTexture.NO_OVERLAY, blockDefinition.modelData());

			if (transformed) {
				matrixStack.popPose();
			}
		}
		ForgeHooksClient.setRenderLayer(null);
	}

	public boolean isTransformed() {
		return translation.x != 0 || translation.y != 0 || translation.z != 0;
	}

	public void save(CompoundNBT tag) {
		CompoundNBT def = new CompoundNBT();
		blockDefinition.save(def);
		def.putString("Type", blockDefinition.getFactory().getId());
		tag.put("Def", def);
	}

	@Nullable
	public static ScopeStack load(CompoundNBT tag) {
		BlockDefinition blockDefinition = BlockDefinition.fromNBT(tag.getCompound("Def"));
		if (blockDefinition == null) {
			return null;
		}
		ScopeStack stack = new ScopeStack(blockDefinition);
		return stack;
	}
}
