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
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kaleido.core.ModelInfo;

@OnlyIn(Dist.CLIENT)
public class StackButton extends Button {

    public static void renderItemIntoGUI(ItemRenderer renderer, ItemStack stack, int x, int y, int light) {
        renderItemModelIntoGUI(renderer, stack, x, y, renderer.getItemModelWithOverrides(stack, (World) null, (LivingEntity) null), light);
    }

    @SuppressWarnings("deprecation")
    public static void renderItemModelIntoGUI(ItemRenderer renderer, ItemStack stack, int x, int y, IBakedModel bakedmodel, int light) {
        RenderSystem.pushMatrix();
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        textureManager.getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).setBlurMipmapDirect(false, false);
        RenderSystem.enableRescaleNormal();
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.translatef(x, y, 100.0F + renderer.zLevel);
        RenderSystem.translatef(8.0F, 8.0F, 0.0F);
        RenderSystem.scalef(1.0F, -1.0F, 1.0F);
        RenderSystem.scalef(16.0F, 16.0F, 16.0F);
        MatrixStack matrixstack = new MatrixStack();
        IRenderTypeBuffer.Impl irendertypebuffer$impl = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        boolean flag = !bakedmodel.isSideLit();
        if (flag) {
            RenderHelper.setupGuiFlatDiffuseLighting();
        }

        renderer.renderItem(stack, ItemCameraTransforms.TransformType.GUI, false, matrixstack, irendertypebuffer$impl, light, OverlayTexture.NO_OVERLAY, bakedmodel);
        irendertypebuffer$impl.finish();
        RenderSystem.enableDepthTest();
        if (flag) {
            RenderHelper.setupGui3DDiffuseLighting();
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

    public StackButton(int x, int y, ModelInfo info, ItemStack stack, IPressable onPress, Button.ITooltip onTooltip) {
        super(x, y, 27, 27, stack.getDisplayName(), onPress, onTooltip);
        this.stack = stack;
        this.info = info;
        originalX = x;
        originalY = y;
        alpha = 0;
        if (info.reward) {
            frameColor = 0xFFDF00;
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void renderButton(MatrixStack matrix, int mouseX, int mouseY, float pTicks) {
        //        alpha = Math.min(alpha + pTicks * 0.2F, 1);
        //        int y = (int) (this.y + 15 - 15 * MathHelper.sin(alpha));
        AbstractGui.fill(matrix, x, y, x + width, y + height, 0xAA222222);
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
            FontRenderer font = Minecraft.getInstance().fontRenderer;
            font.drawStringWithShadow(matrix, "?", x + 11, y + 11, 0xFF888888);
            RenderSystem.popMatrix();
        }
    }
}
