package snownee.kaleido.preview;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;

@Deprecated //TODO 1.17
@KiwiModule("preview")
@KiwiModule.Optional
public final class PreviewModule extends AbstractModule {

	@Override
	protected void clientInit(FMLClientSetupEvent event) {
		MinecraftForge.EVENT_BUS.register(PlacementPreview.class);
		ClientRegistry.registerKeyBinding(PlacementPreview.toggle);
	}

}
