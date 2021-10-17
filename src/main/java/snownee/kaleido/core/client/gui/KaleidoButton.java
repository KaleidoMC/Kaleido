package snownee.kaleido.core.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kaleido.core.client.KaleidoClient;
import snownee.kaleido.util.KaleidoUtil;
import snownee.kaleido.util.SmoothChasingValue;

@OnlyIn(Dist.CLIENT)
public class KaleidoButton extends Button {

	public int xOffset;
	public int yOffset;
	public int lineColor;
	private final SmoothChasingValue hoverProgress = new SmoothChasingValue();
	public final AnimatedWidget pos = new AnimatedWidget();

	public float textWidth;
	public ITextProperties wrappedTitle;

	public KaleidoButton(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage, IPressable pOnPress) {
		this(pX, pY, pWidth, pHeight, pMessage, pOnPress, NO_TOOLTIP);
	}

	public KaleidoButton(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage, IPressable pOnPress, ITooltip pOnTooltip) {
		super(pX, pY, pWidth, pHeight, pMessage, pOnPress, pOnTooltip);
		pos.x.start(pX);
		pos.y.start(pY);
		lineColor = 0xEEEEEE;
		hoverProgress.withSpeed(0.5F);
		onMessageUpdate(pMessage);
	}

	@Override
	public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
		pos.tick(this, pPartialTicks);
		hoverProgress.target(isHovered() ? 1 : 0).tick(pPartialTicks);
		super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
	}

	@Override
	public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
		Minecraft minecraft = Minecraft.getInstance();

		int bgColor = active ? 0xFF555555 : 0x66555555;
		float alpha = this.alpha * (0.6F + hoverProgress.value * 0.4F);
		KaleidoClient.fill(pMatrixStack, x + xOffset, y + yOffset, x + width, y + height, KaleidoUtil.applyAlpha(bgColor, alpha));
		drawUnderline(pMatrixStack, alpha, pPartialTicks);

		renderBg(pMatrixStack, minecraft, pMouseX, pMouseY);
		drawText(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
		if (isHovered()) {
			renderToolTip(pMatrixStack, pMouseX, pMouseY);
		}
	}

	public void drawText(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
		FontRenderer font = Minecraft.getInstance().font;
		int j = getFGColor();
		int textColor = j | MathHelper.ceil(alpha * 255.0F) << 24;
		RenderSystem.disableDepthTest();
		if (wrappedTitle != null) {
			ITextProperties properties = isHovered ? getMessage() : wrappedTitle;
			IReorderingProcessor processor = LanguageMap.getInstance().getVisualOrder(properties);
			font.drawShadow(pMatrixStack, processor, x + 2, y + (height - 8) / 2, textColor);
		} else {
			drawCenteredString(pMatrixStack, font, getMessage(), x + width / 2, y + (height - 8) / 2, textColor);
		}
		RenderSystem.enableDepthTest();
	}

	public void drawUnderline(MatrixStack pMatrixStack, float alpha, float pPartialTicks) {
		if (active) {
			float w = (width - xOffset) * (1 - hoverProgress.value) * 0.5F;
			KaleidoClient.fill(pMatrixStack, x + xOffset + w, y + height - 1, x + width - w, y + height, KaleidoUtil.applyAlpha(lineColor, alpha));
		}
	}

	@Override // make it public
	public void setFocused(boolean pFocused) {
		super.setFocused(pFocused);
	}

	public boolean hackyIsHovered() {
		return isHovered;
	}

	@Override
	public void setMessage(ITextComponent pMessage) {
		super.setMessage(pMessage);
		onMessageUpdate(pMessage);
	}

	@Override
	public void setWidth(int pWidth) {
		super.setWidth(pWidth);
		onMessageUpdate(getMessage());
	}

	public void onMessageUpdate(ITextComponent pMessage) {
		FontRenderer font = Minecraft.getInstance().font;
		textWidth = font.getSplitter().stringWidth(pMessage);
		if (textWidth > width + 4) {
			ITextProperties line = font.getSplitter().headByWidth(pMessage, width - 4, Style.EMPTY);
			ITextProperties dots = ITextProperties.of("..");
			wrappedTitle = ITextProperties.composite(line, dots);
		}
	}

}
