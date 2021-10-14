package snownee.kaleido.scope.client.gui;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import snownee.kaleido.core.client.KaleidoClient;
import snownee.kaleido.core.client.gui.DarkBackground;
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
		private Button button;

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

	private final DarkBackground background = new DarkBackground();
	private final ScopeBlockEntity scope;
	private float ticks;
	private final SimulationBlockReader blockReader = new SimulationBlockReader();
	private final SmoothChasingValue scale = new SmoothChasingValue();
	private final SmoothChasingValue rotX = new SmoothChasingValue();
	private final SmoothChasingValue rotY = new SmoothChasingValue();
	private final Map<ScopeStack, StackInfo> infos = Maps.newIdentityHashMap();
	private final AxisEditBox[] editBoxes = new AxisEditBox[9];
	@Nullable
	private StackInfo activeInfo;
	private CheckboxButton snapCheckbox;
	private boolean canceled;
	private Button cancelButton;
	private Button confirmButton;

	public ScopeScreen(ScopeBlockEntity blockEntity) {
		super(new TranslationTextComponent("gui.kaleido.scope"));
		scope = blockEntity;
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
		for (ScopeStack stack : scope.stacks) {
			StackInfo info = getInfo(stack);
			Button button = new Button(0, 50 + i * 20, 90, 20, stack.blockDefinition.getDescription(), $ -> {
				setActiveInfo(info);
			});
			info.button = button;
			addButton(button);
			++i;
		}

		snapCheckbox = new CheckboxButton(0, 10, 30, 20, new TranslationTextComponent("gui.kaleido.snap"), $ -> {
		});
		addButton(snapCheckbox);

		ITextComponent translationTitle = new TranslationTextComponent("gui.kaleido.translation");
		ITextComponent scaleTitle = new TranslationTextComponent("gui.kaleido.scale");
		ITextComponent rotationTitle = new TranslationTextComponent("gui.kaleido.rotation");

		DecimalFormat dfCommas = new DecimalFormat("##.###");

		AxisEditBox editBox;
		for (Axis axis0 : Axis.values()) {
			Axis axis = axis0;
			int ord = axis.ordinal();
			// position
			editBox = new AxisEditBox(0, 0, 40, 15, translationTitle, snapCheckbox::selected, 1, axis);
			editBox.getter = () -> activeInfo == null ? "" : dfCommas.format(activeInfo.translation.get(ord).getTarget());
			editBox.setter = f -> {
				if (activeInfo != null)
					activeInfo.translation.get(ord).target(f);
			};
			editBoxes[ord] = editBox;
			// scale
			editBox = new AxisEditBox(0, 0, 40, 15, scaleTitle, snapCheckbox::selected, 0.1F, axis);
			editBox.getter = () -> activeInfo == null ? "" : dfCommas.format(activeInfo.scale.get(ord).getTarget());
			editBox.setter = f -> {
				if (activeInfo != null)
					activeInfo.scale.get(ord).target(f);
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

		cancelButton = new Button(0, 0, 50, 20, new TranslationTextComponent("gui.kaleido.cancel"), $ -> {
			canceled = true;
			onClose();
		});
		addButton(cancelButton);

		confirmButton = new Button(0, 0, 50, 20, new TranslationTextComponent("gui.kaleido.confirm"), $ -> {
			onClose();
		});
		addButton(confirmButton);
	}

	@Override
	public void init(Minecraft pMinecraft, int pWidth, int pHeight) {
		minecraft = pMinecraft;
		itemRenderer = pMinecraft.getItemRenderer();
		font = pMinecraft.font;
		width = pWidth;
		height = pHeight;
		java.util.function.Consumer<Widget> remove = (b) -> {
			buttons.remove(b);
			children.remove(b);
		};
		if (!net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent.Pre(this, buttons, this::addButton, remove))) {
			buttons.clear();
			children.clear();
			setFocused((IGuiEventListener) null);
			this.init();
			resize(pMinecraft, pWidth, pHeight);
		}
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent.Post(this, buttons, this::addButton, remove));
	}

	@Override
	public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
		minecraft = pMinecraft;
		itemRenderer = pMinecraft.getItemRenderer();
		font = pMinecraft.font;
		width = pWidth;
		height = pHeight;

		int y = 40;
		int x = width - 127;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				AxisEditBox editBox = editBoxes[i * 3 + j];
				editBox.x = x + j * 42;
				editBox.y = y + i * 30;
			}
		}

		snapCheckbox.x = x;
		x += 25;
		cancelButton.x = x;
		cancelButton.y = height - 25;
		confirmButton.x = x + 50;
		confirmButton.y = height - 25;
	}

	@Override
	public void tick() {
		for (AxisEditBox editBox : editBoxes) {
			editBox.tick();
		}
	}

	public void setActiveInfo(StackInfo info) {
		activeInfo = info;
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
		super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
		pMatrixStack.pushPose();

		pMatrixStack.translate(width / 2 + 0.5, height / 2 + 0.5, 100.5);
		pMatrixStack.scale(scale.value, -scale.value, scale.value);
		pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(rotX.value));
		pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(rotY.value));
		pMatrixStack.translate(-0.5, -0.5, -0.5);

		GhostRenderType.Buffer buffer = GhostRenderType.defaultBuffer();
		BlockModelRenderer.clearCache();
		List<StackInfo> toRender = Lists.newArrayListWithCapacity(scope.stacks.size());
		StackInfo hovered = null;
		for (ScopeStack stack : scope.stacks) {
			StackInfo info = getInfo(stack);
			if (info.removed) {
				continue;
			}
			if (info.button.isHovered()) {
				hovered = info;
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
			info.stack.render(pMatrixStack, buffer, blockReader, scope.getBlockPos(), OverlayTexture.NO_OVERLAY, false);
		}
		BlockModelRenderer.enableCaching();

		buffer.endBatch();
		pMatrixStack.popPose();
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
		background.closing = true;
		for (StackInfo info : infos.values()) {
			info.alpha.target(0).withSpeed(0.7F);
		}
	}

	@Override
	public void removed() {
		List<Data> data = Lists.newArrayList();
		if (!canceled) {
			for (ScopeStack stack : scope.stacks) {
				data.add(getInfo(stack).toData());
			}
		}
		new CConfigureScopePacket(scope.getBlockPos(), data).send();
		scope.refresh(); //TODO
	}

}
