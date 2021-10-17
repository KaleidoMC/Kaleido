package snownee.kaleido.core.client;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.kaleido.mixin.MixinItemColors;

@OnlyIn(Dist.CLIENT)
public class ItemColorCache extends ColorCache<IItemColor> {

	@Override
	@Nullable
	IItemColor loadColor(String key) {
		Item item = parseItem(key);
		if (item != null) {
			ItemColors itemColors = Minecraft.getInstance().getItemColors();
			return ((MixinItemColors) itemColors).getItemColors().get(item.delegate);
		}
		return null;
	}

	@Override
	IItemColor loadConstant(int color) {
		return (a, b) -> color;
	}

	public int getColor(String key, ItemStack stack, int index) {
		return getColor(key, $ -> $.getColor(stack, index));
	}

	@Override
	public IItemColor fallback(String key, IItemColor colorProvider) {
		if (colorProvider instanceof Fallback) {
			return super.fallback(key, colorProvider);
		}
		Item item = parseItem(key);
		if (item != null) {
			return new Fallback(new ItemStack(item), colorProvider);
		}
		return super.fallback(key, colorProvider);
	}

	private static Item parseItem(String key) {
		ResourceLocation id = ResourceLocation.tryParse(key);
		if (id != null) {
			Item item = ForgeRegistries.ITEMS.getValue(id);
			if (item != Items.AIR) {
				return item;
			}
		}
		return null;
	}

	public static class Fallback implements IItemColor {

		private final ItemStack stack;
		private final IItemColor colorProvider;

		public Fallback(ItemStack stack, IItemColor colorProvider) {
			this.stack = stack;
			this.colorProvider = colorProvider;
		}

		@Override
		public int getColor(ItemStack stack, int index) {
			return colorProvider.getColor(this.stack, index);
		}

	}

}
