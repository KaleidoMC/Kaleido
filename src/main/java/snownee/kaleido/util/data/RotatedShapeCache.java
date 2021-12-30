package snownee.kaleido.util.data;

import java.util.Arrays;
import java.util.Collections;
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

public class RotatedShapeCache {

	private Instance block;
	private Instance empty;
	private final HashFunction hashFunction;
	private final Map<HashCode, Instance> map = Maps.newConcurrentMap();

	public RotatedShapeCache(HashFunction hashFunction) {
		this.hashFunction = hashFunction;
		init();
	}

	public Instance put(HashCode hashCode, VoxelShape shape) {
		return map.computeIfAbsent(hashCode, $ -> new Instance($, shape));
	}

	public void init() {
		empty = put(newHasher().hash(), VoxelShapes.empty());
		Hasher hasher = newHasher();
		double[] shape = new double[] { 0, 0, 0, 16, 16, 16 };
		for (double d : shape)
			hasher.putDouble(d);
		block = put(hasher.hash(), VoxelShapes.block());
	}

	public void clear() {
		map.clear();
		init();
	}

	public Hasher newHasher() {
		return hashFunction.newHasher();
	}

	public Map<HashCode, Instance> getMap() {
		return Collections.unmodifiableMap(map);
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
		public VoxelShape[] shapes = new VoxelShape[4];
		public final HashCode hashCode;
		public boolean outOfBlock; // if random offset applies to this shape, it will out of box

		private Instance(HashCode hashCode, VoxelShape shape) {
			this.hashCode = hashCode;
			shapes[index(Direction.NORTH)] = shape;
			if (shape.max(Axis.X) > 0.75 || shape.max(Axis.Z) > 0.75 || shape.min(Axis.X) > 0.25 || shape.min(Axis.Z) > 0.25) {
				outOfBlock = true;
			}
		}

		public boolean isEmpty() {
			return shapes[index(Direction.NORTH)].isEmpty();
		}

		public VoxelShape get(Direction direction) {
			int i = index(direction);
			if (i >= shapes.length) {
				shapes = Arrays.copyOf(shapes, 6);
			}
			if (shapes[i] == null) {
				shapes[i] = VoxelUtil.rotateHorizontal(shapes[index(Direction.NORTH)], direction);
			}
			return shapes[i];
		}

		public int index(Direction direction) {
			if (direction == Direction.UP)
				return 4;
			if (direction == Direction.DOWN)
				return 5;
			return direction.get2DDataValue();
		}
	}

}
