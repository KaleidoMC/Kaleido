package snownee.kaleido.carpentry.client.gui;

import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.INestedGuiEventHandler;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button.IPressable;
import net.minecraft.client.gui.widget.button.Button.ITooltip;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CHeldItemChangePacket;
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
import snownee.kaleido.Kaleido;
import snownee.kaleido.KaleidoCommonConfig;
import snownee.kaleido.carpentry.CarpentryModule;
import snownee.kaleido.carpentry.network.CRedeemPacket;
import snownee.kaleido.core.CoreModule;
import snownee.kaleido.core.KaleidoDataManager;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.ModelPack;
import snownee.kaleido.core.client.gui.DarkBackground;
import snownee.kaleido.core.client.gui.KButton;
import snownee.kaleido.core.client.gui.KEditBox;
import snownee.kaleido.core.client.gui.KEditBox.ContentType;
import snownee.kaleido.core.client.gui.ResizeableScreen;
import snownee.kaleido.util.KaleidoUtil;

@OnlyIn(Dist.CLIENT)
public class CarpentryCraftingScreen extends ResizeableScreen {

	static class Entry extends MyList.MyEntry<Entry> implements INestedGuiEventHandler {

		private static final ResourceLocation GUI_BARS_TEXTURES = new ResourceLocation(Kaleido.MODID, "textures/gui/bars.png");
		private final java.util.List<StackButton> children = Lists.newArrayList();
		private int height;
		private final String name;
		private final CarpentryCraftingScreen parent;
		private int size;
		private int unlocked;
		private float progress;
		private boolean fold;
		public final String id;

		public Entry(CarpentryCraftingScreen parent, ModelPack pack, boolean fold) {
			this.parent = parent;
			this.fold = fold;
			id = pack.id;
			name = I18n.get(pack.descriptionId);

			java.util.List<ModelInfo> allInfos = Streams.concat(pack.normalInfos.stream(), pack.rewardInfos.stream()).filter($ -> !$.hide).collect(Collectors.toList());
			int i = 0;
			IPressable pressable = btn -> {
				parent.setSelectedButton((StackButton) btn);
			};
			ITooltip tooltip = (btn, matrix, mouseX, mouseY) -> {
				parent.renderTooltip(matrix, btn.getMessage(), mouseX, mouseY);
			};
			for (ModelInfo info : allInfos) {
				StackButton button = new StackButton(parent.list, 5 + i % 8 * 28, 17 + i / 8 * 28, info, info.makeItem(), pressable, tooltip);
				children.add(button);
				++i;
			}

			size = pack.normalInfos.size();
			unlocked = (int) pack.normalInfos.stream().filter($ -> !$.isLocked()).count();
			progress = (float) unlocked / size;
			height = (children.size() - 1) / 8 * 28 + 50;
		}

		@Override
		public java.util.List<? extends IGuiEventListener> children() {
			return children;
		}

		@Override
		public IGuiEventListener getFocused() {
			return null;
		}

		@Override
		public int getHeight() {
			return fold ? 18 : height;
		}

		@Override
		public boolean isDragging() {
			return false;
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int p_mouseClicked_5_) {
			if (mouseY - (top - parent.list.getScrollAmount()) < 35) {
				setFold(!fold);
				parent.list.refreshHeight();
				return true;
			}
			for (IGuiEventListener iguieventlistener : children()) {
				if (iguieventlistener.mouseClicked(mouseX, mouseY, p_mouseClicked_5_)) {
					setFocused(iguieventlistener);
					if (p_mouseClicked_5_ == 0) {
						setDragging(true);
					}

					return true;
				}
			}
			return true;
		}

		public void setFold(boolean fold) {
			this.fold = fold;
		}

		@Override
		public void render(MatrixStack matrix, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hover, float partialTicks) {
			AbstractGui.fill(matrix, left, top, left + entryWidth, top + entryHeight, 0x22FFFFFF);
			parent.font.draw(matrix, name, left + 8, top + 4, 0xFFFFFF);

			if (!fold) {
				children.forEach(btn -> {
					btn.y = btn.originalY + top;
					if (btn.y < -btn.getHeight() || btn.y > parent.height)
						return;
					btn.x = btn.originalX + left;
					btn.render(matrix, mouseX, mouseY, partialTicks);
				});
			}

			if (!KaleidoCommonConfig.autoUnlock()) {
				matrix.pushPose();
				matrix.scale(0.75f, 0.75f, 0.75f);
				AbstractGui.drawCenteredString(matrix, parent.font, unlocked + "/" + size, (int) ((left + entryWidth - 18) * 1.33), (int) ((top + 5.5) * 1.33), 0xFFFFFF);
				matrix.popPose();
				parent.minecraft.getTextureManager().bind(GUI_BARS_TEXTURES);
				AbstractGui.blit(matrix, left + entryWidth - 66, top + 5, parent.getBlitOffset(), 0, 0, 32, 5, 32, 32);
				if (unlocked > 0) {
					AbstractGui.blit(matrix, left + entryWidth - 66, top + 5, parent.getBlitOffset(), 0, 5, (int) (progress * 32), 10, 32, 32);
				}
			}
		}

		@Override
		public void setDragging(boolean p_setDragging_1_) {
		}

		@Override
		public void setFocused(IGuiEventListener p_setFocused_1_) {
		}

	}

	static class List extends MyList<Entry> {

		public List(Minecraft mcIn, int widthIn, int heightIn, int topIn) {
			super(mcIn, widthIn, heightIn, topIn);
			renderScrollbar = false;
			scrollFactor = 25;
		}

	}

	private static final Set<String> foldEntries = Sets.newHashSet();
	private static double scrollAmount;
	private static final Random RANDOM = new Random();
	private int coins;
	private ItemStack coinStack;
	private int cooldown;
	private List list;
	private final BlockPos pos;
	private StackButton selectedButton;
	private KButton addBtn;
	private KButton shrinkBtn;
	private KButton confirmBtn;
	private KEditBox editBox;
	private int timer;
	private float ticks;
	private final DarkBackground background = new DarkBackground();

	private java.util.List<ITextProperties> tip;

	private final World world;
	private boolean redeemed;

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
		editBox.setValue(Integer.toString(cur));
	}

	public int getRedeemAmount() {
		if (selectedButton == null) {
			return 0;
		}
		try {
			return Integer.parseInt(editBox.getValue());
		} catch (Exception e) {
			return 0;
		}
	}

	@Override
	protected void init() {
		if (!isUsable()) {
			return;
		}
		ClientPlayerEntity player = minecraft.player;
		if (tip == null) {
			String s;
			if (KaleidoCommonConfig.autoUnlock() || player.isCreative()) {
				s = I18n.get("tip.kaleido.quickSelect", minecraft.options.keyJump.getTranslatedKeyMessage().getString());
			} else {
				s = I18n.get("tip.kaleido.unlock");
			}
			tip = font.getSplitter().splitLines(s, 120, Style.EMPTY);
		}
		list = new List(minecraft, 238, height, 20);
		list.setLeftPos(30);
		/* off */
			KaleidoDataManager.INSTANCE.allPacks.values().stream()
					.map($ -> new Entry(this, $, foldEntries.contains($.id)))
					.sorted((a,b)->Float.compare(b.progress, a.progress))
					.forEachOrdered(list::addEntry);
		/* on */
		list.setScrollAmount(scrollAmount);
		children.add(list);

		addButton(shrinkBtn = new KButton(0, 0, 20, 20, new StringTextComponent("-"), btn -> {
			addNum(-1);
		}));
		shrinkBtn.blur = true;
		addButton(editBox = new KEditBox(0, 0, 38, 18, new StringTextComponent("")));
		editBox.setContentType(ContentType.Int);
		editBox.setFilter(str -> {
			if (str == null) {
				return false;
			}
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
		addButton(addBtn = new KButton(0, 0, 20, 20, new StringTextComponent("+"), btn -> {
			addNum(1);
		}));
		addBtn.blur = true;
		addButton(confirmBtn = new KButton(0, 0, 20, 20, new StringTextComponent("✓"), btn -> {
			if (cooldown > 0) {
				return;
			}
			if (selectedButton != null) {
				if (redeemed && !selectedButton.stack.equals(player.getMainHandItem(), true)) {
					player.inventory.selected = (player.inventory.selected + 1) % 9;
					player.connection.send(new CHeldItemChangePacket(player.inventory.selected));
				}
				new CRedeemPacket(selectedButton.info, getRedeemAmount()).send();
				timer = 17; // defer update coins
				cooldown = 8;
				redeemed = true;
			}
		}));
		confirmBtn.blur = true;
		confirmBtn.lineColor = 0x0894ED;
		if (selectedButton == null) {
			setSelectedButton(null);
		}
		update();
	}

	@Override
	public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
		super.resize(pMinecraft, pWidth, pHeight);
		list.setHeight(pHeight);
		int x = width / 2 + 125;
		int y = height / 2 + 50;
		editBox.x = x - 19;
		editBox.y = y + 1;
		shrinkBtn.setPos(x - 40, y);
		addBtn.setPos(x + 20, y);
		confirmBtn.setPos(x + 40, y);
	}

	private void saveState() {
		if (list == null)
			return;
		foldEntries.clear();
		for (Entry entry : list.children()) {
			if (entry.fold) {
				foldEntries.add(entry.id);
			}
		}
		scrollAmount = list.getScrollAmount();
	}

	@Override
	public void removed() {
		saveState();
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
		background.closing = true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void render(MatrixStack matrix, int mouseX, int mouseY, float pTicks) {
		ticks += pTicks;
		this.renderBackground(matrix);
		if (background.isClosed()) {
			Minecraft.getInstance().setScreen(null);
			list = null;
			return;
		}
		float top = MathHelper.clamp(ticks / 8, 0, 1);
		if (top > 0 && top < 1) {
			top = (float) (Math.pow(2, -10 * top)) * MathHelper.sin((top * 6 - 0.75f) * 2.1f) + 1;
		}
		list.setTop(top * height - height + 20);
		list.render(matrix, mouseX, mouseY, pTicks);
		int textColor = KaleidoUtil.applyAlpha(0xFFFFFF, background.alpha);
		font.draw(matrix, getTitle(), list.x0 + 4, list.y0 - 14, 0xFFFFFF);

		itemRenderer.renderAndDecorateItem(coinStack, width - 20, (int) (top * 20 - 15));
		String numText = Integer.toString(coins);
		font.drawShadow(matrix, numText, width - 25 - font.width(numText), top * 20 - 11, textColor);

		for (Widget widget : buttons) {
			widget.setAlpha(background.alpha);
			widget.render(matrix, mouseX, mouseY, pTicks);
		}
		int x = minecraft.getWindow().getGuiScaledWidth() / 2 + 85;
		int y = minecraft.getWindow().getGuiScaledHeight() / 2 - 65;
		if (selectedButton != null) {
			if (background.alpha < 0.5f) {
				return;
			}
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
			drawCenteredString(matrix, font, selectedButton.stack.getHoverName(), x, y, textColor);
			if (!selectedButton.info.isLocked()) {
				itemRenderer.renderAndDecorateItem(coinStack, x + 45, y + 95);
				int amount = getRedeemAmount();
				if (amount < 1) {
					amount = 1;
				}
				itemRenderer.renderGuiItemDecorations(font, coinStack, x + 50, y + 95, Integer.toString(amount * selectedButton.info.price));
			}
		} else {
			x += 40;
			y += 40;
			for (ITextProperties s : tip) {
				drawCenteredString(matrix, font, s.getString(), x, y, textColor);
				y += 15;
			}
		}
	}

	@Override
	public void renderBackground(MatrixStack matrix, int p_renderBackground_1_) {
		background.renderBackground(this, matrix, minecraft.getDeltaFrameTime());
	}

	public void setSelectedButton(StackButton selectedButton) {
		this.selectedButton = selectedButton;
		if (selectedButton == null) {
			confirmBtn.visible = false;
			addBtn.visible = false;
			shrinkBtn.visible = false;
			confirmBtn.visible = false;
			editBox.visible = false;
		} else {
			if (editBox.getValue().isEmpty()) {
				editBox.setValue("1");
			}
			confirmBtn.visible = true;
			addBtn.visible = true;
			shrinkBtn.visible = true;
			confirmBtn.visible = true;
			editBox.visible = true;
			boolean active = !selectedButton.info.isLocked();
			confirmBtn.active = active;
			addBtn.active = active;
			shrinkBtn.active = active;
			confirmBtn.active = active;
			editBox.active = active;
		}
	}

	@Override
	public void tick() {
		editBox.tick();
		--cooldown;
		if (!background.closing && ++timer == 20) {
			timer = 0;
			update();
		}
		//		System.out.println(minecraft.options.keyUp.isDown());
		//		if (minecraft.options.keyUp.matches(key, scan)) {
		//			list.setScrollAmount(list.getScrollAmount() - list.scrollFactor * 2);
		//		}
		//		else if (minecraft.options.keyDown.matches(key, scan)) {
		//			list.setScrollAmount(list.getScrollAmount() + list.scrollFactor * 2);
		//		}
	}

	public void update() {
		if (!isUsable()) {
			onClose();
		} else {
			coins = KaleidoUtil.getCoins(minecraft.player);
		}
	}

	@Override
	public boolean keyPressed(int key, int scan, int mods) {
		if (minecraft.options.keyJump.matches(key, scan)) {
			int x = (int) (minecraft.mouseHandler.xpos() * minecraft.getWindow().getGuiScaledWidth() / minecraft.getWindow().getScreenWidth());
			int y = (int) (minecraft.mouseHandler.ypos() * minecraft.getWindow().getGuiScaledHeight() / minecraft.getWindow().getScreenHeight());
			IGuiEventListener widget = list.getChildAt(x, y).orElse(null);
			if (widget instanceof Entry) {
				widget = ((Entry) widget).getChildAt(x, y).orElse(null);
				if (widget instanceof StackButton) {
					((StackButton) widget).playDownSound(minecraft.getSoundManager());
					((StackButton) widget).onPress();
					if (minecraft.player.isCreative()) {
						confirmBtn.onPress();
					}
					return true;
				}
			}
		}
		return super.keyPressed(key, scan, mods);
	}

}
