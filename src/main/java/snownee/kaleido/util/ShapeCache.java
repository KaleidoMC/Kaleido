package snownee.kaleido.util;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import net.minecraft.util.Direction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

public class ShapeCache {

	public static final HashCode BLOCK = HashCode.fromInt(0);
	public static final VoxelShape[] fallback = new VoxelShape[] { VoxelShapes.empty(), VoxelShapes.empty(), VoxelShapes.empty(), VoxelShapes.empty() };
	private final HashFunction hashFunction = Hashing.md5();
	private final Map<HashCode, VoxelShape[]> map = Maps.newConcurrentMap();

	public ShapeCache() {
		put(BLOCK, VoxelShapes.block());
	}

	public void put(HashCode hashCode, VoxelShape shape) {
		map.computeIfAbsent(hashCode, $ -> new VoxelShape[] { shape, null, null, null });
	}

	public Hasher newHasher() {
		return hashFunction.newHasher();
	}

	public Map<HashCode, VoxelShape[]> getMap() {
		return map;
	}

	public VoxelShape[] get(HashCode hashCode) {
		return map.getOrDefault(hashCode, fallback);
	}

	public void update(HashCode hashCode, Direction direction) {
		VoxelShape[] shape = get(hashCode);
		shape[direction.get2DDataValue()] = VoxelUtil.rotate(shape[0], direction);
	}

	public boolean has(HashCode hashCode) {
		return map.containsKey(hashCode);
	}

}
