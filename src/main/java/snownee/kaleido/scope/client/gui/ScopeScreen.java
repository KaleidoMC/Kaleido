package snownee.kaleido.scope.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.TranslationTextComponent;
import snownee.kaleido.core.client.gui.DarkBackground;
import snownee.kaleido.scope.block.ScopeBlockEntity;
import snownee.kaleido.scope.client.ScopeRenderer;

public class ScopeScreen extends Screen {

	private static final TileEntityRenderer<ScopeBlockEntity> TER = new ScopeRenderer(TileEntityRendererDispatcher.instance);

	private final DarkBackground background = new DarkBackground();
	private final ScopeBlockEntity scope;
	private float ticks;

	public ScopeScreen(ScopeBlockEntity blockEntity) {
		super(new TranslationTextComponent("gui.kaleido.scope"));
		scope = blockEntity;
	}

	@Override
	protected void init() {

	}

	@Override
	public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
		ticks += pPartialTicks;
		this.renderBackground(pMatrixStack);
		if (background.isClosed()) {
			Minecraft.getInstance().setScreen(null);
			return;
		}
		super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
		pMatrixStack.pushPose();
		GameRenderer gameRenderer = minecraft.gameRenderer;
		ActiveRenderInfo activerenderinfo = gameRenderer.getMainCamera();

		pMatrixStack.translate(0.5, 0.5, 0.5);
		pMatrixStack.translate(width / 2, height / 2, 0);
		pMatrixStack.scale(-40, -40, 40);

		pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(activerenderinfo.getXRot()));
		pMatrixStack.mulPose(Vector3f.YN.rotationDegrees(activerenderinfo.getYRot() + 180.0F));

		pMatrixStack.scale(-1.0F, 1.0F, 1.0F);
		pMatrixStack.translate(-0.5, -0.5, -0.5);

		IRenderTypeBuffer.Impl buffer = minecraft.renderBuffers().bufferSource();
		//		GhostRenderType.Buffer buffer = GhostRenderType.defaultBuffer();
		//		buffer.setAlpha(0.5F);
		TER.render(scope, pPartialTicks, pMatrixStack, buffer, 15728880, OverlayTexture.NO_OVERLAY);
		buffer.endBatch();
		pMatrixStack.popPose();
	}

	@Override
	public void renderBackground(MatrixStack matrix, int p_renderBackground_1_) {
		background.renderBackground(this, matrix, minecraft.getDeltaFrameTime());
	}

	@Override
	public void onClose() {
		background.closing = true;
	}

}
