package snownee.kaleido.util;

import java.util.function.Function;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import snownee.kaleido.mixin.ChunkRenderCacheAccessor;

public abstract class WrappedBlockReader implements IBlockDisplayReader {

	protected IBlockDisplayReader delegate;

	public void setLevel(IBlockDisplayReader level) {
		delegate = level;
	}

	@Override
	public TileEntity getBlockEntity(BlockPos pos) {
		return safeRun($ -> $.getBlockEntity(pos));
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		return safeRun($ -> $.getBlockState(pos));
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return safeRun($ -> $.getFluidState(pos));
	}

	public <T> T safeRun(Function<IBlockDisplayReader, T> func) {
		if (FMLEnvironment.dist.isClient()) {
			try {
				return func.apply(delegate);
			} catch (ArrayIndexOutOfBoundsException e) {
				// https://github.com/mekanism/Mekanism/issues/5792
				// https://github.com/mekanism/Mekanism/issues/5844
				if (delegate instanceof ChunkRenderCache) {
					return func.apply(((ChunkRenderCacheAccessor) delegate).getLevel());
				}
			}
			return null;
		} else {
			return func.apply(delegate);
		}
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
