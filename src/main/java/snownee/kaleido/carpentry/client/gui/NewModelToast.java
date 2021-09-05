package snownee.kaleido.carpentry.client.gui;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
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
			hasNewOutputs = true;
		}
	}

	public static void addOrUpdate(ToastGui toastGui, List<ItemStack> stacks) {
		NewModelToast toast = toastGui.getToast(NewModelToast.class, NO_TOKEN);
		if (toast == null) {
			toastGui.addToast(new NewModelToast(stacks));
		} else {
			toast.addStacks(stacks);
		}
	}

	@Override
	public Visibility render(MatrixStack matrix, ToastGui toastGui, long delta) {
		if (hasNewOutputs) {
			firstDrawTime = delta;
			hasNewOutputs = false;
		}

		if (stacks.isEmpty()) {
			return IToast.Visibility.HIDE;
		} else {
			toastGui.getMinecraft().getTextureManager().bind(TEXTURE);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			toastGui.blit(matrix, 0, 0, 0, 32, 160, 32);
			toastGui.getMinecraft().font.draw(matrix, I18n.get("kaleido.toast.title"), 30.0F, 7.0F, -11534256);
			toastGui.getMinecraft().font.draw(matrix, I18n.get("kaleido.toast.description"), 30.0F, 18.0F, -16777216);
			ItemStack stack = stacks.get((int) ((delta * stacks.size() / 5000L) % stacks.size())); //Forge: fix math so that it doesn't divide by 0 when there are more than 5000 recipes
			toastGui.getMinecraft().getItemRenderer().renderAndDecorateItem((LivingEntity) null, stack, 8, 8);
			return delta - firstDrawTime >= 5000L ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
		}
	}

}
