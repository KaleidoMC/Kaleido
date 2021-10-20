package snownee.kaleido.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Food;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import snownee.kaleido.core.CoreModule;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {

	@Inject(at = @At("HEAD"), method = "eat")
	private void kaleido_eat(World level, ItemStack stack, CallbackInfoReturnable<ItemStack> ci) {
		if (stack.getItem() == CoreModule.STUFF_ITEM && CoreModule.STUFF_ITEM.isEdible(stack)) {
			LivingEntity entity = (LivingEntity) (Object) this;
			level.playSound((PlayerEntity) null, entity.getX(), entity.getY(), entity.getZ(), entity.getEatingSound(stack), SoundCategory.NEUTRAL, 1.0F, 1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.4F);
			addEatEffect(stack, level, entity);
			if (!(entity instanceof PlayerEntity) || !((PlayerEntity) entity).abilities.instabuild) {
				stack.shrink(1);
			}
		}
	}

	@Shadow
	abstract void addEatEffect(ItemStack stack, World level, LivingEntity entity);

	@Inject(at = @At("HEAD"), method = "addEatEffect")
	private void kaleido_addEatEffect(ItemStack stack, World level, LivingEntity entity, CallbackInfo ci) {
		if (stack.getItem() != CoreModule.STUFF_ITEM)
			return;
		Food food = CoreModule.STUFF_ITEM.getFoodProperties(stack);
		if (food == null)
			return;
		for (Pair<EffectInstance, Float> pair : food.getEffects()) {
			if (!level.isClientSide && pair.getFirst() != null && level.random.nextFloat() < pair.getSecond()) {
				entity.addEffect(new EffectInstance(pair.getFirst()));
			}
		}
	}
}
