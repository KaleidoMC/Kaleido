package snownee.kaleido.util;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;

import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

public class ShapeCache {

	private Instance block;
	private Instance empty;
	private final HashFunction hashFunction;
	private final Map<HashCode, Instance> map = Maps.newConcurrentMap();

	public ShapeCache(HashFunction hashFunction) {
		this.hashFunction = hashFunction;
		empty = put(newHasher().hash(), VoxelShapes.empty());
		Hasher hasher = newHasher();
		hasher.putDouble(0);
		hasher.putDouble(0);
		hasher.putDouble(0);
		hasher.putDouble(1);
		hasher.putDouble(1);
		hasher.putDouble(1);
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
			shapes[0] = shape;
			if (shape.max(Axis.X) > 0.75 || shape.max(Axis.Z) > 0.75 || shape.min(Axis.X) > 0.25 || shape.min(Axis.Z) > 0.25) {
				outOfBlock = true;
			}
		}

		public VoxelShape get(Direction direction) {
			int i = direction.get2DDataValue();
			if (shapes[i] == null) {
				shapes[i] = VoxelUtil.rotate(shapes[0], direction);
			}
			return shapes[i];
		}
	}

}
