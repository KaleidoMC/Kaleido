package snownee.kaleido.core.action;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface Action {
	void perform(World level, BlockPos pos, BlockState state);
}
