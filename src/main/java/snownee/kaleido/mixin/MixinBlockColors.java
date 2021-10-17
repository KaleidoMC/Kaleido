package snownee.kaleido.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraftforge.registries.IRegistryDelegate;

@Mixin(BlockColors.class)
public interface MixinBlockColors {

	@Accessor
	Map<IRegistryDelegate<Block>, IBlockColor> getBlockColors();

}
