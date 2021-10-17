package snownee.kaleido.core.client;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.ToIntFunction;

import javax.annotation.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import moe.mmf.csscolors.Color;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class ColorCache<T> {

	public final Cache<String, T> cache;
	public final Int2ObjectMap<T> constants = new Int2ObjectOpenHashMap<>();

	public ColorCache(Cache<String, T> cache) {
		this.cache = cache;
		createConstant(-1);
	}

	public ColorCache() {
		this(CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build());
	}

	public int getColor(String key, ToIntFunction<T> function) {
		if (key == null) {
			return -1;
		}
		T colorProvider;
		try {
			colorProvider = cache.get(key, () -> _loadColor(key));
		} catch (ExecutionException e) {
			e.printStackTrace();
			return -1;
		}
		int color = -1;
		try {
			color = function.applyAsInt(colorProvider);
		} catch (Throwable e) {
			cache.put(key, fallback(key, colorProvider));
			return getColor(key, function);
		}
		return color;
	}

	public T createConstant(int color) {
		return constants.computeIfAbsent(color, $ -> loadConstant($));
	}

	private T _loadColor(String key) {
		T itemColor = null;
		Color color = Color.fromString(key);
		if (color == null) {
			itemColor = loadColor(key);
		} else {
			int i = color.toInt();
			T constant = constants.get(i);
			itemColor = constant == null ? loadConstant(i) : constant;
		}
		if (itemColor == null) {
			itemColor = constants.get(-1);
		}
		return itemColor;
	}

	public T fallback(String key, T colorProvider) {
		return constants.get(-1);
	}

	abstract T loadConstant(int color);

	@Nullable
	abstract T loadColor(String key);

}
