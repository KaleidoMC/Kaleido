package snownee.kaleido.core.block;

import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import snownee.kaleido.core.template.KaleidoTemplate;

public class KRotatedPillarBlock extends RotatedPillarBlock implements KaleidoBlock {

	public KRotatedPillarBlock(Properties properties) {
		super(properties);
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
	public boolean isPathfindable(BlockState pState, IBlockReader pLevel, BlockPos pPos, PathType pType) {
		return false;
	}

	@Override
	public KaleidoTemplate getTemplate() {
		return KaleidoTemplate.PILLAR;
	}

	@Override
	public List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder) {
		return KaleidoBlock.getDrops(pState, pBuilder);
	}
}
