package snownee.kaleido.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.gson.JsonElement;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.block.Block;
import net.minecraft.network.PacketBuffer;
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

	public HashCode fromJson(JsonElement json) {
		String s = json.getAsString();
		if ("empty".equals(s))
			return null;
		if ("block".equals(s))
			return ShapeCache.BLOCK;
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
		if (!shapeCache.has(hashCode)) {
			VoxelShape shape = VoxelShapes.empty();
			for (int i = 0; i < doubleList.size(); i += 6) {
				shape = VoxelShapes.or(shape, Block.box(doubleList.getDouble(i), doubleList.getDouble(i + 1), doubleList.getDouble(i + 2), doubleList.getDouble(i + 3), doubleList.getDouble(i + 4), doubleList.getDouble(i + 5)));
			}
			shape = shape.optimize();
			shapeCache.put(hashCode, shape);
		}
		return hashCode;
	}

	@OnlyIn(Dist.CLIENT)
	public static VoxelShape fromNetwork(ByteBuf buf) {
		return null;
	}

	public static void toNetwork(PacketBuffer buf, VoxelShape shape) {
	}

}
