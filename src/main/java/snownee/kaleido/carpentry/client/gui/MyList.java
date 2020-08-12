package snownee.kaleido.carpentry.client.gui;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FocusableGui;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;
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

        public abstract void render(int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta);
    }
    class SimpleArrayList extends java.util.AbstractList<E> {
        private final List<E> field_216871_b = Lists.newArrayList();

        private SimpleArrayList() {}

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

    public MyList(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn) {
        this.minecraft = mcIn;
        this.width = widthIn;
        this.height = heightIn;
        this.y0 = topIn;
        this.y1 = bottomIn;
        this.x0 = 0;
        this.x1 = widthIn;
    }

    protected int addEntry(E entry) {
        this.children.add(entry);
        entry.top = maxItemPosition;
        maxItemPosition += entry.getHeight();
        return this.children.size() - 1;
    }

    protected void centerScrollOn(E entry) {
        this.setScrollAmount(this.children().indexOf(entry) * entry.getHeight() + entry.getHeight() / 2 - (this.y1 - this.y0) / 2);
    }

    @Override
    public final List<E> children() {
        return this.children;
    }

    protected final void clearEntries() {
        this.children.clear();
    }

    protected void clickedHeader(int p_clickedHeader_1_, int p_clickedHeader_2_) {}

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

    protected E getEntry(int entry) {
        return (this.children().get(entry));
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
        return (E) (super.getFocused());
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
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_)) {
            return true;
        } else if (p_keyPressed_1_ == 264) {
            this.moveSelection(1);
            return true;
        } else if (p_keyPressed_1_ == 265) {
            this.moveSelection(-1);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
        this.updateScrollingState(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        if (!this.isMouseOver(p_mouseClicked_1_, p_mouseClicked_3_)) {
            return false;
        } else {
            E e = this.getEntryAtPosition(p_mouseClicked_1_, p_mouseClicked_3_);
            if (e != null) {
                pressTicks = 1;
                this.setFocused(e);
                this.setDragging(true);
                return true;
            } else if (p_mouseClicked_5_ == 0) {
                this.clickedHeader((int) (p_mouseClicked_1_ - (this.x0 + this.width / 2 - this.getRowWidth() / 2)), (int) (p_mouseClicked_3_ - this.y0) + (int) this.getScrollAmount() - 4);
                return true;
            }

            return this.scrolling;
        }
    }

    @Override
    public boolean mouseDragged(double p_mouseDragged_1_, double p_mouseDragged_3_, int p_mouseDragged_5_, double p_mouseDragged_6_, double p_mouseDragged_8_) {
        if (super.mouseDragged(p_mouseDragged_1_, p_mouseDragged_3_, p_mouseDragged_5_, p_mouseDragged_6_, p_mouseDragged_8_)) {
            return true;
        } else if (p_mouseDragged_5_ == 0 && this.scrolling) {
            double d0 = Math.max(1, this.getMaxScroll());
            int i = this.y1 - this.y0;
            int j = MathHelper.clamp((int) ((float) (i * i) / (float) this.getMaxPosition()), 32, i - 8);
            double d1 = d0 / 25;
            this.setScrollAmount(this.getScrollAmount() + p_mouseDragged_8_ * d1 * -0.2);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int pTicks) {
        if (this.getFocused() != null) {
            if (pressTicks > 0) {
                if (pressTicks < 4) {
                    getFocused().mouseClicked(mouseX, mouseY, pTicks);
                }
                pressTicks = 0;
            }
            this.getFocused().mouseReleased(mouseX, mouseY, pTicks);
        }
        setDragging(false);
        return false;
    }

    @Override
    public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_) {
        this.setScrollAmount(this.getScrollAmount() - p_mouseScrolled_5_ * scrollFactor);
        return true;
    }

    protected void moveSelection(int p_moveSelection_1_) {
        if (!this.children().isEmpty()) {
            int i = this.children().indexOf(this.getSelected());
            int j = MathHelper.clamp(i + p_moveSelection_1_, 0, this.getItemCount() - 1);
            E e = this.children().get(j);
            this.setSelected(e);
            this.ensureVisible(e);
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

    @Override
    public void render(int mouseX, int mouseY, float pTicks) {
        if (pressTicks > 0) {
            if (isMouseOver(mouseX, mouseY)) {
                pressTicks += pTicks;
            } else {
                pressTicks = 0;
                setDragging(false);
            }
        }
        this.renderBackground();
        int i = this.getScrollbarPosition();
        int j = i + 6;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        this.minecraft.getTextureManager().bindTexture(AbstractGui.BACKGROUND_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos(this.x0, this.y1, 0.0D).tex(this.x0 / 32.0F, (this.y1 + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
        bufferbuilder.pos(this.x1, this.y1, 0.0D).tex(this.x1 / 32.0F, (this.y1 + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
        bufferbuilder.pos(this.x1, this.y0, 0.0D).tex(this.x1 / 32.0F, (this.y0 + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
        bufferbuilder.pos(this.x0, this.y0, 0.0D).tex(this.x0 / 32.0F, (this.y0 + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
        tessellator.draw();
        int k = this.getRowLeft();
        int l = this.y0 + getSpacer() - (int) this.getScrollAmount();
        if (this.renderHeader) {
            this.renderHeader(k, l, tessellator);
        }

        this.renderList(k, l, mouseX, mouseY, pTicks);
        RenderSystem.disableDepthTest();
        this.renderHoleBackground(0, this.y0, 255, 255);
        this.renderHoleBackground(this.y1, this.height, 255, 255);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        RenderSystem.disableAlphaTest();
        RenderSystem.shadeModel(7425);
        RenderSystem.disableTexture();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos(this.x0, this.y0 + 4, 0.0D).tex(0.0F, 1.0F).color(0, 0, 0, 0).endVertex();
        bufferbuilder.pos(this.x1, this.y0 + 4, 0.0D).tex(1.0F, 1.0F).color(0, 0, 0, 0).endVertex();
        bufferbuilder.pos(this.x1, this.y0, 0.0D).tex(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
        bufferbuilder.pos(this.x0, this.y0, 0.0D).tex(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
        tessellator.draw();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos(this.x0, this.y1, 0.0D).tex(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
        bufferbuilder.pos(this.x1, this.y1, 0.0D).tex(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
        bufferbuilder.pos(this.x1, this.y1 - 4, 0.0D).tex(1.0F, 0.0F).color(0, 0, 0, 0).endVertex();
        bufferbuilder.pos(this.x0, this.y1 - 4, 0.0D).tex(0.0F, 0.0F).color(0, 0, 0, 0).endVertex();
        tessellator.draw();
        int j1 = this.getMaxScroll();
        if (renderScrollbar && j1 > 0) {
            int k1 = (int) ((float) ((this.y1 - this.y0) * (this.y1 - this.y0)) / (float) this.getMaxPosition());
            k1 = MathHelper.clamp(k1, 32, this.y1 - this.y0 - 8);
            int l1 = (int) this.getScrollAmount() * (this.y1 - this.y0 - k1) / j1 + this.y0;
            if (l1 < this.y0) {
                l1 = this.y0;
            }

            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            bufferbuilder.pos(i, this.y1, 0.0D).tex(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(j, this.y1, 0.0D).tex(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(j, this.y0, 0.0D).tex(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(i, this.y0, 0.0D).tex(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
            tessellator.draw();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            bufferbuilder.pos(i, l1 + k1, 0.0D).tex(0.0F, 1.0F).color(128, 128, 128, 255).endVertex();
            bufferbuilder.pos(j, l1 + k1, 0.0D).tex(1.0F, 1.0F).color(128, 128, 128, 255).endVertex();
            bufferbuilder.pos(j, l1, 0.0D).tex(1.0F, 0.0F).color(128, 128, 128, 255).endVertex();
            bufferbuilder.pos(i, l1, 0.0D).tex(0.0F, 0.0F).color(128, 128, 128, 255).endVertex();
            tessellator.draw();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            bufferbuilder.pos(i, l1 + k1 - 1, 0.0D).tex(0.0F, 1.0F).color(192, 192, 192, 255).endVertex();
            bufferbuilder.pos(j - 1, l1 + k1 - 1, 0.0D).tex(1.0F, 1.0F).color(192, 192, 192, 255).endVertex();
            bufferbuilder.pos(j - 1, l1, 0.0D).tex(1.0F, 0.0F).color(192, 192, 192, 255).endVertex();
            bufferbuilder.pos(i, l1, 0.0D).tex(0.0F, 0.0F).color(192, 192, 192, 255).endVertex();
            tessellator.draw();
        }

        this.renderDecorations(mouseX, mouseY);
        RenderSystem.enableTexture();
        RenderSystem.shadeModel(7424);
        RenderSystem.enableAlphaTest();
        RenderSystem.disableBlend();
    }

    protected void renderBackground() {}

    protected void renderDecorations(int p_renderDecorations_1_, int p_renderDecorations_2_) {}

    protected void renderHeader(int p_renderHeader_1_, int p_renderHeader_2_, Tessellator p_renderHeader_3_) {}

    protected void renderHoleBackground(int p_renderHoleBackground_1_, int p_renderHoleBackground_2_, int p_renderHoleBackground_3_, int p_renderHoleBackground_4_) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        this.minecraft.getTextureManager().bindTexture(AbstractGui.BACKGROUND_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos(this.x0, p_renderHoleBackground_2_, 0.0D).tex(0.0F, p_renderHoleBackground_2_ / 32.0F).color(64, 64, 64, p_renderHoleBackground_4_).endVertex();
        bufferbuilder.pos(this.x0 + this.width, p_renderHoleBackground_2_, 0.0D).tex(this.width / 32.0F, p_renderHoleBackground_2_ / 32.0F).color(64, 64, 64, p_renderHoleBackground_4_).endVertex();
        bufferbuilder.pos(this.x0 + this.width, p_renderHoleBackground_1_, 0.0D).tex(this.width / 32.0F, p_renderHoleBackground_1_ / 32.0F).color(64, 64, 64, p_renderHoleBackground_3_).endVertex();
        bufferbuilder.pos(this.x0, p_renderHoleBackground_1_, 0.0D).tex(0.0F, p_renderHoleBackground_1_ / 32.0F).color(64, 64, 64, p_renderHoleBackground_3_).endVertex();
        tessellator.draw();
    }

    protected void renderList(int x, int y, int mouseX, int mouseY, float pTicks) {
        int i = this.getItemCount();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

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
                    bufferbuilder.pos(l1, i1 + realHeight + 2, 0.0D).endVertex();
                    bufferbuilder.pos(i2, i1 + realHeight + 2, 0.0D).endVertex();
                    bufferbuilder.pos(i2, i1 - 2, 0.0D).endVertex();
                    bufferbuilder.pos(l1, i1 - 2, 0.0D).endVertex();
                    tessellator.draw();
                    RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
                    bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
                    bufferbuilder.pos(l1 + 1, i1 + realHeight + 1, 0.0D).endVertex();
                    bufferbuilder.pos(i2 - 1, i1 + realHeight + 1, 0.0D).endVertex();
                    bufferbuilder.pos(i2 - 1, i1 - 1, 0.0D).endVertex();
                    bufferbuilder.pos(l1 + 1, i1 - 1, 0.0D).endVertex();
                    tessellator.draw();
                    RenderSystem.enableTexture();
                }
                //System.out.println(mouseY + " " + i1);

                int j2 = this.getRowLeft();
                entry.render(j, top, j2, rowWidth, realHeight, mouseX, mouseY, !isDragging() && this.isMouseOver(mouseX, mouseY) && mouseY >= top && mouseY < bottom, pTicks);
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

    public void updateSize(int p_updateSize_1_, int p_updateSize_2_, int p_updateSize_3_, int p_updateSize_4_) {
        this.width = p_updateSize_1_;
        this.height = p_updateSize_2_;
        this.y0 = p_updateSize_3_;
        this.y1 = p_updateSize_4_;
        this.x0 = 0;
        this.x1 = p_updateSize_1_;
    }
}
