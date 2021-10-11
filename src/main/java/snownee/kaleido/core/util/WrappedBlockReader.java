package snownee.kaleido.core.util;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class WrappedBlockReader implements IBlockDisplayReader {

	protected IBlockDisplayReader delegate;

	public void setLevel(IBlockDisplayReader level) {
		this.delegate = level;
	}

	@Override
	public TileEntity getBlockEntity(BlockPos pos) {
		return delegate.getBlockEntity(pos);
	}

	@Override
	public BlockState getBlockState(BlockPos p_180495_1_) {
		return delegate.getBlockState(p_180495_1_);
	}

	@Override
	public FluidState getFluidState(BlockPos p_204610_1_) {
		return delegate.getFluidState(p_204610_1_);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public float getShade(Direction p_230487_1_, boolean p_230487_2_) {
		return delegate.getShade(p_230487_1_, p_230487_2_);
	}

	@Override
	public WorldLightManager getLightEngine() {
		return delegate.getLightEngine();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public int getBlockTint(BlockPos pos, ColorResolver colorResolver) {
		return delegate.getBlockTint(pos, colorResolver);
	}

}
