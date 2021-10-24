package snownee.kaleido.hub;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import snownee.kaleido.Hooks;
import snownee.kaleido.hub.util.HTTPUtil;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;

@KiwiModule("hub")
@KiwiModule.Optional
public class HubModule extends AbstractModule {

	@Override
	protected void init(FMLCommonSetupEvent event) {
		Hooks.hubEnabled = true;
	}

	public static void fetch(String url) throws MalformedURLException, IOException {
		try (InputStreamReader reader = new InputStreamReader(new URL(url).openStream())) {
			HTTPUtil.downloadTo(null, url, null, 0, null, null);
		}
	}

}
