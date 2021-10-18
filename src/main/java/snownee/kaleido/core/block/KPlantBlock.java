package snownee.kaleido.core.block;

import net.minecraft.block.BlockState;
import net.minecraft.pathfinding.PathType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import snownee.kaleido.core.util.KaleidoTemplate;

public class KPlantBlock extends KHorizontalBlock implements KaleidoBlock {

	public KPlantBlock(Properties builder) {
		super(builder);
	}

	@Override
	public KaleidoTemplate getTemplate() {
		return KaleidoTemplate.plant;
	}

	@Override
	public boolean propagatesSkylightDown(BlockState pState, IBlockReader pReader, BlockPos pPos) {
		return pState.getFluidState().isEmpty();
	}

	@Override
	public boolean isPathfindable(BlockState pState, IBlockReader pLevel, BlockPos pPos, PathType pType) {
		return pType == PathType.AIR && !hasCollision ? true : super.isPathfindable(pState, pLevel, pPos, pType);
	}
}
