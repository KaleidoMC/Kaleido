package snownee.kaleido.carpentry.client.gui;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FocusableGui;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import snownee.kaleido.carpentry.client.gui.MyList.MyEntry;

public class MyList<E extends MyEntry<E>> extends FocusableGui implements IRenderable {
	public abstract static class MyEntry<E extends MyEntry<E>> implements IGuiEventListener {
		@Deprecated
		protected MyList<E> list;
		protected int top;

		public abstract int getHeight();

		@Override
		public boolean isMouseOver(double p_isMouseOver_1_, double p_isMouseOver_3_) {
			return Objects.equals(this.list.getEntryAtPosition(p_isMouseOver_1_, p_isMouseOver_3_), this);
		}

		public abstract void render(MatrixStack matrix, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta);
	}

	class SimpleArrayList extends java.util.AbstractList<E> {
		private final List<E> field_216871_b = Lists.newArrayList();

		private SimpleArrayList() {
		}

		@Override
		public void add(int p_add_1_, E p_add_2_) {
			this.field_216871_b.add(p_add_1_, p_add_2_);
			p_add_2_.list = MyList.this;
		}

		@Override
		public E get(int p_get_1_) {
			return (this.field_216871_b.get(p_get_1_));
		}

		@Override
		public E remove(int p_remove_1_) {
			return (this.field_216871_b.remove(p_remove_1_));
		}

		@Override
		public E set(int p_set_1_, E p_set_2_) {
			E e = this.field_216871_b.set(p_set_1_, p_set_2_);
			p_set_2_.list = MyList.this;
			return e;
		}

		@Override
		public int size() {
			return this.field_216871_b.size();
		}
	}

	protected static final int DRAG_OUTSIDE = -2;
	protected boolean centerListVertically = true;
	private final List<E> children = new SimpleArrayList();
	protected int headerHeight;
	protected int height;
	private int maxItemPosition;
	protected final Minecraft minecraft;
	private float pressTicks;
	protected boolean renderHeader;
	protected boolean renderScrollbar = true;
	protected boolean renderSelection = true;
	private double scrollAmount;
	protected float scrollFactor = 10;
	private boolean scrolling;
	private E selected;
	protected int width;
	protected int x0;
	protected int x1;
	protected int y0;
	protected int y1;
	protected int yDrag = -2;

	public MyList(Minecraft mcIn, int widthIn, int heightIn, int topIn) {
		this.minecraft = mcIn;
		this.width = widthIn;
		this.height = heightIn;
		setTop(topIn);
		this.x0 = 0;
		this.x1 = widthIn;
	}

	public void setTop(float topIn) {
		int top = (int) topIn;
		this.y0 = top;
		this.y1 = top + height - 40;
	}

	protected int addEntry(E entry) {
		this.children.add(entry);
		entry.top = maxItemPosition;
		maxItemPosition += entry.getHeight();
		return this.children.size() - 1;
	}

	public void refreshHeight() {
		maxItemPosition = 0;
		for (E entry : children) {
			entry.top = maxItemPosition;
			maxItemPosition += entry.getHeight();
		}
	}

	protected void centerScrollOn(E entry) {
		this.setScrollAmount(this.children().indexOf(entry) * entry.getHeight() + entry.getHeight() / 2 - (this.y1 - this.y0) / 2);
	}

	protected final void clearEntries() {
		this.children.clear();
	}

	protected void clickedHeader(int p_clickedHeader_1_, int p_clickedHeader_2_) {
	}

	protected void ensureVisible(E entry) {
		int i = entry.top;
		int j = i - this.y0 - 4 - entry.getHeight();
		if (j < 0) {
			this.scroll(j);
		}

		int k = this.y1 - i - entry.getHeight() * 2;
		if (k < 0) {
			this.scroll(-k);
		}

	}

	public int getBottom() {
		return this.y1;
	}

	protected E getEntry(int index) {
		return this.children().get(index);
	}

	@Nullable
	protected final E getEntryAtPosition(double x, double y) {
		int i = this.getRowWidth() / 2;
		int j = this.x0 + this.width / 2;
		int k = j - i;
		int l = j + i;
		if (/*x < this.getScrollbarPosition() && */x >= k && x <= l) {
			int i1 = MathHelper.floor(y - this.y0) - this.headerHeight + (int) this.getScrollAmount() - getSpacer();
			if (i1 >= 0) {
				for (E e : children) {
					if (i1 > e.top && i1 < e.top + e.getHeight()) {
						return e;
					}
				}
			}
		}
		return null;
	}

	@Override
	@Nullable
	public E getFocused() {
		return (E) super.getFocused();
	}

	public int getHeight() {
		return this.height;
	}

	protected int getItemCount() {
		return this.children().size();
	}

	public int getLeft() {
		return this.x0;
	}

	protected int getMaxPosition() {
		return maxItemPosition + headerHeight;
	}

	private int getMaxScroll() {
		return Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - getSpacer()));
	}

	public int getRight() {
		return this.x1;
	}

	protected int getRowLeft() {
		return this.x0 + this.width / 2 - this.getRowWidth() / 2;
	}

	public int getRowWidth() {
		return width - 4;
	}

	public double getScrollAmount() {
		return this.scrollAmount;
	}

	protected int getScrollbarPosition() {
		return this.width / 2 + 124;
	}

	public int getScrollBottom() {
		return (int) this.getScrollAmount() - this.height - this.headerHeight;
	}

	@Nullable
	public E getSelected() {
		return this.selected;
	}

	public int getSpacer() {
		return 2;
	}

	public int getTop() {
		return this.y0;
	}

	public int getWidth() {
		return this.width;
	}

	protected boolean isFocused() {
		return false;
	}

	@Override
	public boolean isMouseOver(double p_isMouseOver_1_, double p_isMouseOver_3_) {
		return p_isMouseOver_3_ >= this.y0 && p_isMouseOver_3_ <= this.y1 && p_isMouseOver_1_ >= this.x0 && p_isMouseOver_1_ <= this.x1;
	}

	protected boolean isSelectedItem(int i) {
		return Objects.equals(this.getSelected(), this.children().get(i));
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (super.keyPressed(keyCode, scanCode, modifiers)) {
			return true;
		} else if (keyCode == 264) {
			this.moveSelection(AbstractList.Ordering.DOWN);
			return true;
		} else if (keyCode == 265) {
			this.moveSelection(AbstractList.Ordering.UP);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.updateScrollingState(mouseX, mouseY, button);
		if (!this.isMouseOver(mouseX, mouseY)) {
			return false;
		} else {
			E e = this.getEntryAtPosition(mouseX, mouseY);
			if (e != null) {
				pressTicks = 1;
				setFocused(e);
				setDragging(true);
				return true;
			} else if (button == 0) {
				this.clickedHeader((int) (mouseX - (this.x0 + this.width / 2 - this.getRowWidth() / 2)), (int) (mouseY - this.y0) + (int) this.getScrollAmount() - 4);
				return true;
			}

			return this.scrolling;
		}
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
			return true;
		} else if (button == 0 && this.scrolling) {
			double d0 = Math.max(1, this.getMaxScroll());
			double d1 = Math.max(1.0D, d0 / 25);
			this.setScrollAmount(this.getScrollAmount() + dragY * d1 * -0.2);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (this.getFocused() != null) {
			if (pressTicks > 0) {
				if (pressTicks < 4) {
					getFocused().mouseClicked(mouseX, mouseY, button);
				}
				pressTicks = 0;
			}
			this.getFocused().mouseReleased(mouseX, mouseY, button);
		}
		setDragging(false);
		return false;
	}

	@Override
	public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_) {
		this.setScrollAmount(this.getScrollAmount() - p_mouseScrolled_5_ * scrollFactor);
		return true;
	}

	protected void moveSelection(AbstractList.Ordering p_241219_1_) {
		this.func_241572_a_(p_241219_1_, p_241573_0_ -> true);
	}

	protected void func_241572_a_(AbstractList.Ordering p_241572_1_, Predicate<E> p_241572_2_) {
		int i = p_241572_1_ == AbstractList.Ordering.UP ? -1 : 1;
		if (!this.children().isEmpty()) {
			int j = this.children().indexOf(this.getSelected());

			while (true) {
				int k = MathHelper.clamp(j + i, 0, this.getItemCount() - 1);
				if (j == k) {
					break;
				}

				E e = this.children().get(k);
				if (p_241572_2_.test(e)) {
					this.setSelected(e);
					this.ensureVisible(e);
					break;
				}

				j = k;
			}
		}
	}

	protected E remove(int p_remove_1_) {
		E e = this.children.get(p_remove_1_);
		return this.removeEntry((this.children.get(p_remove_1_))) ? e : null;
	}

	protected boolean removeEntry(E p_removeEntry_1_) {
		boolean flag = this.children.remove(p_removeEntry_1_);
		if (flag && p_removeEntry_1_ == this.getSelected()) {
			this.setSelected((E) null);
		}

		return flag;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void render(MatrixStack matrix, int mouseX, int mouseY, float pTicks) {
		if (pressTicks > 0) {
			pressTicks += pTicks;
		}
		this.renderBackground(matrix);
		int i = this.getScrollbarPosition();
		int j = i + 6;
		double scale = minecraft.getWindow().getGuiScale();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuilder();
		matrix.pushPose();
		//matrix.translate(0, 10, 0);
		AbstractGui.fill(matrix, x0 - 2, 0, x1 + 2, y1 + 3, 0xAA000000);
		RenderSystem.enableScissor((int) (x0 * scale), (int) ((height - y1) * scale), (int) (width * scale), (int) ((y1 - y0) * scale));
		int k = this.getRowLeft();
		int l = this.y0 + getSpacer() - (int) this.getScrollAmount();
		if (this.renderHeader) {
			this.renderHeader(matrix, k, l, tessellator);
		}

		this.renderList(matrix, k, l, mouseX, mouseY, pTicks);
		RenderSystem.disableDepthTest();
		//this.renderHoleBackground(0, this.y0, 255, 255);
		//this.renderHoleBackground(this.y1, this.height, 255, 255);
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
		RenderSystem.disableAlphaTest();
		RenderSystem.shadeModel(7425);
		RenderSystem.disableTexture();
		int j1 = this.getMaxScroll();
		if (renderScrollbar && j1 > 0) {
			int k1 = (int) ((float) ((this.y1 - this.y0) * (this.y1 - this.y0)) / (float) this.getMaxPosition());
			k1 = MathHelper.clamp(k1, 32, this.y1 - this.y0 - 8);
			int l1 = (int) this.getScrollAmount() * (this.y1 - this.y0 - k1) / j1 + this.y0;
			if (l1 < this.y0) {
				l1 = this.y0;
			}

			bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
			bufferbuilder.vertex(i, this.y1, 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
			bufferbuilder.vertex(j, this.y1, 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
			bufferbuilder.vertex(j, this.y0, 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
			bufferbuilder.vertex(i, this.y0, 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
			tessellator.end();
			bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
			bufferbuilder.vertex(i, l1 + k1, 0.0D).uv(0.0F, 1.0F).color(128, 128, 128, 255).endVertex();
			bufferbuilder.vertex(j, l1 + k1, 0.0D).uv(1.0F, 1.0F).color(128, 128, 128, 255).endVertex();
			bufferbuilder.vertex(j, l1, 0.0D).uv(1.0F, 0.0F).color(128, 128, 128, 255).endVertex();
			bufferbuilder.vertex(i, l1, 0.0D).uv(0.0F, 0.0F).color(128, 128, 128, 255).endVertex();
			tessellator.end();
			bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
			bufferbuilder.vertex(i, l1 + k1 - 1, 0.0D).uv(0.0F, 1.0F).color(192, 192, 192, 255).endVertex();
			bufferbuilder.vertex(j - 1, l1 + k1 - 1, 0.0D).uv(1.0F, 1.0F).color(192, 192, 192, 255).endVertex();
			bufferbuilder.vertex(j - 1, l1, 0.0D).uv(1.0F, 0.0F).color(192, 192, 192, 255).endVertex();
			bufferbuilder.vertex(i, l1, 0.0D).uv(0.0F, 0.0F).color(192, 192, 192, 255).endVertex();
			tessellator.end();
		}

		this.renderDecorations(matrix, mouseX, mouseY);
		RenderSystem.enableTexture();
		RenderSystem.shadeModel(7424);
		RenderSystem.enableAlphaTest();
		RenderSystem.disableBlend();
		RenderSystem.disableScissor();
		matrix.popPose();
	}

	protected void renderBackground(MatrixStack matrix) {
	}

	protected void renderDecorations(MatrixStack matrix, int p_renderDecorations_1_, int p_renderDecorations_2_) {
	}

	protected void renderHeader(MatrixStack matrix, int p_renderHeader_1_, int p_renderHeader_2_, Tessellator p_renderHeader_3_) {
	}

	@SuppressWarnings("deprecation")
	protected void renderHoleBackground(int p_renderHoleBackground_1_, int p_renderHoleBackground_2_, int p_renderHoleBackground_3_, int p_renderHoleBackground_4_) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuilder();
		this.minecraft.getTextureManager().bind(AbstractGui.BACKGROUND_LOCATION);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		bufferbuilder.vertex(this.x0, p_renderHoleBackground_2_, 0.0D).uv(0.0F, p_renderHoleBackground_2_ / 32.0F).color(64, 64, 64, p_renderHoleBackground_4_).endVertex();
		bufferbuilder.vertex(this.x0 + this.width, p_renderHoleBackground_2_, 0.0D).uv(this.width / 32.0F, p_renderHoleBackground_2_ / 32.0F).color(64, 64, 64, p_renderHoleBackground_4_).endVertex();
		bufferbuilder.vertex(this.x0 + this.width, p_renderHoleBackground_1_, 0.0D).uv(this.width / 32.0F, p_renderHoleBackground_1_ / 32.0F).color(64, 64, 64, p_renderHoleBackground_3_).endVertex();
		bufferbuilder.vertex(this.x0, p_renderHoleBackground_1_, 0.0D).uv(0.0F, p_renderHoleBackground_1_ / 32.0F).color(64, 64, 64, p_renderHoleBackground_3_).endVertex();
		tessellator.end();
	}

	@SuppressWarnings("deprecation")
	protected void renderList(MatrixStack matrix, int x, int y, int mouseX, int mouseY, float pTicks) {
		int i = this.getItemCount();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuilder();

		int top = this.y0 + getSpacer() - (int) this.getScrollAmount() + this.headerHeight;
		for (int j = 0; j < i; ++j) {
			E entry = this.getEntry(j);
			int itemHeight = entry.getHeight();
			int bottom = top + itemHeight;
			if (bottom >= this.y0 && top <= this.y1) {
				int i1 = y + top;
				int realHeight = itemHeight - getSpacer();
				int rowWidth = this.getRowWidth();
				if (this.renderSelection && this.isSelectedItem(j)) {
					int l1 = this.x0 + this.width / 2 - rowWidth / 2;
					int i2 = this.x0 + this.width / 2 + rowWidth / 2;
					RenderSystem.disableTexture();
					float f = this.isFocused() ? 1.0F : 0.5F;
					RenderSystem.color4f(f, f, f, 1.0F);
					bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
					bufferbuilder.vertex(l1, i1 + realHeight + 2, 0.0D).endVertex();
					bufferbuilder.vertex(i2, i1 + realHeight + 2, 0.0D).endVertex();
					bufferbuilder.vertex(i2, i1 - 2, 0.0D).endVertex();
					bufferbuilder.vertex(l1, i1 - 2, 0.0D).endVertex();
					tessellator.end();
					RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
					bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
					bufferbuilder.vertex(l1 + 1, i1 + realHeight + 1, 0.0D).endVertex();
					bufferbuilder.vertex(i2 - 1, i1 + realHeight + 1, 0.0D).endVertex();
					bufferbuilder.vertex(i2 - 1, i1 - 1, 0.0D).endVertex();
					bufferbuilder.vertex(l1 + 1, i1 - 1, 0.0D).endVertex();
					tessellator.end();
					RenderSystem.enableTexture();
				}
				//System.out.println(mouseY + " " + i1);

				int j2 = this.getRowLeft();
				entry.render(matrix, j, top, j2, rowWidth, realHeight, mouseX, mouseY, !isDragging() && this.isMouseOver(mouseX, mouseY) && mouseY >= top && mouseY < bottom, pTicks);
			}
			top = bottom;
		}

	}

	protected void replaceEntries(Collection<E> p_replaceEntries_1_) {
		this.children.clear();
		this.children.addAll(p_replaceEntries_1_);
	}

	private void scroll(int p_scroll_1_) {
		this.setScrollAmount(this.getScrollAmount() + p_scroll_1_);
		this.yDrag = -2;
	}

	public void setLeftPos(int p_setLeftPos_1_) {
		this.x0 = p_setLeftPos_1_;
		this.x1 = p_setLeftPos_1_ + this.width;
	}

	protected void setRenderHeader(boolean p_setRenderHeader_1_, int p_setRenderHeader_2_) {
		this.renderHeader = p_setRenderHeader_1_;
		this.headerHeight = p_setRenderHeader_2_;
		if (!p_setRenderHeader_1_) {
			this.headerHeight = 0;
		}

	}

	public void setRenderSelection(boolean p_setRenderSelection_1_) {
		this.renderSelection = p_setRenderSelection_1_;
	}

	public void setScrollAmount(double p_setScrollAmount_1_) {
		this.scrollAmount = MathHelper.clamp(p_setScrollAmount_1_, 0.0D, this.getMaxScroll());
	}

	public void setSelected(@Nullable E p_setSelected_1_) {
		this.selected = p_setSelected_1_;
	}

	protected void updateScrollingState(double p_updateScrollingState_1_, double p_updateScrollingState_3_, int p_updateScrollingState_5_) {
		this.scrolling = true;
	}

	@Override
	public List<E> children() {
		return children;
	}

}
