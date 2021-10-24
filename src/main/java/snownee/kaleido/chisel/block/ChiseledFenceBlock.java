package snownee.kaleido.chisel.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FenceBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class ChiseledFenceBlock extends FenceBlock implements ChiseledBlock {

	public ChiseledFenceBlock() {
		super(AbstractBlock.Properties.copy(Blocks.OAK_FENCE));
	}

	@Override
	public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hitResult) {
		return ChiseledBlock.use(state, level, pos, player, hand, hitResult);
	}

}
