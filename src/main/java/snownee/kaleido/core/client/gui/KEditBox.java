package snownee.kaleido.core.client.gui;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.mojang.blaze3d.matrix.MatrixStack;

import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kaleido.util.KaleidoUtil;

@OnlyIn(Dist.CLIENT)
public class KEditBox extends TextFieldWidget {

	public Supplier<String> getter;
	public FloatConsumer setter;
	private ContentType contentType = ContentType.Text;

	public KEditBox(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage) {
		super(Minecraft.getInstance().font, pX, pY, pWidth, pHeight, pMessage);
	}

	public float getFloat() {
		try {
			return Float.valueOf(getValue());
		} catch (Throwable e) {
			return 0;
		}
	}

	public int getInt() {
		try {
			return Integer.valueOf(getValue());
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
		if (!active || !contentType.isNumeric()) {
			return false;
		}
		float scrolledValue = (float) pDelta * getStep();
		if (scrolledValue != 0) {
			if (getter != null && setter != null) {
				setter.accept(getFloat() + scrolledValue);
				setValue(getter.get());
			} else {
				setValue(String.valueOf((int) (getFloat() + scrolledValue))); //FIXME
			}
		}
		return true;
	}

	public float getStep() {
		return 1;
	}

	@Override
	public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
		if (!isVisible()) {
			return;
		}

		renderBg(pMatrixStack, Minecraft.getInstance(), pMouseX, pMouseY);

		int textColor = isEditable() ? this.textColor : textColorUneditable;
		textColor = KaleidoUtil.applyAlpha(textColor, alpha);
		if (textColor == 0)
			return;
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
	protected void renderBg(MatrixStack pMatrixStack, Minecraft pMinecraft, int pMouseX, int pMouseY) {
		int bgColor = active ? 0x66000000 : 0x66555555;
		float alpha = isFocused() ? this.alpha : this.alpha * 0.6F;
		fill(pMatrixStack, x, y, x + width, y + height, KaleidoUtil.applyAlpha(bgColor, alpha));
		if (active) {
			int borderColor = 0xDDDDDD;
			borderColor = KaleidoUtil.applyAlpha(borderColor, alpha);
			fill(pMatrixStack, x - 1, y, x, y + height, borderColor);
			fill(pMatrixStack, x + width, y, x + width + 1, y + height, borderColor);
			fill(pMatrixStack, x - 1, y - 1, x + width + 1, y, borderColor);
			fill(pMatrixStack, x - 1, y + height, x + width + 1, y + height + 1, borderColor);
		}
	}

	@Override
	public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
		if (!canConsumeInput()) {
			return false;
		}
		if (pKeyCode == 257) {
			setFocus(false);
			return true;
		}
		return super.keyPressed(pKeyCode, pScanCode, pModifiers);
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
			if (getter != null && setter != null) {
				setter.accept(getFloat());
				setValue(getter.get());
			}
		}
	}

	public ContentType getContentType() {
		return contentType;
	}

	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
		setFilter(contentType.filter);
	}

	public enum ContentType {
		Text(Objects::nonNull),
		Number(s -> s != null && s.matches("[0-9.+-]*")),
		Int(s -> s != null && s.matches("[0-9+-]*"));

		public final Predicate<String> filter;

		ContentType(Predicate<String> filter) {
			this.filter = filter;
		}

		public boolean isNumeric() {
			return this == Number || this == Int;
		}
	}

}
