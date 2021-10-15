package snownee.kaleido.scope.client.gui;

import java.util.Arrays;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.gui.GuiUtils;
import snownee.kaleido.core.client.KaleidoClient;
import snownee.kaleido.core.client.gui.KaleidoButton;
import snownee.kaleido.util.KaleidoUtil;

@OnlyIn(Dist.CLIENT)
public class CheckboxButton extends KaleidoButton {

	private boolean selected;

	public CheckboxButton(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage, IPressable pOnPress) {
		super(pX, pY, pWidth, pHeight, pMessage, pOnPress);
	}

	@Override
	public void onPress() {
		selected = !selected;
		super.onPress();
	}

	public boolean selected() {
		return selected;
	}

	@Override
	protected int getYImage(boolean pIsHovered) {
		int i = 1;
		if (!active) {
			i = 0;
		} else if (pIsHovered || selected()) {
			i = 2;
		}
		return i;
	}

	@Override
	public void renderToolTip(MatrixStack pPoseStack, int pMouseX, int pMouseY) {
		//FIXME tooltip z index
		int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
		int height = Minecraft.getInstance().getWindow().getGuiScaledWidth();
		GuiUtils.drawHoveringText(pPoseStack, Arrays.asList(getMessage()), pMouseX, pMouseY, width, height, -1, Minecraft.getInstance().font);
	}

	@Override
	public void drawUnderline(MatrixStack pMatrixStack, float alpha, float pPartialTicks) {
		if (selected()) {
			KaleidoClient.fill(pMatrixStack, x + xOffset, y + height - 1, x + width, y + height, KaleidoUtil.applyAlpha(lineColor, alpha));
		}
	}

}
