package snownee.kaleido.scope.client.gui;

import java.text.DecimalFormat;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import snownee.kaleido.core.client.gui.DarkBackground;
import snownee.kaleido.scope.ScopeStack;
import snownee.kaleido.scope.block.ScopeBlockEntity;
import snownee.kaleido.util.GhostRenderType;
import snownee.kaleido.util.SimulationBlockReader;
import snownee.kaleido.util.SmoothChasingValue;
import snownee.kaleido.util.SmoothChasingVector;

public class ScopeScreen extends Screen {

	private static class StackInfo {
		private final ScopeStack stack;
		private final SmoothChasingValue alpha;
		private final SmoothChasingVector rotation = new SmoothChasingVector(true);
		private final SmoothChasingVector scale = new SmoothChasingVector();
		private final SmoothChasingVector translation = new SmoothChasingVector();

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
	}

	private final DarkBackground background = new DarkBackground();
	private final ScopeBlockEntity scope;
	private float ticks;
	private final SimulationBlockReader blockReader = new SimulationBlockReader();
	private final SmoothChasingValue scale = new SmoothChasingValue();
	private final SmoothChasingValue rotX = new SmoothChasingValue();
	private final SmoothChasingValue rotY = new SmoothChasingValue();
	private final Map<ScopeStack, StackInfo> infos = Maps.newIdentityHashMap();
	private final AxisTextField[] textFields = new AxisTextField[9];
	@Nullable
	private StackInfo activeInfo;
	private CheckboxButton snapCheckbox;

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
		int x = width - 130;
		snapCheckbox = new CheckboxButton(x, 100, 20, 20, new TranslationTextComponent("gui.kaleido.snap"), $ -> {
		});
		addButton(snapCheckbox);

		ITextComponent translationTitle = new TranslationTextComponent("gui.kaleido.translation");
		ITextComponent scaleTitle = new TranslationTextComponent("gui.kaleido.scale");
		ITextComponent rotationTitle = new TranslationTextComponent("gui.kaleido.rotation");

		DecimalFormat dfCommas = new DecimalFormat("##.###");

		AxisTextField textField;
		for (Axis axis0 : Axis.values()) {
			Axis axis = axis0;
			int ord = axis.ordinal();
			// position
			textField = new AxisTextField(x, 20, 40, 15, translationTitle, snapCheckbox::selected, 1, axis);
			textField.getter = () -> activeInfo == null ? "" : dfCommas.format(activeInfo.translation.get(ord).getTarget());
			textField.setter = f -> {
				if (activeInfo != null)
					activeInfo.translation.get(ord).target(f);
			};
			textFields[ord] = textField;
			// scale
			textField = new AxisTextField(x, 50, 40, 15, scaleTitle, snapCheckbox::selected, 0.1F, axis);
			textField.getter = () -> activeInfo == null ? "" : dfCommas.format(activeInfo.scale.get(ord).getTarget());
			textField.setter = f -> {
				if (activeInfo != null)
					activeInfo.scale.get(ord).target(f);
			};
			textFields[ord + 3] = textField;
			// rotation
			textField = new AxisTextField(x, 80, 40, 15, rotationTitle, snapCheckbox::selected, 22.5F, axis);
			textField.getter = () -> activeInfo == null ? "" : dfCommas.format(activeInfo.rotation.get(ord).getTarget() / 180);
			textField.setter = f -> {
				if (activeInfo != null)
					activeInfo.rotation.get(ord).target(f);
			};
			textFields[ord + 6] = textField;
			x += 42;
		}
		//retain focus (tab) order, add widgets at last
		for (AxisTextField textField0 : textFields) {
			addButton(textField0);
		}
		setActiveInfo(getInfo(scope.stacks.get(0)));
	}

	@Override
	public void tick() {
		for (AxisTextField textField : textFields) {
			textField.tick();
		}
	}

	public void setActiveInfo(StackInfo info) {
		activeInfo = info;
		for (AxisTextField textField : textFields) {
			textField.setValue(textField.getter.get());
			textField.active = info != null;
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
			scope.refresh(); //TODO
			return;
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
		for (ScopeStack stack : scope.stacks) {
			StackInfo info = getInfo(stack);
			info.tick(pPartialTicks);
			buffer.setAlpha(info.alpha.value);
			stack.render(pMatrixStack, buffer, blockReader, scope.getBlockPos(), OverlayTexture.NO_OVERLAY, false);
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
	}

	@Override
	public void onClose() {
		background.closing = true;
		for (StackInfo info : infos.values()) {
			info.alpha.target(0).withSpeed(0.7F);
		}
	}

}
