package snownee.kaleido.core.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kaleido.core.CoreModule;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.block.entity.MasterBlockEntity;

public class KHorizontalBlock extends HorizontalBlock {

	public KHorizontalBlock(Properties builder) {
		super(builder);
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
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new MasterBlockEntity();
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		return KaleidoBlocks.getPickBlock(state, target, world, pos, player);
	}

	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		return KaleidoBlocks.use(state, worldIn, pos, player, handIn, hit);
	}

	@Override
	public void attack(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer) {
		KaleidoBlocks.attack(pState, pLevel, pPos, pPlayer);
	}

	@Override
	public void onProjectileHit(World pLevel, BlockState pState, BlockRayTraceResult pHit, ProjectileEntity pProjectile) {
		KaleidoBlocks.onProjectileHit(pLevel, pState, pHit, pProjectile);
	}

	@Override
	public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		KaleidoBlocks.setPlacedBy(worldIn, pos, state, placer, stack);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return KaleidoBlocks.getShape(state, worldIn, pos, context);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return KaleidoBlocks.getCollisionShape(state, worldIn, pos, context);
	}

	@Override
	public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
		return KaleidoBlocks.getLightValue(state, world, pos);
	}

	@Override
	public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> items) {
		KaleidoBlocks.fillItemCategory(group, items);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean isPathfindable(BlockState p_196266_1_, IBlockReader p_196266_2_, BlockPos p_196266_3_, PathType p_196266_4_) {
		if (this == CoreModule.STUFF)
			return false;
		return super.isPathfindable(p_196266_1_, p_196266_2_, p_196266_3_, p_196266_4_);
	}

	@Override
	@SuppressWarnings("deprecation")
	@OnlyIn(Dist.CLIENT)
	public float getShadeBrightness(BlockState state, IBlockReader level, BlockPos pos) {
		if (state.is(CoreModule.STUFF)) {
			ModelInfo info = KaleidoBlocks.getInfo(level, pos);
			if (info != null && info.glass)
				return 1;
		}
		return super.getShadeBrightness(state, level, pos);
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, IBlockReader level, BlockPos pos) {
		if (state.is(CoreModule.STUFF)) {
			ModelInfo info = KaleidoBlocks.getInfo(level, pos);
			if (info != null && info.glass)
				return true;
		}
		return super.propagatesSkylightDown(state, level, pos);
	}

	@SuppressWarnings("deprecation")
	@Override
	public VoxelShape getVisualShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context) {
		if (state.is(CoreModule.STUFF)) {
			ModelInfo info = KaleidoBlocks.getInfo(level, pos);
			if (info != null && info.glass)
				return VoxelShapes.empty();
		}
		return super.getVisualShape(state, level, pos, context);
	}

	@Override
	public SoundType getSoundType(BlockState state, IWorldReader world, BlockPos pos, Entity entity) {
		return KaleidoBlocks.getSoundType(world, pos);
	}
}
