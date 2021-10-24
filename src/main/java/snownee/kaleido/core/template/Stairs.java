package snownee.kaleido.core.template;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.StairsShape;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kaleido.core.ModelInfo;

public class Stairs extends KaleidoTemplate {

	public Stairs(String name, Block block, boolean solid, int metaCount) {
		super(name, block, solid, metaCount);
	}

	@Override
	protected int _toMeta(BlockState state) {
		state = transform(state);
		int meta = state.getValue(StairsBlock.HALF).ordinal() * 3 * 4;
		meta += state.getValue(StairsBlock.FACING).get2DDataValue() * 3;
		meta += Shape.of(state.getValue(StairsBlock.SHAPE)).ordinal();
		return meta;
	}

	@Override
	@Nullable
	protected BlockState _fromMeta(int meta) {
		Shape shape = Shape.values()[meta % 3];
		Direction direction = Direction.from2DDataValue(meta / 3);
		Half half = Half.values()[meta / 12 % 2];
		return getBlock().defaultBlockState().setValue(StairsBlock.SHAPE, shape.example()).setValue(StairsBlock.FACING, direction).setValue(StairsBlock.HALF, half);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ResourceLocation getModelLocation(ModelInfo info, BlockState state) {
		String path = "kaleido/" + info.id.getPath();
		Shape shape = Shape.of(state.getValue(StairsBlock.SHAPE));
		if (shape == Shape.INNER) {
			path += "_inner";
		} else if (shape == Shape.OUTER) {
			path += "_outer";
		}
		return new ResourceLocation(info.id.getNamespace(), path);
	}

	public static BlockState transform(BlockState state) {
		Direction direction = state.getValue(StairsBlock.FACING);
		StairsShape shape = state.getValue(StairsBlock.SHAPE);
		boolean top = state.getValue(StairsBlock.HALF) == Half.TOP;
		Shape shape2 = Shape.of(shape);
		if (shape2 == Shape.INNER) {
			if ((shape == StairsShape.INNER_LEFT) != top) {
				shape = top ? StairsShape.INNER_LEFT : StairsShape.INNER_RIGHT;
				direction = top ? direction.getClockWise() : direction.getCounterClockWise();
			}
		} else if (shape2 == Shape.OUTER) {
			if ((shape == StairsShape.OUTER_LEFT) != top) {
				shape = top ? StairsShape.OUTER_LEFT : StairsShape.OUTER_RIGHT;
				direction = top ? direction.getClockWise() : direction.getCounterClockWise();
			}
		}
		return state.setValue(StairsBlock.FACING, direction).setValue(StairsBlock.SHAPE, shape);
	}

	public enum Shape {
		STRAIGHT, INNER, OUTER;

		public static Shape of(StairsShape shape) {
			switch (shape) {
			default:
				return STRAIGHT;
			case INNER_LEFT:
			case INNER_RIGHT:
				return INNER;
			case OUTER_LEFT:
			case OUTER_RIGHT:
				return OUTER;
			}
		}

		public StairsShape example() {
			switch (this) {
			case INNER:
				return StairsShape.INNER_RIGHT;
			case OUTER:
				return StairsShape.OUTER_RIGHT;
			default:
				return StairsShape.STRAIGHT;
			}
		}
	}
}
