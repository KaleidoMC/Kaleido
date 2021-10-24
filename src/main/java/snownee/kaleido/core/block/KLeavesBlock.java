package snownee.kaleido.core.block;

import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kaleido.core.template.KaleidoTemplate;

// LeavesBlock
public class KLeavesBlock extends KHorizontalBlock {

	public KLeavesBlock(Properties builder) {
		super(builder);
	}

	@Override
	public KaleidoTemplate getTemplate() {
		return KaleidoTemplate.LEAVES;
	}

	@Override
	public VoxelShape getBlockSupportShape(BlockState pState, IBlockReader pReader, BlockPos pPos) {
		return VoxelShapes.empty();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState pState, World pLevel, BlockPos pPos, Random pRand) {
		if (pLevel.isRainingAt(pPos.above())) {
			if (pRand.nextInt(15) == 1) {
				BlockPos blockpos = pPos.below();
				BlockState blockstate = pLevel.getBlockState(blockpos);
				if (!blockstate.canOcclude() || !blockstate.isFaceSturdy(pLevel, blockpos, Direction.UP)) {
					double d0 = pPos.getX() + pRand.nextDouble();
					double d1 = pPos.getY() - 0.05D;
					double d2 = pPos.getZ() + pRand.nextDouble();
					pLevel.addParticle(ParticleTypes.DRIPPING_WATER, d0, d1, d2, 0.0D, 0.0D, 0.0D);
				}
			}
		}
	}

	@Override
	public int getLightBlock(BlockState pState, IBlockReader pLevel, BlockPos pPos) {
		return 1;
	}
}
