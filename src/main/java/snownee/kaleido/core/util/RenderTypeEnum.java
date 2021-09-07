package snownee.kaleido.core.util;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.renderer.RenderType;

public enum RenderTypeEnum {
	/* off */
	solid(RenderType.solid()),
	cutout(RenderType.cutout()),
	cutoutMipped(RenderType.cutoutMipped()),
	translucent(RenderType.translucent());
	/* on */

	public static final RenderTypeEnum[] VALUES = values();
	private static final Map<RenderType, RenderTypeEnum> map = Maps.newHashMap();
	public RenderType renderType;

	private RenderTypeEnum(RenderType renderType) {
		this.renderType = renderType;
	}

	static {
		for (RenderTypeEnum e : VALUES) {
			map.put(e.renderType, e);
		}
	}

	public static RenderTypeEnum fromType(RenderType type) {
		return map.get(type);
	}
}
