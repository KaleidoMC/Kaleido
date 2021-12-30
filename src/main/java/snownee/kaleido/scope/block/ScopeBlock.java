package snownee.kaleido.scope.block;

import javax.annotation.Nullable;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kaleido.chisel.item.ChiselItem;
import snownee.kaleido.core.definition.BlockDefinition;
import snownee.kaleido.scope.ScopeModule;
import snownee.kaleido.scope.client.gui.ScopeScreen;
import snownee.kiwi.block.ModBlock;

public class ScopeBlock extends HorizontalBlock implements IWaterLoggable {

	public ScopeBlock() {
		super(AbstractBlock.Properties.of(Material.BUILDABLE_GLASS).noCollission().strength(0.3F));
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(BlockStateProperties.WATERLOGGED, false));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return ScopeModule.TILE.create();
	}

	@Override
	public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hitResult) {
		TileEntity blockEntity = level.getBlockEntity(pos);
		if (!(blockEntity instanceof ScopeBlockEntity)) {
			return ActionResultType.FAIL;
		}
		if (player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty()) {
			if (level.isClientSide) {
				openScreen((ScopeBlockEntity) blockEntity);
			}
			return ActionResultType.SUCCESS;
		}
		ItemStack stack = player.getItemInHand(hand);
		if (stack.getItem() == ScopeModule.SCOPE.asItem()) {
			return ActionResultType.PASS;
		}
		BlockDefinition definition;
		if (stack.getItem() instanceof ChiselItem) {
			definition = BlockDefinition.fromNBT(stack.getTagElement("Def"));
		} else {
			BlockItemUseContext context = new BlockItemUseContext(player, hand, stack, hitResult);
			definition = BlockDefinition.fromItem(stack, context);
		}
		if (definition == null) {
			return ActionResultType.PASS;
		}
		Direction facing = state.getValue(FACING);
		Rotation rotation = rotFromNorth(facing);
		if (rotation != Rotation.NONE) {
			definition = definition.rotate(rotation);
		}
		if (!level.isClientSide) {
			((ScopeBlockEntity) blockEntity).addStack(definition, player);
		}
		return ActionResultType.sidedSuccess(level.isClientSide);
	}

	public static Rotation rotFromNorth(Direction facing) {
		switch (facing) {
		case SOUTH:
			return Rotation.CLOCKWISE_180;
		case WEST:
			return Rotation.CLOCKWISE_90;
		case EAST:
			return Rotation.COUNTERCLOCKWISE_90;
		default:
			return Rotation.NONE;
		}
	}

	@OnlyIn(Dist.CLIENT)
	private static void openScreen(ScopeBlockEntity blockEntity) {
		Minecraft.getInstance().setScreen(new ScopeScreen(blockEntity));
	}

	@Override
	public SoundType getSoundType(BlockState state, IWorldReader world, BlockPos pos, Entity entity) {
		TileEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof ScopeBlockEntity) {
			BlockDefinition definition = ((ScopeBlockEntity) blockEntity).getBlockDefinition();
			if (definition != null) {
				return BlockDefinition.getCamo(definition).getSoundType();
			}
		}
		return SoundType.GLASS;
	}

	@Override
	public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
		playerWillDestroy(world, pos, state, player);
		TileEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof ScopeBlockEntity) {
			ScopeBlockEntity scope = (ScopeBlockEntity) blockEntity;
			if (scope.fromLevel) {
				return scope.getBlockDefinition().place(world, pos);
			}
		}
		return world.setBlock(pos, fluid.createLegacyBlock(), world.isClientSide ? 11 : 3);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		pBuilder.add(FACING, BlockStateProperties.WATERLOGGED);
	}

	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockItemUseContext pContext) {
		FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
		boolean flag = fluidstate.getType() == Fluids.WATER;
		return defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, flag).setValue(FACING, pContext.getHorizontalDirection());
	}

	@Override
	public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, IWorld pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
		if (pState.getValue(BlockStateProperties.WATERLOGGED)) {
			pLevel.getLiquidTicks().scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
		}
		return pState;
	}

	@Override
	@SuppressWarnings("deprecation")
	public FluidState getFluidState(BlockState pState) {
		return pState.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
	}

	@Override
	public boolean isPathfindable(BlockState pState, IBlockReader pLevel, BlockPos pPos, PathType pType) {
		return false;
	}

	@Override
	public BlockState rotate(BlockState p_185499_1_, Rotation p_185499_2_) {
		return p_185499_1_.setValue(FACING, p_185499_2_.rotate(p_185499_1_.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState p_185471_1_, Mirror p_185471_2_) {
		return p_185471_1_.setValue(FACING, p_185471_2_.mirror(p_185471_1_.getValue(FACING)));
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		return ModBlock.pickBlock(state, target, world, pos, player);
	}

}
