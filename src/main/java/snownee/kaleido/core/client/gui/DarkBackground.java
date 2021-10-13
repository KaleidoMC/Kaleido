package snownee.kaleido.core.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent;
import net.minecraftforge.common.MinecraftForge;

@OnlyIn(Dist.CLIENT)
public class DarkBackground extends AbstractGui {

	public float alpha;
	public boolean closing;

	public void renderBackground(Screen screen, MatrixStack matrix, float pTicks) {
		alpha += closing ? -pTicks * .4f : pTicks * .2f;
		if (isClosed()) {
			return;
		}
		alpha = MathHelper.clamp(alpha, 0, 1);
		int textColor1 = (int)(alpha * 0xA0) << 24;
		int textColor2 = (int)(alpha * 0x90) << 24;
		int width = screen.width;
		int height = screen.height;
		fillGradient(matrix, 0, 0, width, (int) (height * 0.125), textColor1, textColor2);
		fillGradient(matrix, 0, (int) (height * 0.125), width, (int) (height * 0.875), textColor2, textColor2);
		fillGradient(matrix, 0, (int) (height * 0.875), width, height, textColor2, textColor1);
		MinecraftForge.EVENT_BUS.post(new BackgroundDrawnEvent(screen, matrix));
	}

	public boolean isClosed() {
		return closing && alpha <= 0;
	}

}
