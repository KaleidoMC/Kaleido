package snownee.kaleido.core.block;

import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import snownee.kaleido.core.template.KaleidoTemplate;

public class KStairsBlock extends StairsBlock implements KaleidoBlock {

	public KStairsBlock(Properties pProperties) {
		super(() -> Blocks.OAK_STAIRS.defaultBlockState(), pProperties);
		registerDefaultState(defaultBlockState().setValue(FACING, Direction.EAST));
	}

	@Override
	public KaleidoTemplate getTemplate() {
		return KaleidoTemplate.STAIRS;
	}

	@Override
	public void onRemove(BlockState pState, World pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
		if (pState.hasTileEntity() && (!pState.is(pNewState.getBlock()) || !pNewState.hasTileEntity())) {
			pLevel.removeBlockEntity(pPos);
		}
	}

	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		return KaleidoBlock.use(state, worldIn, pos, player, handIn, hit);
	}

	@Override
	public void attack(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer) {
		KaleidoBlock.attack(pState, pLevel, pPos, pPlayer);
	}

	@Override
	public void onProjectileHit(World pLevel, BlockState pState, BlockRayTraceResult pHit, ProjectileEntity pProjectile) {
		KaleidoBlock.onProjectileHit(pLevel, pState, pHit, pProjectile);
	}

	@Override
	public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		KaleidoBlock.setPlacedBy(worldIn, pos, state, placer, stack);
	}

	@Override
	public List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder) {
		return KaleidoBlock.getDrops(pState, pBuilder);
	}

}
