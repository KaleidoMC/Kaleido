package snownee.kaleido.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import snownee.kaleido.Hooks;
import snownee.kaleido.chisel.item.ChiselItem;

@Mixin(ItemRenderer.class)
public class MixinItemRenderer {
	@Inject(
			at = @At(
				"TAIL"
			), method = "renderGuiItemDecorations(Lnet/minecraft/client/gui/FontRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V"
	)
	public void kaleido_renderGuiItemDecorations(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, @Nullable String text, CallbackInfo info) {
		if (Hooks.chiselEnabled && stack.getItem() instanceof ChiselItem)
			Hooks.renderChiselOverlay(stack, xPosition, yPosition);
	}
}
