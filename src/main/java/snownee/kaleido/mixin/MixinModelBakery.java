package snownee.kaleido.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.ResourceLocation;
import snownee.kaleido.Hooks;
import snownee.kaleido.core.client.KaleidoClient;

@Mixin(ModelBakery.class)
public abstract class MixinModelBakery {

	@Shadow
	private Map<ResourceLocation, IUnbakedModel> unbakedCache;
	@Shadow
	private Map<ResourceLocation, IUnbakedModel> topLevelModels;

	// addModelToCache
	@Inject(at = @At("HEAD"), method = "processLoading", remap = false)
	private void kaleido_OnProcessLoading(IProfiler p_i226056_3_, int p_i226056_4_, CallbackInfo ci) {
		KaleidoClient.registerModels($ -> {
			IUnbakedModel unbakedModel = getModel($);
			if (unbakedModel == null)
				return;
			if (unbakedModel instanceof BlockModel) {
				Hooks.forceTransforms(l -> getModel(l), (BlockModel) unbakedModel);
			}
			unbakedCache.put($, unbakedModel);
			topLevelModels.put($, unbakedModel);
		});
	}

	@Shadow
	abstract IUnbakedModel getModel(ResourceLocation $);

}
