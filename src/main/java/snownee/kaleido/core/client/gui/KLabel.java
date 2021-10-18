package snownee.kaleido.core.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;
import snownee.kaleido.util.KaleidoUtil;

public class KLabel extends Widget {

	public KLabel(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage) {
		super(pX, pY, pWidth, pHeight, pMessage);
	}

	@Override
	public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
		Minecraft minecraft = Minecraft.getInstance();
		FontRenderer fontrenderer = minecraft.font;
		renderBg(pMatrixStack, minecraft, pMouseX, pMouseY);
		int j = getFGColor();
		drawString(pMatrixStack, fontrenderer, getMessage(), x, y, KaleidoUtil.applyAlpha(j, alpha));
		//drawCenteredString(pMatrixStack, fontrenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
	}

	@Override
	protected boolean isValidClickButton(int pButton) {
		return false;
	}

	@Override
	public boolean changeFocus(boolean pFocus) {
		return false;
	}
}
