package snownee.kaleido.scope.client.gui;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import com.mojang.blaze3d.matrix.MatrixStack;

import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kaleido.util.KaleidoUtil;

@OnlyIn(Dist.CLIENT)
public class AxisEditBox extends TextFieldWidget {

	private final BooleanSupplier snap;
	private final float step;
	private final Axis axis;
	public Supplier<String> getter;
	public FloatConsumer setter;
	private float scrolledValue;

	public AxisEditBox(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage, BooleanSupplier snap, float step, Axis axis) {
		super(Minecraft.getInstance().font, pX, pY, pWidth, pHeight, pMessage);
		this.snap = snap;
		this.step = step;
		this.axis = axis;
		active = false;
		setFilter(s -> s.matches("[0-9.+-]*"));
	}

	@Override
	protected void onDrag(double pMouseX, double pMouseY, double pDragX, double pDragY) {
		mouseScrolled(pMouseX, pMouseY, pDragX * 0.5F);
		super.setFocused(false);
	}

	public float getFloat() {
		try {
			return Float.valueOf(getValue());
		} catch (Throwable e) {
			return 0;
		}
	}

	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
		if (!active) {
			return visible && pMouseX >= x && pMouseY >= y && pMouseX < x + width && pMouseY < y + height;
		}
		return super.mouseClicked(pMouseX, pMouseY, pButton);
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
	public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
		if (!isVisible()) {
			return;
		}

		int bgColor = active ? 0x66000000 : 0x66555555;
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

		int textColor = isEditable() ? this.textColor : textColorUneditable;
		int j = getCursorPosition() - displayPos;
		int k = highlightPos - displayPos;
		String s = font.plainSubstrByWidth(getValue().substring(displayPos), getInnerWidth());
		boolean flag = j >= 0 && j <= s.length();
		boolean flag1 = isFocused() && frame / 6 % 2 == 0 && flag;
		int l = x + 4;
		int i1 = y + (height - 6) / 2;
		int j1 = l;
		if (k > s.length()) {
			k = s.length();
		}

		if (!s.isEmpty()) {
			String s1 = flag ? s.substring(0, j) : s;
			j1 = font.drawShadow(pMatrixStack, formatter.apply(s1, displayPos), l, i1, textColor);
		}

		boolean flag2 = getCursorPosition() < getValue().length() || getValue().length() >= 4;
		int k1 = j1;
		if (!flag) {
			k1 = j > 0 ? l + width : l;
		} else if (flag2) {
			k1 = j1 - 1;
			--j1;
		}

		if (!s.isEmpty() && flag && j < s.length()) {
			font.drawShadow(pMatrixStack, formatter.apply(s.substring(j), getCursorPosition()), j1, i1, textColor);
		}

		if (flag1) {
			if (flag2) {
				AbstractGui.fill(pMatrixStack, k1, i1 - 1, k1 + 1, i1 + 1 + 9, -3092272);
			} else {
				font.drawShadow(pMatrixStack, "_", k1, i1, textColor);
			}
		}

		if (k != j) {
			int l1 = l + font.width(s.substring(0, k));
			renderHighlight(k1, i1 - 1, l1 - 1, i1 + 1 + 9);
		}

	}

	@Override
	public void setFocus(boolean pIsFocused) {
		setFocused(pIsFocused);
	}

	@Override
	protected void setFocused(boolean pFocused) {
		super.setFocused(pFocused);
		onFocusedChanged(pFocused);
	}

	@Override
	protected void onFocusedChanged(boolean pFocused) {
		if (pFocused) {
			frame = 0;
		} else if (active && visible) {
			setter.accept(getFloat());
			setValue(getter.get());
		}
	}

	@Override
	public ITextComponent getMessage() {
		return new TranslationTextComponent("%s %s", super.getMessage(), axis);
	}

}
