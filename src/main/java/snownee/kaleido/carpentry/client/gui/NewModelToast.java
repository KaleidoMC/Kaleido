package snownee.kaleido.carpentry.client.gui;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NewModelToast implements IToast {
    private final List<ItemStack> stacks = Lists.newArrayList();
    private long firstDrawTime;
    private boolean hasNewOutputs;

    public NewModelToast(List<ItemStack> stacks) {
        this.stacks.addAll(stacks);
    }

    public void addStacks(List<ItemStack> stacks) {
        if (this.stacks.addAll(stacks)) {
            this.hasNewOutputs = true;
        }
    }

    public static void addOrUpdate(ToastGui toastGui, List<ItemStack> stacks) {
        NewModelToast toast = toastGui.getToast(NewModelToast.class, NO_TOKEN);
        if (toast == null) {
            toastGui.add(new NewModelToast(stacks));
        } else {
            toast.addStacks(stacks);
        }
    }

    @Override
    public Visibility draw(ToastGui toastGui, long delta) {
        if (this.hasNewOutputs) {
            this.firstDrawTime = delta;
            this.hasNewOutputs = false;
        }

        if (this.stacks.isEmpty()) {
            return IToast.Visibility.HIDE;
        } else {
            toastGui.getMinecraft().getTextureManager().bindTexture(TEXTURE_TOASTS);
            RenderSystem.color3f(1.0F, 1.0F, 1.0F);
            toastGui.blit(0, 0, 0, 32, 160, 32);
            toastGui.getMinecraft().fontRenderer.drawString(I18n.format("kaleido.toast.title"), 30.0F, 7.0F, -11534256);
            toastGui.getMinecraft().fontRenderer.drawString(I18n.format("kaleido.toast.description"), 30.0F, 18.0F, -16777216);
            ItemStack stack = this.stacks.get((int) ((delta * this.stacks.size() / 5000L) % this.stacks.size())); //Forge: fix math so that it doesn't divide by 0 when there are more than 5000 recipes
            toastGui.getMinecraft().getItemRenderer().renderItemAndEffectIntoGUI((LivingEntity) null, stack, 8, 8);
            return delta - this.firstDrawTime >= 5000L ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
        }
    }

}
