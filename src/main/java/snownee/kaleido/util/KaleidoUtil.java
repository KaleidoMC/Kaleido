package snownee.kaleido.util;

import java.util.function.Predicate;

import com.google.common.collect.Iterables;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.items.ItemHandlerHelper;
import snownee.kaleido.core.CoreModule;

public class KaleidoUtil {

	public static Lazy<Ingredient> COIN = Lazy.of(() -> Ingredient.of(CoreModule.CLOTH_TAG));

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
		if (player.isCreative()) {
			ItemStack held = player.getMainHandItem();
			if (!held.equals(stack, true)) {
				int size = Math.min(amount, stack.getMaxStackSize());
				ItemStack newStack = ItemHandlerHelper.copyStackWithSize(stack, size);
				player.setItemInHand(Hand.MAIN_HAND, newStack);
				ItemHandlerHelper.giveItemToPlayer(player, held);
				amount -= size;
			}
		}
		while (amount > 0) {
			int size = Math.min(amount, stack.getMaxStackSize());
			ItemStack newStack = ItemHandlerHelper.copyStackWithSize(stack, size);
			ItemHandlerHelper.giveItemToPlayer(player, newStack);
			amount -= size;
		}
	}

	public static void takeCoins(PlayerEntity player, int amount) {
		takeItems(player, amount, COIN.get());
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

	public static int friendlyCompare(String a, String b) {
		int aLength = a.length();
		int bLength = b.length();
		int minSize = Math.min(aLength, bLength);
		char aChar, bChar;
		boolean aNumber, bNumber;
		boolean asNumeric = false;
		int lastNumericCompare = 0;
		for (int i = 0; i < minSize; i++) {
			aChar = a.charAt(i);
			bChar = b.charAt(i);
			aNumber = aChar >= '0' && aChar <= '9';
			bNumber = bChar >= '0' && bChar <= '9';
			if (asNumeric)
				if (aNumber && bNumber) {
					if (lastNumericCompare == 0)
						lastNumericCompare = aChar - bChar;
				} else if (aNumber)
					return 1;
				else if (bNumber)
					return -1;
				else if (lastNumericCompare == 0) {
					if (aChar != bChar)
						return aChar - bChar;
					asNumeric = false;
				} else
					return lastNumericCompare;
			else if (aNumber && bNumber) {
				asNumeric = true;
				if (lastNumericCompare == 0)
					lastNumericCompare = aChar - bChar;
			} else if (aChar != bChar)
				return aChar - bChar;
		}
		if (asNumeric)
			if (aLength > bLength && a.charAt(bLength) >= '0' && a.charAt(bLength) <= '9') // as number
				return 1; // a has bigger size, thus b is smaller
			else if (bLength > aLength && b.charAt(aLength) >= '0' && b.charAt(aLength) <= '9') // as number
				return -1; // b has bigger size, thus a is smaller
			else if (lastNumericCompare == 0)
				return aLength - bLength;
			else
				return lastNumericCompare;
		else
			return aLength - bLength;
	}

	public static boolean canPlayerBreak(PlayerEntity player, BlockState state, BlockPos pos) {
		if (!player.mayBuild() || !player.level.mayInteract(player, pos)) {
			return false;
		}
		if (!player.isCreative() && state.getDestroyProgress(player, player.level, pos) <= 0) {
			return false;
		}
		BreakEvent event = new BreakEvent(player.level, pos, state, player);
		if (MinecraftForge.EVENT_BUS.post(event)) {
			return false;
		}
		return true;
	}
}
