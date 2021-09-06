package snownee.kaleido.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.ResourceLocation;
import snownee.kaleido.core.client.KaleidoClient;

@Mixin(ModelBakery.class)
public abstract class MixinModelBakery {

	@Inject(at = @At("HEAD"), method = "processLoading", remap = false)
	private void kaleido_OnProcessLoading(IProfiler p_i226056_3_, int p_i226056_4_, CallbackInfo ci) {
		KaleidoClient.registerModels(this::addModelToCache);
	}

	@Shadow(remap = false)
	abstract void addModelToCache(ResourceLocation p_217843_1_);
}
