package snownee.kaleido.compat.buildinggadgets;

import com.direwolf20.buildinggadgets.client.cache.RemoteInventoryCache;
import com.direwolf20.buildinggadgets.common.world.MockBuilderWorld;

public interface BaseRendererAccessor {

	MockBuilderWorld kaleido_getBuilderWorld();

	RemoteInventoryCache kaleido_getCacheInventory();

}
