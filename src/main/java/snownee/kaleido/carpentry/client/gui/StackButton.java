package snownee.kaleido.carpentry.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kaleido.KaleidoCommonConfig;
import snownee.kaleido.core.ModelInfo;

@OnlyIn(Dist.CLIENT)
public class StackButton extends Button {

	public static void renderItemIntoGUI(ItemRenderer renderer, ItemStack stack, int x, int y, int light) {
		renderItemModelIntoGUI(renderer, stack, x, y, renderer.getModel(stack, (World) null, (LivingEntity) null), light);
	}

	@SuppressWarnings("deprecation")
	public static void renderItemModelIntoGUI(ItemRenderer renderer, ItemStack stack, int x, int y, IBakedModel bakedmodel, int light) {
		RenderSystem.pushMatrix();
		TextureManager textureManager = Minecraft.getInstance().getTextureManager();
		textureManager.bind(PlayerContainer.BLOCK_ATLAS);
		textureManager.getTexture(PlayerContainer.BLOCK_ATLAS).setFilter(false, false);
		RenderSystem.enableRescaleNormal();
		RenderSystem.enableAlphaTest();
		RenderSystem.defaultAlphaFunc();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.translatef(x, y, 100.0F + renderer.blitOffset);
		RenderSystem.translatef(8.0F, 8.0F, 0.0F);
		RenderSystem.scalef(1.0F, -1.0F, 1.0F);
		RenderSystem.scalef(16.0F, 16.0F, 16.0F);
		MatrixStack matrixstack = new MatrixStack();
		IRenderTypeBuffer.Impl irendertypebuffer$impl = Minecraft.getInstance().renderBuffers().bufferSource();
		boolean flag = !bakedmodel.usesBlockLight();
		if (flag) {
			RenderHelper.setupForFlatItems();
		}

		renderer.render(stack, ItemCameraTransforms.TransformType.GUI, false, matrixstack, irendertypebuffer$impl, light, OverlayTexture.NO_OVERLAY, bakedmodel);
		irendertypebuffer$impl.endBatch();
		RenderSystem.enableDepthTest();
		if (flag) {
			RenderHelper.setupFor3DItems();
		}

		RenderSystem.disableAlphaTest();
		RenderSystem.disableRescaleNormal();
		RenderSystem.popMatrix();
	}

	public int frameColor = 0xFFFFFF;
	private float hoverProgress;
	public final ModelInfo info;
	public int originalX, originalY;
	public boolean selected;
	public final ItemStack stack;
	private final CarpentryCraftingScreen.List list;

	public StackButton(CarpentryCraftingScreen.List list, int x, int y, ModelInfo info, ItemStack stack, IPressable onPress, Button.ITooltip onTooltip) {
		super(x, y, 27, 27, stack.getDisplayName(), onPress, onTooltip);
		this.stack = stack;
		this.info = info;
		this.list = list;
		originalX = x;
		originalY = y;
		alpha = 0;
		if (!KaleidoCommonConfig.autoUnlock && info.reward) {
			frameColor = 0xFFDF00;
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public void renderButton(MatrixStack matrix, int mouseX, int mouseY, float pTicks) {
		//        alpha = Math.min(alpha + pTicks * 0.2F, 1);
		//        int y = (int) (this.y + 15 - 15 * MathHelper.sin(alpha));
		AbstractGui.fill(matrix, x, y, x + width, y + height, 0xAA222222);
		isHovered = isHovered && list.isMouseOver(mouseX, mouseY);
		if (!info.isLocked()) {
			hoverProgress += isHovered ? pTicks * .2f : -pTicks * .2f;
		}
		hoverProgress = MathHelper.clamp(hoverProgress, .4f, 1);
		int linecolor = frameColor | (int) (hoverProgress * 0xFF) << 24;
		AbstractGui.fill(matrix, x, y, x + 1, y + height, linecolor);
		AbstractGui.fill(matrix, x + width - 1, y, x + width, y + height, linecolor);
		AbstractGui.fill(matrix, x + 1, y, x + width - 1, y + 1, linecolor);
		AbstractGui.fill(matrix, x + 1, y + height - 1, x + width - 1, y + height, linecolor);
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		RenderSystem.pushMatrix();
		RenderSystem.translatef(x + 4, y + 4, 0);
		RenderSystem.scalef(1.25F, 1.25F, 1.25F);
		renderItemIntoGUI(itemRenderer, stack, 0, 0, info.isLocked() ? 0 : 15728880);
		RenderSystem.popMatrix();
		if (info.isLocked()) {
			RenderSystem.pushMatrix();
			RenderSystem.translatef(0, 0, 500);
			FontRenderer font = Minecraft.getInstance().font;
			font.drawShadow(matrix, "?", x + 11, y + 11, 0xFF888888);
			RenderSystem.popMatrix();
		}
	}
}
