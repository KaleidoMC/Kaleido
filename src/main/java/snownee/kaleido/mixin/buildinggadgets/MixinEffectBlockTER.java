package snownee.kaleido.mixin.buildinggadgets;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.direwolf20.buildinggadgets.client.renderer.EffectBlockTER;
import com.direwolf20.buildinggadgets.client.renderer.OurRenderTypes;
import com.direwolf20.buildinggadgets.common.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import com.direwolf20.buildinggadgets.common.tainted.building.BlockData;
import com.direwolf20.buildinggadgets.common.tileentities.EffectBlockTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import snownee.kaleido.compat.buildinggadgets.KaleidoTileData;

@Mixin(value = EffectBlockTER.class, remap = false)
public class MixinEffectBlockTER {
	@Inject(at = @At("HEAD"), method = "render", cancellable = true)
	private void render(EffectBlockTileEntity tile, float partialTicks, MatrixStack stack, IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn, CallbackInfo ci) {
		ci.cancel();
		BlockData renderData = tile.getRenderedBlock();
		if (renderData == null)
			return;
		IVertexBuilder builder;

		IRenderTypeBuffer.Impl buffer2 = Minecraft.getInstance().renderBuffers().bufferSource();
		EffectBlock.Mode toolMode = tile.getReplacementMode();
		BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();

		int teCounter = tile.getTicksExisted();
		int maxLife = tile.getLifespan();
		teCounter = Math.min(teCounter, maxLife);

		float scale = (float) (teCounter) / (float) maxLife;
		if (scale >= 1.0f)
			scale = 0.99f;
		if (toolMode == EffectBlock.Mode.REMOVE || toolMode == EffectBlock.Mode.REPLACE)
			scale = (float) (maxLife - teCounter) / maxLife;

		float trans = (1 - scale) / 2;

		stack.pushPose();
		stack.translate(trans, trans, trans);
		stack.scale(scale, scale, scale);

		BlockState renderBlockState = renderData.getState();
		/// Kaleido patch START
		IModelData modelData = EmptyModelData.INSTANCE;
		if (renderData.getTileData() instanceof KaleidoTileData)
			modelData = ((KaleidoTileData) renderData.getTileData()).getModelData();
		/// Kaleido patch END

		if (tile.isUsingPaste() && toolMode == EffectBlock.Mode.PLACE)
			renderBlockState = OurBlocks.CONSTRUCTION_DENSE_BLOCK.get().defaultBlockState();

		OurRenderTypes.MultiplyAlphaRenderTypeBuffer mutatedBuffer = new OurRenderTypes.MultiplyAlphaRenderTypeBuffer(Minecraft.getInstance().renderBuffers().bufferSource(), .55f);
		try {
			dispatcher.renderBlock(renderBlockState, stack, mutatedBuffer, 15728640, OverlayTexture.NO_OVERLAY, modelData);
		} catch (Exception ignored) {
		} // if it fails to render then we'll get a bug report I'm sure.

		stack.popPose();
		stack.pushPose();

		builder = buffer.getBuffer(OurRenderTypes.MissingBlockOverlay);

		float x = 0, y = 0, z = 0, maxX = 1, maxY = 1, maxZ = 1, red = 0f, green = 1f, blue = 1f;

		if (toolMode == EffectBlock.Mode.REMOVE || toolMode == EffectBlock.Mode.REPLACE) {
			red = 1f;
			green = 0.25f;
			blue = 0.25f;
		}

		float alpha = (1f - (scale));
		if (alpha < 0.051f)
			alpha = 0.051f;

		if (alpha > 0.33f)
			alpha = 0.33f;

		Matrix4f matrix = stack.last().pose();

		// Down
		if (tile.getLevel().getBlockState(tile.getBlockPos().below()).getBlock() != OurBlocks.EFFECT_BLOCK.get()) {
			builder.vertex(matrix, x, y, z).color(red, green, blue, alpha).endVertex();
			builder.vertex(matrix, maxX, y, z).color(red, green, blue, alpha).endVertex();
			builder.vertex(matrix, maxX, y, maxZ).color(red, green, blue, alpha).endVertex();
			builder.vertex(matrix, x, y, maxZ).color(red, green, blue, alpha).endVertex();
		}
		// Up
		if (tile.getLevel().getBlockState(tile.getBlockPos().above()).getBlock() != OurBlocks.EFFECT_BLOCK.get()) {
			builder.vertex(matrix, x, maxY, z).color(red, green, blue, alpha).endVertex();
			builder.vertex(matrix, x, maxY, maxZ).color(red, green, blue, alpha).endVertex();
			builder.vertex(matrix, maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
			builder.vertex(matrix, maxX, maxY, z).color(red, green, blue, alpha).endVertex();
		}
		// North
		if (tile.getLevel().getBlockState(tile.getBlockPos().north()).getBlock() != OurBlocks.EFFECT_BLOCK.get()) {
			builder.vertex(matrix, x, y, z).color(red, green, blue, alpha).endVertex();
			builder.vertex(matrix, x, maxY, z).color(red, green, blue, alpha).endVertex();
			builder.vertex(matrix, maxX, maxY, z).color(red, green, blue, alpha).endVertex();
			builder.vertex(matrix, maxX, y, z).color(red, green, blue, alpha).endVertex();
		}
		// South
		if (tile.getLevel().getBlockState(tile.getBlockPos().south()).getBlock() != OurBlocks.EFFECT_BLOCK.get()) {
			builder.vertex(matrix, x, y, maxZ).color(red, green, blue, alpha).endVertex();
			builder.vertex(matrix, maxX, y, maxZ).color(red, green, blue, alpha).endVertex();
			builder.vertex(matrix, maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
			builder.vertex(matrix, x, maxY, maxZ).color(red, green, blue, alpha).endVertex();
		}
		// East
		if (tile.getLevel().getBlockState(tile.getBlockPos().east()).getBlock() != OurBlocks.EFFECT_BLOCK.get()) {
			builder.vertex(matrix, maxX, y, z).color(red, green, blue, alpha).endVertex();
			builder.vertex(matrix, maxX, maxY, z).color(red, green, blue, alpha).endVertex();
			builder.vertex(matrix, maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
			builder.vertex(matrix, maxX, y, maxZ).color(red, green, blue, alpha).endVertex();
		}
		// West
		if (tile.getLevel().getBlockState(tile.getBlockPos().west()).getBlock() != OurBlocks.EFFECT_BLOCK.get()) {
			builder.vertex(matrix, x, y, z).color(red, green, blue, alpha).endVertex();
			builder.vertex(matrix, x, y, maxZ).color(red, green, blue, alpha).endVertex();
			builder.vertex(matrix, x, maxY, maxZ).color(red, green, blue, alpha).endVertex();
			builder.vertex(matrix, x, maxY, z).color(red, green, blue, alpha).endVertex();
		}
		stack.popPose();
		buffer2.endBatch(); // @mcp: draw (yarn) = finish (mcp)
	}

}
