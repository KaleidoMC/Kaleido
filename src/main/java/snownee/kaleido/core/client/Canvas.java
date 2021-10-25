package snownee.kaleido.core.client;

import java.io.IOException;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.google.gson.JsonSyntaxException;
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
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;
import snownee.kaleido.Kaleido;
import snownee.kaleido.util.SimulationBlockReader;

@SuppressWarnings("deprecation")
@OnlyIn(Dist.CLIENT)
public final class Canvas implements IResourceManagerReloadListener {

	private final Minecraft mc = Minecraft.getInstance();
	private final RenderState.AlphaState MIDWAY_ALPHA = new RenderState.AlphaState(0.5F);
	private final RenderState.TextureState BLOCK_SHEET = new RenderState.TextureState(PlayerContainer.BLOCK_ATLAS, false, false);
	private Framebuffer framebuffer;
	private final RenderType renderType = RenderType.create("kaleido:pick", DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_QUADS, 131072, true, false, RenderType.State.builder().setTextureState(BLOCK_SHEET).setAlphaState(MIDWAY_ALPHA).createCompositeState(true));
	private final SimulationBlockReader simulationLevel = new SimulationBlockReader();
	private ShaderGroup blur;

	public Canvas() {
		RenderSystem.recordRenderCall(() -> {
			framebuffer = new Framebuffer(mc.getWindow().getWidth(), mc.getWindow().getHeight(), true, Minecraft.ON_OSX);
			onResourceManagerReload(mc.getResourceManager());
		});
		simulationLevel.setOverrideLight(15);
		simulationLevel.useSelfLight(true);
	}

	public int pickColor(World level, BlockPos pos, BlockState state, MatrixStack matrixStack) {
		if (state.getRenderShape() != BlockRenderType.MODEL) {
			return -1; //TODO
		}
		int width = mc.getWindow().getWidth();
		int height = mc.getWindow().getHeight();
		if (framebuffer.viewWidth != width || framebuffer.viewHeight != height) {
			framebuffer.resize(width, height, Minecraft.ON_OSX);
		}
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

		framebuffer.clear(Minecraft.ON_OSX);

		int a = abgr >> 24 & 255;
		int b = abgr >> 16 & 255;
		int g = abgr >> 8 & 255;
		int r = abgr & 255;
		return (a << 24) + (r << 16) + (g << 8) + b;
	}

	public void fillBlur(MatrixStack matrix, float mixX, float minY, float maxX, float maxY, int bgColor, float blurRadius) {
		if (blurRadius > 0.1F && blur != null && Minecraft.useFancyGraphics()) {
			int width = mc.getWindow().getGuiScaledWidth();
			int height = mc.getWindow().getGuiScaledHeight();
			float y1 = 1 - minY / height;
			float y0 = 1 - maxY / height;
			for (Shader shader : blur.passes) {
				shader.getEffect().safeGetUniform("Radius").set(blurRadius);
				shader.getEffect().safeGetUniform("Area").set(mixX / width, y0, maxX / width, y1);
				//shader.getEffect().safeGetUniform("Area").set(0, 0, 0.5F, 0.5F);
			}
			blur.process(mc.getFrameTime());
			mc.getMainRenderTarget().bindWrite(true);
		}
		KaleidoClient.fill(matrix, mixX, minY, maxX, maxY, bgColor);
	}

	@Override
	public void onResourceManagerReload(IResourceManager pResourceManager) {
		if (blur != null) {
			blur.close();
		}
		ResourceLocation pResourceLocation = new ResourceLocation(Kaleido.MODID, "shaders/post/blur.json");
		try {
			blur = new ShaderGroup(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), pResourceLocation);
			blur.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
		} catch (IOException ioexception) {
			Kaleido.logger.warn("Failed to load shader: {}", pResourceLocation, ioexception);
		} catch (JsonSyntaxException jsonsyntaxexception) {
			Kaleido.logger.warn("Failed to parse shader: {}", pResourceLocation, jsonsyntaxexception);
		} catch (Throwable e) {
			Kaleido.logger.catching(e);
		}
	}

	public void resize() {
		if (blur != null)
			blur.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
	}

}
