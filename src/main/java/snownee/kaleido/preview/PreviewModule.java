package snownee.kaleido.preview;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.loading.FMLEnvironment;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;

@Deprecated //TODO 1.17
@KiwiModule("preview")
@KiwiModule.Optional
public final class PreviewModule extends AbstractModule {

	public PreviewModule() {
		if (FMLEnvironment.dist.isClient()) {
			MinecraftForge.EVENT_BUS.addListener(PlacementPreview::render);
		}
	}

}
