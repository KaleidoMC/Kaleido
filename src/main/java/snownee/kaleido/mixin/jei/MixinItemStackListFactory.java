package snownee.kaleido.mixin.jei;

import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import mezz.jei.plugins.vanilla.ingredients.item.ItemStackListFactory;

@Mixin(value = ItemStackListFactory.class, remap = false)
public class MixinItemStackListFactory {

	@Redirect(
			at = @At(
					value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;)V"
			), method = "create"
	)
	private void kaleido_blockEmptyStackWarning(Logger logger, String s, Object o) {
		if (!"Found an empty itemStack from creative tab: {}".equals(s)) {
			logger.error(s, o);
		}
	}

}
