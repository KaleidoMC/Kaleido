package snownee.kaleido.core.client;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.kaleido.mixin.MixinBlockColors;

@OnlyIn(Dist.CLIENT)
public class BlockColorCache extends ColorCache<IBlockColor> {

	@Override
	IBlockColor loadConstant(int color) {
		return (a, b, c, d) -> color;
	}

	@Override
	IBlockColor loadColor(String key) {
		IBlockColor blockColor = null;
		Block block = parseBlock(key);
		if (block != null) {
			BlockColors blockColors = Minecraft.getInstance().getBlockColors();
			blockColor = ((MixinBlockColors) blockColors).getBlockColors().get(block.delegate);
		}
		return blockColor;
	}

	public int getColor(String key, BlockState state, IBlockDisplayReader level, BlockPos pos, int index) {
		return getColor(key, $ -> $.getColor(state, level, pos, index));
	}

	@Override
	public IBlockColor fallback(String key, IBlockColor colorProvider) {
		if (colorProvider instanceof Fallback) {
			return super.fallback(key, colorProvider);
		}
		Block block = parseBlock(key);
		if (block != null) {
			return new Fallback(block.defaultBlockState(), colorProvider);
		}
		return super.fallback(key, colorProvider);
	}

	private static Block parseBlock(String key) {
		ResourceLocation id = ResourceLocation.tryParse(key);
		if (id != null) {
			Block block = ForgeRegistries.BLOCKS.getValue(id);
			if (block != Blocks.AIR) {
				return block;
			}
		}
		return null;
	}

	public static class Fallback implements IBlockColor {

		private final BlockState state;
		private final IBlockColor colorProvider;

		public Fallback(BlockState state, IBlockColor colorProvider) {
			this.state = state;
			this.colorProvider = colorProvider;
		}

		@Override
		public int getColor(BlockState state, IBlockDisplayReader level, BlockPos pos, int index) {
			return colorProvider.getColor(this.state, level, pos, index);
		}

	}
}
