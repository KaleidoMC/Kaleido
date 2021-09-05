package snownee.kaleido.util;

import java.util.function.Predicate;

import com.google.common.collect.Iterables;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.items.ItemHandlerHelper;
import snownee.kaleido.core.CoreModule;

public class KaleidoUtil {

	public static Ingredient COIN = Ingredient.of(CoreModule.CLOTH_TAG);

	public static int getCoins(PlayerEntity player) {
		int count = 0;
		for (ItemStack stack : getInv(player)) {
			if (CoreModule.CLOTH_TAG.contains(stack.getItem())) {
				count += stack.getCount();
			}
		}
		return count;
	}

	public static Iterable<ItemStack> getInv(PlayerEntity player) {
		return Iterables.concat(player.inventory.items, player.inventory.offhand);
	}

	public static void giveItems(PlayerEntity player, int amount, ItemStack stack) {
		while (amount > 0) {
			int size = Math.min(amount, stack.getMaxStackSize());
			ItemStack newStack = ItemHandlerHelper.copyStackWithSize(stack, size);
			ItemHandlerHelper.giveItemToPlayer(player, newStack);
			amount -= size;
		}
	}

	public static void takeCoins(PlayerEntity player, int amount) {
		takeItems(player, amount, COIN);
	}

	public static boolean takeItems(PlayerEntity player, int amount, Predicate<ItemStack> ingredient) {
		for (ItemStack stack : getInv(player)) {
			if (amount == 0) {
				return true;
			}
			if (ingredient.test(stack)) {
				int decr = Math.min(amount, stack.getCount());
				stack.shrink(decr);
				amount -= decr;
			}
		}
		return amount == 0;
	}

}
