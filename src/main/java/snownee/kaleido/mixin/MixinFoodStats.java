package snownee.kaleido.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.FoodStats;
import snownee.kaleido.core.CoreModule;

@Mixin(FoodStats.class)
public class MixinFoodStats {

	@Inject(at = @At("HEAD"), method = "eat(Lnet/minecraft/item/Item;Lnet/minecraft/item/ItemStack;)V")
	private void eat(Item pItem, ItemStack pStack, CallbackInfo ci) {
		if (pItem != CoreModule.STUFF_ITEM)
			return;
		Food food = CoreModule.STUFF_ITEM.getFoodProperties(pStack);
		if (food == null)
			return;
		((FoodStats) (Object) this).eat(food.getNutrition(), food.getSaturationModifier());
	}
}
