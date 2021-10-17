package snownee.kaleido.scope.client.gui;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import snownee.kaleido.core.client.KaleidoClient;
import snownee.kaleido.core.client.cursor.Cursor;
import snownee.kaleido.core.client.cursor.CursorChanger;
import snownee.kaleido.core.client.cursor.StandardCursor;
import snownee.kaleido.core.client.gui.DarkBackground;
import snownee.kaleido.core.client.gui.KaleidoButton;
import snownee.kaleido.core.client.gui.Label;
import snownee.kaleido.scope.ScopeStack;
import snownee.kaleido.scope.block.ScopeBlockEntity;
import snownee.kaleido.scope.network.CConfigureScopePacket;
import snownee.kaleido.scope.network.CConfigureScopePacket.Data;
import snownee.kaleido.util.GhostRenderType;
import snownee.kaleido.util.KaleidoUtil;
import snownee.kaleido.util.SimulationBlockReader;
import snownee.kaleido.util.SmoothChasingValue;
import snownee.kaleido.util.SmoothChasingVector;

public class ScopeScreen extends Screen {

	private static class StackInfo {
		private final ScopeStack stack;
		private final SmoothChasingValue alpha;
		private final SmoothChasingVector rotation = new SmoothChasingVector();
		private final SmoothChasingVector scale = new SmoothChasingVector();
		private final SmoothChasingVector translation = new SmoothChasingVector();
		private boolean removed;
		private KaleidoButton button;
		private KaleidoButton removeButton;

		private StackInfo(ScopeStack stack) {
			this.stack = stack;
			alpha = new SmoothChasingValue().target(1).withSpeed(0.2F);
			translation.start(stack.translation);
			rotation.start(stack.rotation);
			scale.start(stack.scale);
		}

		private void tick(float ticks) {
			alpha.tick(ticks);
			rotation.tick(ticks);
			scale.tick(ticks);
			translation.tick(ticks);
			rotation.copyTo(stack.rotation);
			stack.updateRotation();
			scale.copyTo(stack.scale);
			translation.copyTo(stack.translation);
		}

		private Data toData() {
			Data data = new Data();
			data.removed = removed;
			if (!removed) {
				data.position = translation.getTarget();
				data.size = scale.getTarget();
				data.rotation = rotation.getTarget();
			}
			return data;
		}
	}

	private static boolean snap = true;
	private final DarkBackground background = new DarkBackground();
	private float ticks;
	private final SimulationBlockReader blockReader = new SimulationBlockReader();
	private final SmoothChasingValue scale = new SmoothChasingValue();
	private final SmoothChasingValue rotX = new SmoothChasingValue();
	private final SmoothChasingValue rotY = new SmoothChasingValue();
	private final BlockPos tilePos;
	private final List<ScopeStack> stacks;
	private final Map<ScopeStack, StackInfo> infos = Maps.newIdentityHashMap();
	private final AxisEditBox[] editBoxes = new AxisEditBox[9];
	@Nullable
	private StackInfo activeInfo;
	private CheckboxButton snapCheckbox;
	private KaleidoButton resetButton;
	private boolean canceled;
	private KaleidoButton cancelButton;
	private KaleidoButton confirmButton;
	private Label positionLabel;
	private Label sizeLabel;
	private Label rotationLabel;
	private int existedStacks;

	public ScopeScreen(ScopeBlockEntity blockEntity) {
		super(new TranslationTextComponent("gui.kaleido.scope"));
		tilePos = blockEntity.getBlockPos();
		stacks = ImmutableList.copyOf(blockEntity.stacks);
		existedStacks = stacks.size();
		blockReader.setOverrideLight(15);
		blockReader.setLevel(Minecraft.getInstance().level);
		scale.set(160).target(60).withSpeed(0.2F);
		ActiveRenderInfo camera = Minecraft.getInstance().gameRenderer.getMainCamera();
		rotX.target(camera.getXRot()).withSpeed(0.2F);
		rotY.target(camera.getYRot() + 180).withSpeed(0.2F);
		rotX.value = rotX.getTarget() - 20;
		rotY.value = rotY.getTarget() + 20;
	}

	@Override
	protected void init() {
		int i = 0;
		ITextComponent xTitle = new StringTextComponent("X");
		for (ScopeStack stack : stacks) {
			StackInfo info = getInfo(stack);
			KaleidoButton button = new KaleidoButton(0, 50 + i * 20, 90, 20, stack.blockDefinition.getDescription(), $ -> {
				setActiveInfo(info);
			});
			button.yOffset = 1;
			info.button = button;
			addButton(button);
			if (i != 0 || minecraft.player.isCreative()) {
				KaleidoButton removeButton = new KaleidoButton(90, 50 + i * 20, 20, 20, xTitle, $ -> {
					info.removed = true;
					info.button.visible = false;
					$.visible = false;
					if (activeInfo == info)
						setActiveInfo(null);
					--existedStacks;
					sortStackButtons();
				});
				removeButton.xOffset = 1;
				removeButton.yOffset = 1;
				removeButton.lineColor = 0xFF1242;
				info.removeButton = removeButton;
				addButton(removeButton);
			}
			++i;
		}

		snapCheckbox = new CheckboxButton(0, 5, 30, 18, new TranslationTextComponent("gui.kaleido.snap"), $ -> {
		});
		snapCheckbox.selected = snap;
		addButton(snapCheckbox);

		resetButton = new KaleidoButton(0, 5, 35, 18, new TranslationTextComponent("gui.kaleido.reset"), $ -> reset());
		addButton(resetButton);

		ITextComponent translationTitle = new TranslationTextComponent("gui.kaleido.translation");
		ITextComponent scaleTitle = new TranslationTextComponent("gui.kaleido.scale");
		ITextComponent rotationTitle = new TranslationTextComponent("gui.kaleido.rotation");

		positionLabel = new Label(0, 30, 100, 0, translationTitle);
		addButton(positionLabel);
		sizeLabel = new Label(0, 60, 100, 0, scaleTitle);
		addButton(sizeLabel);
		rotationLabel = new Label(0, 90, 100, 0, rotationTitle);
		addButton(rotationLabel);

		DecimalFormat dfCommas = new DecimalFormat("##.###");
		AxisEditBox editBox;
		for (Axis axis0 : Axis.values()) {
			Axis axis = axis0;
			int ord = axis.ordinal();
			// position
			editBox = new AxisEditBox(0, 0, 40, 15, translationTitle, snapCheckbox::selected, 1, axis);
			editBox.getter = () -> activeInfo == null ? "" : dfCommas.format(activeInfo.translation.get(ord).getTarget());
			editBox.setter = f -> {
				if (activeInfo != null) {
					f = MathHelper.clamp(f, -32, 32);
					activeInfo.translation.get(ord).target(f);
				}
			};
			editBoxes[ord] = editBox;
			// scale
			editBox = new AxisEditBox(0, 0, 40, 15, scaleTitle, snapCheckbox::selected, 0.1F, axis);
			editBox.getter = () -> activeInfo == null ? "" : dfCommas.format(activeInfo.scale.get(ord).getTarget());
			editBox.setter = f -> {
				if (activeInfo != null) {
					f = MathHelper.clamp(f, -2, 2);
					activeInfo.scale.get(ord).target(f);
				}
			};
			editBoxes[ord + 3] = editBox;
			// rotation
			editBox = new AxisEditBox(0, 0, 40, 15, rotationTitle, snapCheckbox::selected, 22.5F, axis);
			editBox.getter = () -> activeInfo == null ? "" : dfCommas.format(activeInfo.rotation.get(ord).getTarget());
			editBox.setter = f -> {
				if (activeInfo != null)
					activeInfo.rotation.get(ord).target(f);
			};
			editBoxes[ord + 6] = editBox;
		}
		//retain focus (tab) order, add widgets at last
		for (AxisEditBox editBox2 : editBoxes) {
			addButton(editBox2);
		}

		cancelButton = new KaleidoButton(0, 0, 50, 20, new TranslationTextComponent("gui.kaleido.cancel"), $ -> {
			canceled = true;
			onClose();
		});
		addButton(cancelButton);

		confirmButton = new KaleidoButton(0, 0, 50, 20, new TranslationTextComponent("gui.kaleido.confirm"), $ -> {
			onClose();
		});
		confirmButton.lineColor = 0x0894ED;
		addButton(confirmButton);

		Label inDevLabel = new Label(5, height - 11, 100, 15, new StringTextComponent("In Development"));
		inDevLabel.active = false;
		addButton(inDevLabel);

		setActiveInfo(stacks.size() == 1 ? getInfo(stacks.get(0)) : null);
	}

	@Override
	public void init(Minecraft pMinecraft, int pWidth, int pHeight) {
		minecraft = pMinecraft;
		itemRenderer = pMinecraft.getItemRenderer();
		font = pMinecraft.font;
		width = pWidth;
		height = pHeight;
		Consumer<Widget> remove = b -> {
			buttons.remove(b);
			children.remove(b);
		};
		if (!MinecraftForge.EVENT_BUS.post(new InitGuiEvent.Pre(this, buttons, this::addButton, remove))) {
			buttons.clear();
			children.clear();
			setFocused((IGuiEventListener) null);
			this.init();
			resize(pMinecraft, pWidth, pHeight);
		}
		MinecraftForge.EVENT_BUS.post(new InitGuiEvent.Post(this, buttons, this::addButton, remove));
	}

	@Override
	public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
		minecraft = pMinecraft;
		itemRenderer = pMinecraft.getItemRenderer();
		font = pMinecraft.font;
		width = pWidth;
		height = pHeight;

		int y = 41;
		int x = width - 127;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				AxisEditBox editBox = editBoxes[i * 3 + j];
				editBox.x = x + j * 42;
				editBox.y = y + i * 30;
			}
		}

		snapCheckbox.pos.x.start(x);
		resetButton.pos.x.start(x + snapCheckbox.getWidth() + 2);
		x += 2;
		positionLabel.x = x;
		sizeLabel.x = x;
		rotationLabel.x = x;
		x += 22;
		cancelButton.pos.x.start(x);
		cancelButton.pos.y.start(height - 25);
		confirmButton.pos.x.start(x + 51);
		confirmButton.pos.y.start(height - 25);
	}

	public void sortStackButtons() {
		int i = 0;
		for (ScopeStack stack : stacks) {
			StackInfo info = getInfo(stack);
			if (info.removed) {
				continue;
			}
			info.button.pos.y.target(50 + i * 20);
			if (info.removeButton != null) {
				info.removeButton.pos.y.target(50 + i * 20);
			}
			++i;
		}
	}

	@Override
	public void tick() {
		for (AxisEditBox editBox : editBoxes) {
			editBox.tick();
		}
	}

	private void reset() {
		if (activeInfo == null) {
			return;
		}
		for (AxisEditBox editBox : editBoxes) {
			editBox.setter.accept(0);
			editBox.setValue(editBox.getter.get());
		}
	}

	public void setActiveInfo(StackInfo info) {
		activeInfo = info;
		positionLabel.active = sizeLabel.active = rotationLabel.active = info != null;
		for (AxisEditBox editBox : editBoxes) {
			editBox.setValue(editBox.getter.get());
			editBox.active = info != null;
		}
	}

	@Override
	public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
		if (ticks + pPartialTicks > 20) {
			rotX.withSpeed(0.4F);
			rotY.withSpeed(0.4F);
		}
		ticks += pPartialTicks;
		scale.tick(pPartialTicks);
		rotX.tick(pPartialTicks);
		rotY.tick(pPartialTicks);
		this.renderBackground(pMatrixStack);
		if (background.isClosed()) {
			Minecraft.getInstance().setScreen(null);
			return;
		}
		for (Widget widget : buttons) {
			widget.setAlpha(background.alpha);
		}
		pMatrixStack.pushPose();

		pMatrixStack.translate(width / 2 + 0.5, height / 2 + 0.5, 200.5);
		pMatrixStack.scale(scale.value, -scale.value, scale.value);
		pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(rotX.value));
		pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(rotY.value));
		pMatrixStack.translate(-0.5, -0.5, -0.5);

		IGuiEventListener pointingListener = getChildAt(pMouseX, pMouseY).orElse(null);
		boolean drawBox = false;
		if (activeInfo != null) {
			if (pointingListener instanceof AxisEditBox) {
				drawBox = true;
			} else if (getFocused() instanceof AxisEditBox) {
				drawBox = true;
			}
		}
		if (drawBox) {
			IRenderTypeBuffer.Impl buffer = minecraft.renderBuffers().bufferSource();
			WorldRenderer.renderLineBox(pMatrixStack, buffer.getBuffer(RenderType.lines()), 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0.45F, 0.45F, 0.45F);
			buffer.endBatch();
		}

		GhostRenderType.Buffer buffer = GhostRenderType.defaultBuffer();
		BlockModelRenderer.clearCache();
		List<StackInfo> toRender = Lists.newArrayListWithCapacity(stacks.size());
		StackInfo hovered = null;
		for (ScopeStack stack : stacks) {
			StackInfo info = getInfo(stack);
			if (info.removed) {
				continue;
			}
			info.button.setFocused(info == activeInfo);
			if (info.button.hackyIsHovered()) {
				hovered = info;
			}
			if (info.removeButton != null) {
				if (existedStacks == 1) {
					info.removeButton.visible = false;
				} else {
					info.removeButton.visible = info.button.hackyIsHovered() || info.removeButton.isHovered();
					if (info.removeButton.hackyIsHovered()) {
						hovered = info;
					}
				}

				if (info.removeButton.visible && info.button.wrappedTitle != null) {
					String x = info.button.hackyIsHovered() ? "" : "X";
					info.removeButton.setMessage(new StringTextComponent(x));
				}
			}
			toRender.add(info);
		}
		for (StackInfo info : toRender) {
			if (!background.closing) {
				float alpha = 1;
				if (hovered != null && hovered != info) {
					alpha = 0.4F;
				}
				info.alpha.target(alpha);
			}
			info.tick(pPartialTicks);
			buffer.setAlpha(info.alpha.value);
			info.stack.render(pMatrixStack, buffer, blockReader, tilePos, OverlayTexture.NO_OVERLAY, false);
		}
		BlockModelRenderer.enableCaching();
		//		buffer.endBatch(GhostRenderType.remap(RenderType.solid(), 1));
		//		buffer.endBatch(GhostRenderType.remap(RenderType.cutoutMipped(), 1));
		//		buffer.endBatch(GhostRenderType.remap(RenderType.cutout(), 1));
		//		buffer.endBatch(RenderType.translucent());
		//		buffer.getBuffer(RenderType.translucent()).sortQuads(1, 1, 1);
		buffer.endBatch();
		pMatrixStack.popPose();

		if (!isDragging()) {
			Cursor cursor = StandardCursor.ARROW;
			if (pointingListener instanceof CursorChanger) {
				cursor = ((CursorChanger) pointingListener).getCursor(pMouseX, pMouseY, pPartialTicks);
			}
			cursor.use();
		}

		super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
	}

	private StackInfo getInfo(ScopeStack stack) {
		return infos.computeIfAbsent(stack, StackInfo::new);
	}

	@Override
	public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
		if (super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY))
			return true;
		rotX.target(rotX.getTarget() + (float) pDragY * 2);
		rotY.target(rotY.getTarget() + (float) pDragX * 2);
		return true;
	}

	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
		boolean success = false;
		for (IGuiEventListener iguieventlistener : children()) {
			if (iguieventlistener.mouseClicked(pMouseX, pMouseY, pButton)) {
				setFocused(iguieventlistener);
				if (pButton == 0) {
					setDragging(true);
				}
				success = true;
			}
		}
		if (!success) {
			setFocused(null);
		}
		return success;
	}

	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
		if (super.mouseScrolled(pMouseX, pMouseY, pDelta))
			return true;
		float f = MathHelper.clamp(scale.getTarget() + (float) pDelta * 10, 40, 130);
		scale.target(f);
		return true;
	}

	@Override
	public void renderBackground(MatrixStack matrix, int p_renderBackground_1_) {
		background.renderBackground(this, matrix, minecraft.getDeltaFrameTime());
		int bgColor = KaleidoUtil.applyAlpha(KaleidoClient.bgColor, background.alpha);
		//fill(matrix, 0, 0, 140, height, bgColor);
		fill(matrix, width - 130, 0, width, height, bgColor);
	}

	@Override
	public void onClose() {
		List<Data> data = Lists.newArrayList();
		if (!canceled) {
			for (ScopeStack stack : stacks) {
				data.add(getInfo(stack).toData());
			}
		}
		new CConfigureScopePacket(tilePos, data).send();

		background.closing = true;
		for (StackInfo info : infos.values()) {
			info.alpha.target(0).withSpeed(0.7F);
		}
	}

}
