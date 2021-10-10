package snownee.kaleido.chisel.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import snownee.kaleido.util.VoxelUtil;

public class LayersBlock extends DirectionalBlock {

	public static final IntegerProperty LAYERS = BlockStateProperties.LAYERS;
	public static final VoxelShape[][] SHAPES = new VoxelShape[6][8];

	public LayersBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.DOWN).setValue(LAYERS, 1));
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader p_220053_2_, BlockPos p_220053_3_, ISelectionContext p_220053_4_) {
		Direction direction = state.getValue(FACING).getOpposite();
		int layers = state.getValue(LAYERS);
		return getShape(direction, layers - 1);
	}

	private static VoxelShape getShape(Direction direction, int i) {
		if (SHAPES[direction.get3DDataValue()][i] == null) {
			if (direction == Direction.DOWN) {
				int height = i == 0 ? 1 : i * 2;
				SHAPES[direction.get3DDataValue()][i] = box(0, 0, 0, 16, height, 16);
			} else {
				SHAPES[direction.get3DDataValue()][i] = VoxelUtil.rotate(getShape(Direction.DOWN, i), direction);
			}
		}
		return SHAPES[direction.get3DDataValue()][i];
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction direction = context.getClickedFace();
		BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos());
		ItemStack stack = context.getItemInHand();
		if (blockstate.is(this) && matchesItem(context.getLevel(), context.getClickedPos(), stack)) {
			int i = blockstate.getValue(LAYERS) + 1;
			if (i <= 8) {
				return blockstate.setValue(LAYERS, i);
			} else {
				return getFullState(blockstate, context.getLevel(), context.getClickedPos());
			}
		}
		int i = 1;
		if (stack.hasTag())
			i = MathHelper.clamp(stack.getTag().getInt("Layers"), 1, 8);
		return defaultBlockState().setValue(FACING, direction).setValue(LAYERS, i);
	}

	public BlockState getFullState(BlockState blockstate, World level, BlockPos pos) {
		return null;
	}

	@Override
	public boolean canBeReplaced(BlockState state, BlockItemUseContext context) {
		ItemStack stack = context.getItemInHand();
		World level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		int i = state.getValue(LAYERS);
		if (i == 8 && getFullState(state, level, pos) == null)
			return false;
		if (stack.getItem() != asItem() || !matchesItem(context.getLevel(), context.getClickedPos(), stack))
			return false;
		if (context.replacingClickedOnBlock()) {
			return context.getClickedFace() == state.getValue(FACING);
		} else {
			return true;
		}
	}

	public boolean matchesItem(World level, BlockPos pos, ItemStack stack) {
		return true;
	}

	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> p_206840_1_) {
		p_206840_1_.add(FACING, LAYERS);
	}

	@Override //TODO
	public boolean isPathfindable(BlockState p_196266_1_, IBlockReader p_196266_2_, BlockPos p_196266_3_, PathType p_196266_4_) {
		return false;
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState p_220074_1_) {
		return true;
	}

}
