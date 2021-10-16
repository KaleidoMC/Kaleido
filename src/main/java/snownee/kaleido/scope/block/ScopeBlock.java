package snownee.kaleido.scope.block;

import javax.annotation.Nullable;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import snownee.kaleido.chisel.item.ChiselItem;
import snownee.kaleido.core.definition.BlockDefinition;
import snownee.kaleido.scope.ScopeModule;
import snownee.kaleido.scope.client.gui.ScopeScreen;
import snownee.kiwi.block.ModBlock;

public class ScopeBlock extends ModBlock implements IWaterLoggable {

	public ScopeBlock() {
		super(AbstractBlock.Properties.of(Material.BUILDABLE_GLASS).noCollission().strength(0.3F));
		registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.WATERLOGGED, false));
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
				Minecraft.getInstance().setScreen(new ScopeScreen((ScopeBlockEntity) blockEntity));
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
		if (!level.isClientSide) {
			((ScopeBlockEntity) blockEntity).addStack(definition, player);
		}
		return ActionResultType.sidedSuccess(level.isClientSide);
	}

	@Override
	public SoundType getSoundType(BlockState state, IWorldReader world, BlockPos pos, Entity entity) {
		TileEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof ScopeBlockEntity) {
			BlockDefinition definition = ((ScopeBlockEntity) blockEntity).getBlockDefinition();
			if (definition != null) {
				return definition.getSoundType();
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
		pBuilder.add(BlockStateProperties.WATERLOGGED);
	}

	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockItemUseContext pContext) {
		FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
		boolean flag = fluidstate.getType() == Fluids.WATER;
		return super.getStateForPlacement(pContext).setValue(BlockStateProperties.WATERLOGGED, flag);
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
}
