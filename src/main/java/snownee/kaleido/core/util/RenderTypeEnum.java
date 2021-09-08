package snownee.kaleido.core.util;

import java.util.function.Supplier;

import net.minecraft.client.renderer.RenderType;

public enum RenderTypeEnum {
	/* off */
	solid(()->RenderType::solid),
	cutout(()->RenderType::cutout),
	cutoutMipped(()->RenderType::cutoutMipped),
	translucent(()->RenderType::translucent);
	/* on */

	public static final RenderTypeEnum[] VALUES = values();
	public Supplier<Supplier<RenderType>> renderType;

	RenderTypeEnum(Supplier<Supplier<RenderType>> renderType) {
		this.renderType = renderType;
	}

}
