package snownee.kaleido.chisel.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class ChiseledStairsBlock extends StairsBlock implements ChiseledBlock {

	public ChiseledStairsBlock() {
		super(() -> Blocks.OAK_PLANKS.defaultBlockState(), AbstractBlock.Properties.copy(Blocks.OAK_STAIRS));
	}

	@Override
	public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hitResult) {
		return ChiseledBlock.use(state, level, pos, player, hand, hitResult);
	}

	@Override
	public void onRemove(BlockState pState, World pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
		if (pState.hasTileEntity() && (!pState.is(pNewState.getBlock()) || !pNewState.hasTileEntity())) {
			pLevel.removeBlockEntity(pPos);
		}
	}

}
