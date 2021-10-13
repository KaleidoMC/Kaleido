package snownee.kaleido.scope.client.gui;

import java.util.Arrays;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.gui.GuiUtils;

@OnlyIn(Dist.CLIENT)
public class CheckboxButton extends Button {

	private boolean selected;

	public CheckboxButton(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage, IPressable pOnPress) {
		super(pX, pY, pWidth, pHeight, pMessage, pOnPress);
	}

	public void onPress() {
		this.selected = !this.selected;
		super.onPress();
	}

	public boolean selected() {
		return this.selected;
	}

	protected int getYImage(boolean pIsHovered) {
		int i = 1;
		if (!this.active) {
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

}
