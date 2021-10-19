package snownee.kaleido.util.data;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.gson.JsonElement;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.block.Block;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kaleido.core.KaleidoDataManager;

public class ShapeSerializer {

	private final ShapeCache shapeCache;

	public ShapeSerializer(ShapeCache shapeCache) {
		this.shapeCache = shapeCache;
	}

	public ShapeCache.Instance fromJson(JsonElement json) {
		String s = json.getAsString();
		if ("empty".equals(s))
			return shapeCache.empty();
		if ("block".equals(s))
			return shapeCache.block();
		DoubleList doubleList = new DoubleArrayList();
		Hasher hasher = shapeCache.newHasher();
		s = StringUtils.deleteWhitespace(s);
		Pattern pattern = Pattern.compile("\\((\\d*\\.?\\d*,\\d*\\.?\\d*,\\d*\\.?\\d*,\\d*\\.?\\d*,\\d*\\.?\\d*,\\d*\\.?\\d*)\\)");
		Matcher matcher = pattern.matcher(s);
		while (matcher.find()) {
			String numbers = matcher.group(1);
			double[] args = KaleidoDataManager.GSON.fromJson("[" + numbers + "]", double[].class);
			for (double arg : args) {
				doubleList.add(arg);
				hasher.putDouble(arg);
			}
		}
		HashCode hashCode = hasher.hash();
		if (shapeCache.has(hashCode)) {
			return shapeCache.get(hashCode);
		} else {
			VoxelShape shape = VoxelShapes.empty();
			for (int i = 0; i < doubleList.size(); i += 6) {
				shape = VoxelShapes.or(shape, Block.box(doubleList.getDouble(i), doubleList.getDouble(i + 1), doubleList.getDouble(i + 2), doubleList.getDouble(i + 3), doubleList.getDouble(i + 4), doubleList.getDouble(i + 5)));
			}
			shape = shape.optimize();
			return shapeCache.put(hashCode, shape);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static VoxelShape fromNetwork(PacketBuffer buf) {
		int size = buf.readVarInt();
		VoxelShape shape = VoxelShapes.empty();
		for (int i = 0; i < size; i++) {
			VoxelShape shape0 = VoxelShapes.box(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble());
			shape = VoxelShapes.or(shape, shape0);
		}
		return shape.optimize();
	}

	public static void toNetwork(PacketBuffer buf, VoxelShape shape) {
		List<AxisAlignedBB> aabbs = shape.toAabbs();
		buf.writeVarInt(aabbs.size());
		for (AxisAlignedBB aabb : aabbs) {
			buf.writeDouble(aabb.minX);
			buf.writeDouble(aabb.minY);
			buf.writeDouble(aabb.minZ);
			buf.writeDouble(aabb.maxX);
			buf.writeDouble(aabb.maxY);
			buf.writeDouble(aabb.maxZ);
		}
	}

}
