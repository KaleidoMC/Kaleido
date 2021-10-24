package snownee.kaleido.util;

import java.util.Arrays;
import java.util.Comparator;

import net.minecraft.block.AbstractBlock.OffsetType;
import net.minecraft.util.Direction;

public class EnumUtil {
	public static final Direction[] DIRECTIONS = Direction.values();

	public static final Direction[] HORIZONTAL_DIRECTIONS = Arrays.stream(DIRECTIONS).filter($ -> {
		return $.getAxis().isHorizontal();
	}).sorted(Comparator.comparingInt($ -> {
		return $.get2DDataValue();
	})).toArray($ -> {
		return new Direction[$];
	});

	public static final OffsetType[] OFFSET_TYPES = OffsetType.values();
}
