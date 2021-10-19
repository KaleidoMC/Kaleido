package snownee.kaleido.util.data;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;

import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import snownee.kaleido.util.VoxelUtil;

public class ShapeCache {

	private Instance block;
	private Instance empty;
	private final HashFunction hashFunction;
	private final Map<HashCode, Instance> map = Maps.newConcurrentMap();

	public ShapeCache(HashFunction hashFunction) {
		this.hashFunction = hashFunction;
		empty = put(newHasher().hash(), VoxelShapes.empty());
		Hasher hasher = newHasher();
		double[] shape = new double[] { 0, 0, 0, 1, 1, 1 };
		for (double d : shape)
			hasher.putDouble(d);
		block = put(hasher.hash(), VoxelShapes.block());
	}

	public Instance put(HashCode hashCode, VoxelShape shape) {
		return map.computeIfAbsent(hashCode, $ -> new Instance($, shape));
	}

	public Hasher newHasher() {
		return hashFunction.newHasher();
	}

	public Map<HashCode, Instance> getMap() {
		return map;
	}

	public Instance get(HashCode hashCode) {
		return map.getOrDefault(hashCode, empty);
	}

	public boolean has(HashCode hashCode) {
		return map.containsKey(hashCode);
	}

	public Instance empty() {
		return empty;
	}

	public Instance block() {
		return block;
	}

	public static class Instance {
		public final VoxelShape[] shapes = new VoxelShape[4];
		public final HashCode hashCode;
		public boolean outOfBlock;

		private Instance(HashCode hashCode, VoxelShape shape) {
			this.hashCode = hashCode;
			shapes[Direction.NORTH.get2DDataValue()] = shape;
			if (shape.max(Axis.X) > 0.75 || shape.max(Axis.Z) > 0.75 || shape.min(Axis.X) > 0.25 || shape.min(Axis.Z) > 0.25) {
				outOfBlock = true;
			}
		}

		public boolean isEmpty() {
			return shapes[Direction.NORTH.get2DDataValue()].isEmpty();
		}

		public VoxelShape get(Direction direction) {
			int i = direction.get2DDataValue();
			if (shapes[i] == null) {
				shapes[i] = VoxelUtil.rotateHorizontal(shapes[Direction.NORTH.get2DDataValue()], direction);
			}
			return shapes[i];
		}
	}

}
