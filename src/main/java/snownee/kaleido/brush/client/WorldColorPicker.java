package snownee.kaleido.brush.client;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;
import snownee.kaleido.util.SimulationBlockReader;

@OnlyIn(Dist.CLIENT)
public final class WorldColorPicker {

	private static final Minecraft mc = Minecraft.getInstance();
	private static final RenderState.AlphaState MIDWAY_ALPHA = new RenderState.AlphaState(0.5F);
	private static final RenderState.TextureState BLOCK_SHEET = new RenderState.TextureState(PlayerContainer.BLOCK_ATLAS, false, false);
	private static final Framebuffer framebuffer;
	private static final RenderType renderType = RenderType.create("kaleido:pick", DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_QUADS, 131072, true, false, RenderType.State.builder().setTextureState(BLOCK_SHEET).setAlphaState(MIDWAY_ALPHA).createCompositeState(true));
	private static final SimulationBlockReader simulationLevel = new SimulationBlockReader();

	static {
		framebuffer = new Framebuffer(mc.getWindow().getWidth(), mc.getWindow().getHeight(), true, Minecraft.ON_OSX);
		simulationLevel.setOverrideLight(15);
		simulationLevel.useSelfLight(true);
	}

	public static int pick(World level, BlockPos pos, BlockState state, MatrixStack matrixStack) {
		if (state.getRenderShape() != BlockRenderType.MODEL) {
			return -1; //TODO
		}
		int width = mc.getWindow().getWidth();
		int height = mc.getWindow().getHeight();
		if (framebuffer.viewWidth != width || framebuffer.viewHeight != height) {
			framebuffer.resize(width, height, Minecraft.ON_OSX);
		}
		framebuffer.clear(Minecraft.ON_OSX);
		framebuffer.bindWrite(false);

		Vector3d vector3d = mc.gameRenderer.getMainCamera().getPosition();

		IRenderTypeBuffer.Impl buffer = mc.renderBuffers().bufferSource();
		IVertexBuilder builder = buffer.getBuffer(renderType);

		matrixStack.pushPose();
		matrixStack.translate(pos.getX() - vector3d.x(), pos.getY() - vector3d.y(), pos.getZ() - vector3d.z());
		BlockModelRenderer modelRenderer = mc.getBlockRenderer().getModelRenderer();

		IBakedModel model = mc.getBlockRenderer().getBlockModel(state);
		Vector3d offset = state.getOffset(level, pos);
		matrixStack.translate(offset.x, offset.y, offset.z);
		IModelData modelData = ModelDataManager.getModelData(level, pos);
		modelData = model.getModelData(level, pos, state, modelData);
		simulationLevel.setLevel(level);
		simulationLevel.setPos(pos);
		if (!modelRenderer.renderModelFlat(simulationLevel, model, state, pos, matrixStack, builder, true, new Random(), state.getSeed(pos), OverlayTexture.NO_OVERLAY, modelData)) {
			return -1;
		}
		matrixStack.popPose();
		renderType.setupRenderState();
		buffer.endBatch(renderType);
		renderType.clearRenderState();

		mc.getMainRenderTarget().bindWrite(false);

		NativeImage nativeimage = new NativeImage(width, height, false);
		RenderSystem.bindTexture(framebuffer.getColorTextureId());
		nativeimage.downloadTexture(0, true);

		//nativeimage.flipY();

		//test code to see if the result is correct
		//		ScreenShotHelper.grab(mc.gameDirectory, width, height, framebuffer, $ -> {
		//			mc.execute(() -> {
		//				mc.gui.getChat().addMessage($);
		//			});
		//		});

		int abgr = nativeimage.getPixelRGBA(width / 2, height / 2);
		nativeimage.close();

		int a = abgr >> 24 & 255;
		int b = abgr >> 16 & 255;
		int g = abgr >> 8 & 255;
		int r = abgr & 255;
		return (a << 24) + (r << 16) + (g << 8) + b;
	}

}
