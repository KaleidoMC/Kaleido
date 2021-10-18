package snownee.kaleido.scope.client.gui;

import java.util.function.BooleanSupplier;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kaleido.core.client.cursor.Cursor;
import snownee.kaleido.core.client.cursor.CursorChanger;
import snownee.kaleido.core.client.cursor.StandardCursor;
import snownee.kaleido.core.client.gui.KEditBox;
import snownee.kaleido.util.KaleidoUtil;

@OnlyIn(Dist.CLIENT)
public class AxisEditBox extends KEditBox implements CursorChanger {

	private final BooleanSupplier snap;
	private final float step;
	private final Axis axis;
	private float scrolledValue;

	public AxisEditBox(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage, BooleanSupplier snap, float step, Axis axis) {
		super(pX, pY, pWidth, pHeight, pMessage);
		this.snap = snap;
		this.step = step;
		this.axis = axis;
		active = false;
		setContentType(ContentType.Number);
	}

	@Override
	protected void onDrag(double pMouseX, double pMouseY, double pDragX, double pDragY) {
		if (!active)
			return;
		mouseScrolled(pMouseX, pMouseY, pDragX * 0.5F);
		StandardCursor.H_RESIZE.use();
		super.setFocused(false);
	}

	@Override
	public float getStep() {
		return step;
	}

	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
		if (!active) {
			return false;
		}
		scrolledValue += pDelta * step;
		float f = getFloat();
		float f1 = f + scrolledValue;
		if (snap.getAsBoolean())
			f1 = Math.round(f1 / step) * step;
		if (f != f1) {
			scrolledValue = 0;
			setter.accept(f1);
			setValue(getter.get());
		}
		return true;
	}

	@Override
	protected void renderBg(MatrixStack pMatrixStack, Minecraft pMinecraft, int pMouseX, int pMouseY) {
		int bgColor = active ? 0x66000000 : 0x66555555;
		RenderSystem.enableDepthTest();
		float alpha = isFocused() ? this.alpha : this.alpha * 0.6F;
		fill(pMatrixStack, x, y, x + width, y + height, KaleidoUtil.applyAlpha(bgColor, alpha));
		if (active) {
			int borderColor;
			if (axis == Axis.X) {
				borderColor = 0xFF1242;
			} else if (axis == Axis.Y) {
				borderColor = 0x23D400;
			} else {
				borderColor = 0x0894ED;
			}
			fill(pMatrixStack, x - 1, y, x, y + height, KaleidoUtil.applyAlpha(borderColor, alpha));
		}
	}

	@Override
	public ITextComponent getMessage() {
		return new TranslationTextComponent("%s %s", super.getMessage(), axis);
	}

	@Override
	public Cursor getCursor(int pMouseX, int pMouseY, float pPartialTicks) {
		return visible && active && !isFocused() ? StandardCursor.H_RESIZE : StandardCursor.ARROW;
	}

}
