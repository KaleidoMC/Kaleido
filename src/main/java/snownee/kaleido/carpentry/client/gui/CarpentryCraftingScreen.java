package snownee.kaleido.carpentry.client.gui;

import java.util.LinkedList;
import java.util.Random;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.INestedGuiEventHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent;
import net.minecraftforge.common.MinecraftForge;
import snownee.kaleido.Kaleido;
import snownee.kaleido.carpentry.CarpentryModule;
import snownee.kaleido.core.CoreModule;
import snownee.kaleido.core.KaleidoDataManager;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.ModelPack;
import snownee.kaleido.core.network.CRedeemPacket;
import snownee.kaleido.util.KaleidoUtil;

@OnlyIn(Dist.CLIENT)
public class CarpentryCraftingScreen extends Screen {

    private static class Entry extends MyList.MyEntry<Entry> implements INestedGuiEventHandler {

        private static final ResourceLocation GUI_BARS_TEXTURES = new ResourceLocation(Kaleido.MODID, "textures/gui/bars.png");
        private final java.util.List<StackButton> children = Lists.newArrayList();
        private int height;
        private final String name;
        private final CarpentryCraftingScreen parent;
        private boolean selected;
        private int size;

        private int unlocked;

        public Entry(CarpentryCraftingScreen parent, ModelPack pack) {
            this.parent = parent;
            this.name = I18n.format(pack.translationKey);

            LinkedList<ModelInfo> allInfos = Lists.newLinkedList(pack.normalInfos);
            allInfos.addAll(pack.rewardInfos);
            int i = 0;
            for (ModelInfo info : allInfos) {
                StackButton button = new StackButton(5 + i % 8 * 28, 17 + i / 8 * 28, info, info.makeItem(), btn -> {
                    parent.setSelectedButton((StackButton) btn);
                }, (btn, matrix, mouseX, mouseY) -> {}); // TODO
                children.add(button);
                ++i;
            }

            size = pack.normalInfos.size();
            unlocked = (int) pack.normalInfos.stream().filter($ -> !$.isLocked()).count();
            height = (children.size() - 1) / 8 * 28 + 50;
        }

        @Override
        public java.util.List<? extends IGuiEventListener> getEventListeners() {
            return children;
        }

        @Override
        public IGuiEventListener getListener() {
            return null;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public boolean isDragging() {
            return false;
        }

        @Override
        public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
            selected = !selected;
            for (IGuiEventListener iguieventlistener : this.getEventListeners()) {
                if (iguieventlistener.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_)) {
                    this.setListener(iguieventlistener);
                    if (p_mouseClicked_5_ == 0) {
                        this.setDragging(true);
                    }

                    return true;
                }
            }
            return true;
        }

        @Override
        public void render(MatrixStack matrix, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hover, float partialTicks) {
            AbstractGui.fill(matrix, left, top, left + entryWidth, top + entryHeight, 0x22FFFFFF);
            parent.font.drawString(matrix, name, left + 8, top + 4, 0xFFFFFF);
            children.forEach(btn -> {
                btn.x = btn.originalX + left;
                btn.y = btn.originalY + top;
                btn.render(matrix, mouseX, mouseY, partialTicks);
            });

            matrix.push();
            matrix.scale(0.75f, 0.75f, 0.75f);
            parent.drawCenteredString(matrix, parent.font, unlocked + "/" + size, (int) ((left + entryWidth - 18) * 1.33), (int) ((top + 5.5) * 1.33), 0xFFFFFF);
            matrix.pop();
            parent.minecraft.getTextureManager().bindTexture(GUI_BARS_TEXTURES);
            AbstractGui.blit(matrix, left + entryWidth - 66, top + 5, parent.getBlitOffset(), 0, 0, 32, 5, 32, 32);
            if (unlocked > 0) {
                AbstractGui.blit(matrix, left + entryWidth - 66, top + 5, parent.getBlitOffset(), 0, 5, unlocked * 32 / size, 10, 32, 32);
            }
        }

        @Override
        public void setDragging(boolean p_setDragging_1_) {}

        @Override
        public void setListener(IGuiEventListener p_setFocused_1_) {}

    }

    private static class List extends MyList<Entry> {

        public List(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn) {
            super(mcIn, widthIn, heightIn, topIn, bottomIn);
            renderScrollbar = false;
            scrollFactor = 25;
        }

    }

    private static final Random RANDOM = new Random();
    private Button addBtn;
    private float alpha;
    private boolean closing;
    private int coins;
    private ItemStack coinStack;
    private Button confirmBtn;
    private int cooldown;
    private MyList list;
    private final BlockPos pos;
    private StackButton selectedButton;
    private Button shrinkBtn;
    private TextFieldWidget textField;
    private int timer;

    private java.util.List<ITextProperties> tip;

    private final World world;

    public CarpentryCraftingScreen(ITextComponent title, World world, BlockPos pos) {
        super(title);
        this.world = world;
        this.pos = pos;
        coinStack = new ItemStack(CoreModule.CLOTH_TAG.getRandomElement(RANDOM));
    }

    private void addNum(int n) {
        if (selectedButton == null) {
            return;
        }
        int cur = getRedeemAmount();
        if (!minecraft.player.isCreative()) {
            cur = MathHelper.clamp(cur + n, 1, coins / selectedButton.info.price);
        } else {
            cur = Math.max(cur + n, 1);
        }
        textField.setText(Integer.toString(cur));
    }

    public int getRedeemAmount() {
        if (selectedButton == null) {
            return 0;
        }
        try {
            return Integer.parseInt(textField.getText());
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    protected void init() {
        if (!isUsable()) {
            return;
        }
        tip = font.getCharacterManager().func_238365_g_(I18n.format("tip.kaleido.unlock"), 120, Style.EMPTY);
        children.add(list = new List(minecraft, 238, height, 20, height - 20));
        list.setLeftPos(30);
        for (ModelPack pack : KaleidoDataManager.INSTANCE.allPacks.values()) {
            list.addEntry(new Entry(this, pack));
        }

        int x = minecraft.getMainWindow().getScaledWidth() / 2 + 125;
        int y = minecraft.getMainWindow().getScaledHeight() / 2 + 50;
        addButton(textField = new TextFieldWidget(font, x - 19, y + 1, 38, 18, new StringTextComponent("")));
        //        textField.setResponder(str -> {
        //            if (selectedButton == null) {
        //                return;
        //            }
        //            int n = 1;
        //            try {
        //                n = Integer.parseInt(str);
        //            } catch (Exception e) {}
        //            if (n < 1) {
        //                n = 1;
        //            } else if (n > selectedButton.stack.getMaxStackSize()) {
        //                n = selectedButton.stack.getMaxStackSize();
        //            }
        //            textField.setText(Integer.toString(lastNumber));
        //        });
        textField.setValidator(str -> {
            if (str.isEmpty()) {
                return true;
            }
            if (selectedButton == null) {
                return false;
            }
            int n = 1;
            try {
                n = Integer.parseInt(str);
            } catch (Exception e) {
                return false;
            }
            if (n < 1 /*|| n > selectedButton.stack.getMaxStackSize()*/) {
                return false;
            }
            return true;
        });
        addButton(confirmBtn = new Button(x + 40, y, 20, 20, new StringTextComponent("✓"), btn -> {
            if (cooldown > 0) {
                return;
            }
            if (selectedButton != null) {
                new CRedeemPacket(selectedButton.info, getRedeemAmount()).send();
                timer = 17; // defer update coins
                cooldown = 8;
            }
        }));
        addButton(addBtn = new Button(x + 20, y, 20, 20, new StringTextComponent("+"), btn -> {
            addNum(1);
        }));
        addButton(shrinkBtn = new Button(x - 40, y, 20, 20, new StringTextComponent("-"), btn -> {
            addNum(-1);
        }));
        if (selectedButton == null) {
            setSelectedButton(null);
        }
        update();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public boolean isUsable() {
        if (world == null || pos == null) {
            return true;
        }
        BlockState state = world.getBlockState(pos);
        return state.getBlock() == CarpentryModule.WOODWORKING_BENCH;
    }

    @Override
    public void onClose() {
        closing = true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float pTicks) {
        alpha += closing ? -pTicks * .4f : pTicks * .2f;
        if (closing && alpha <= 0) {
            Minecraft.getInstance().displayGuiScreen(null);
            list = null;
            return;
        }
        alpha = MathHelper.clamp(alpha, 0, 1);

        this.renderBackground(matrix);
        list.render(matrix, mouseX, mouseY, pTicks);
        if (alpha < 0.5f) {
            return;
        }
        for (int i = 0; i < this.buttons.size(); ++i) {
            Widget widget = this.buttons.get(i);
            widget.render(matrix, mouseX, mouseY, pTicks);
        }
        int x = minecraft.getMainWindow().getScaledWidth() / 2 + 85;
        int y = minecraft.getMainWindow().getScaledHeight() / 2 - 65;
        if (selectedButton != null) {
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            RenderSystem.pushMatrix();
            RenderSystem.translatef(x, y, 0);
            RenderSystem.scalef(5F, 5F, 5F);
            RenderSystem.enableBlend();
            RenderSystem.color4f(1, 1, 1, 0.1f);
            StackButton.renderItemIntoGUI(itemRenderer, selectedButton.stack, 0, 0, selectedButton.info.isLocked() ? 0 : 15728880);
            RenderSystem.popMatrix();
            x += 40;
            y += 40 - 62;
            drawCenteredString(matrix, font, selectedButton.stack.getDisplayName().getString(), x, y, 0xFFFFFFFF);
            if (!selectedButton.info.isLocked()) {
                itemRenderer.renderItemAndEffectIntoGUI(coinStack, x + 45, y + 95);
                int amount = getRedeemAmount();
                if (amount < 1) {
                    amount = 1;
                }
                itemRenderer.renderItemOverlayIntoGUI(font, coinStack, x + 50, y + 95, Integer.toString(amount * selectedButton.info.price));
            }
        } else {
            x += 40;
            y += 40;
            for (ITextProperties s : tip) {
                drawCenteredString(matrix, font, s.getString(), x, y, 0xFFFFFFFF);
                y += 15;
            }
        }

        itemRenderer.renderItemAndEffectIntoGUI(coinStack, width - 20, 5);
        String numText = Integer.toString(coins);
        font.drawStringWithShadow(matrix, numText, width - 25 - font.getStringWidth(numText), 9, 0xFFFFFFFF);
    }

    @Override
    public void renderBackground(MatrixStack matrix, int p_renderBackground_1_) {
        int textColor1 = (int) (alpha * 0xA0) << 24;
        int textColor2 = (int) (alpha * 0x70) << 24;
        this.fillGradient(matrix, 0, 0, width, (int) (height * 0.125), textColor1, textColor2);
        this.fillGradient(matrix, 0, (int) (height * 0.125), width, (int) (height * 0.875), textColor2, textColor2);
        this.fillGradient(matrix, 0, (int) (height * 0.875), width, height, textColor2, textColor1);
        MinecraftForge.EVENT_BUS.post(new BackgroundDrawnEvent(this, matrix));
    }

    public void setSelectedButton(StackButton selectedButton) {
        this.selectedButton = selectedButton;
        if (selectedButton == null) {
            confirmBtn.visible = false;
            addBtn.visible = false;
            shrinkBtn.visible = false;
            confirmBtn.visible = false;
            textField.visible = false;
        } else {
            if (textField.getText().isEmpty()) {
                textField.setText("1");
            }
            confirmBtn.visible = true;
            addBtn.visible = true;
            shrinkBtn.visible = true;
            confirmBtn.visible = true;
            textField.visible = true;
            boolean active = !selectedButton.info.isLocked();
            confirmBtn.active = active;
            addBtn.active = active;
            shrinkBtn.active = active;
            confirmBtn.active = active;
            textField.active = active;
        }
    }

    @Override
    public void tick() {
        --cooldown;
        if (!closing && ++timer == 20) {
            timer = 0;
            update();
        }
    }

    public void update() {
        if (!isUsable()) {
            onClose();
        } else {
            coins = KaleidoUtil.getCoins(minecraft.player);
        }
    }

}
