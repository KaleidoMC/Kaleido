package snownee.kaleido.core.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kaleido.core.CoreModule;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.template.KaleidoTemplate;

public class KHorizontalBlock extends HorizontalBlock implements KaleidoBlock {

	public KHorizontalBlock(Properties builder) {
		super(builder);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
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
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		if (getTemplate().allowsCustomShape()) {
			return KaleidoBlock.getShape(state, worldIn, pos, context);
		} else {
			return VoxelShapes.block();
		}
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		if (getTemplate().allowsCustomShape()) {
			return KaleidoBlock.getCollisionShape(state, worldIn, pos, context);
		} else {
			return VoxelShapes.block();
		}
	}

	@Override
	public boolean isPathfindable(BlockState pState, IBlockReader pLevel, BlockPos pPos, PathType pType) {
		return false;
	}

	@Override
	@SuppressWarnings("deprecation")
	@OnlyIn(Dist.CLIENT)
	public float getShadeBrightness(BlockState state, IBlockReader level, BlockPos pos) {
		if (!state.is(CoreModule.HORIZONTAL)) {
			ModelInfo info = KaleidoBlock.getInfo(level, pos);
			if (info != null && info.glass)
				return 1;
		}
		return super.getShadeBrightness(state, level, pos);
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, IBlockReader level, BlockPos pos) {
		if (!state.is(CoreModule.HORIZONTAL)) {
			ModelInfo info = KaleidoBlock.getInfo(level, pos);
			if (info != null && info.glass)
				return true;
		}
		return super.propagatesSkylightDown(state, level, pos);
	}

	@SuppressWarnings("deprecation")
	@Override
	public VoxelShape getVisualShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context) {
		if (!state.is(CoreModule.HORIZONTAL)) {
			ModelInfo info = KaleidoBlock.getInfo(level, pos);
			if (info != null && info.glass)
				return VoxelShapes.empty();
		}
		return super.getVisualShape(state, level, pos, context);
	}

	@Override
	public KaleidoTemplate getTemplate() {
		if (this == CoreModule.HORIZONTAL) {
			return KaleidoTemplate.HORIZONTAL;
		}
		return KaleidoTemplate.NONE;
	}
}
