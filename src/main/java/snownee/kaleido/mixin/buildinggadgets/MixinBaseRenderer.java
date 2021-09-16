package snownee.kaleido.mixin.buildinggadgets;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.direwolf20.buildinggadgets.client.cache.RemoteInventoryCache;
import com.direwolf20.buildinggadgets.client.renders.BaseRenderer;
import com.direwolf20.buildinggadgets.common.world.MockBuilderWorld;

import snownee.kaleido.compat.buildinggadgets.BaseRendererAccessor;

@Mixin(value = BaseRenderer.class, remap = false)
public class MixinBaseRenderer implements BaseRendererAccessor {

	@Shadow
	private static RemoteInventoryCache cacheInventory;
	@Shadow
	private static MockBuilderWorld builderWorld;

	@Override
	public MockBuilderWorld kaleido_getBuilderWorld() {
		return builderWorld;
	}

	@Override
	public RemoteInventoryCache kaleido_getCacheInventory() {
		return cacheInventory;
	}
}
