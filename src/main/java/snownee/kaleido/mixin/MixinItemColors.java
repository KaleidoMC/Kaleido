package snownee.kaleido.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IRegistryDelegate;

@Mixin(ItemColors.class)
public interface MixinItemColors {

	@Accessor
	Map<IRegistryDelegate<Item>, IItemColor> getItemColors();

}
