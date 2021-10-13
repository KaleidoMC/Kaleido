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

@OnlyIn(Dist.CLIENT)
public class AxisTextField extends TextFieldWidget {

	private final BooleanSupplier snap;
	private final float step;
	private final Axis axis;
	public Supplier<String> getter;
	public FloatConsumer setter;
	private float scrolledValue;

	public AxisTextField(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage, BooleanSupplier snap, float step, Axis axis) {
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
		return Float.valueOf(getValue());
	}

	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
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
		if (!this.isVisible()) {
			return;
		}
		// draw border
		int i = this.isFocused() ? -1 : -6250336;
		fill(pMatrixStack, this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, i);
		fill(pMatrixStack, this.x, this.y, this.x + this.width, this.y + this.height, -16777216);

		int textColor = this.isEditable() ? this.textColor : this.textColorUneditable;
		int j = this.getCursorPosition() - this.displayPos;
		int k = this.highlightPos - this.displayPos;
		String s = this.font.plainSubstrByWidth(this.getValue().substring(this.displayPos), this.getInnerWidth());
		boolean flag = j >= 0 && j <= s.length();
		boolean flag1 = this.isFocused() && this.frame / 6 % 2 == 0 && flag;
		int l = this.x + 4;
		int i1 = this.y + (this.height - 8) / 2;
		int j1 = l;
		if (k > s.length()) {
			k = s.length();
		}

		if (!s.isEmpty()) {
			String s1 = flag ? s.substring(0, j) : s;
			j1 = font.drawShadow(pMatrixStack, this.formatter.apply(s1, this.displayPos), (float) l, (float) i1, textColor);
		}

		boolean flag2 = this.getCursorPosition() < this.getValue().length() || this.getValue().length() >= 4;
		int k1 = j1;
		if (!flag) {
			k1 = j > 0 ? l + this.width : l;
		} else if (flag2) {
			k1 = j1 - 1;
			--j1;
		}

		if (!s.isEmpty() && flag && j < s.length()) {
			font.drawShadow(pMatrixStack, this.formatter.apply(s.substring(j), this.getCursorPosition()), (float) j1, (float) i1, textColor);
		}

		if (flag1) {
			if (flag2) {
				AbstractGui.fill(pMatrixStack, k1, i1 - 1, k1 + 1, i1 + 1 + 9, -3092272);
			} else {
				font.drawShadow(pMatrixStack, "_", (float) k1, (float) i1, textColor);
			}
		}

		if (k != j) {
			int l1 = l + font.width(s.substring(0, k));
			this.renderHighlight(k1, i1 - 1, l1 - 1, i1 + 1 + 9);
		}

	}
	
	@Override
	protected void setFocused(boolean pFocused) {
		super.setFocused(pFocused);
		if (!pFocused) {
			setter.accept(getFloat());
			setValue(getter.get());
		}
	}

	@Override
	public ITextComponent getMessage() {
		return new TranslationTextComponent("%s %s", super.getMessage(), axis);
	}

}
