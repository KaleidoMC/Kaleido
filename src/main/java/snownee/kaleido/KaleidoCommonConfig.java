package snownee.kaleido;

import java.util.Collections;
import java.util.List;

import snownee.kiwi.config.KiwiConfig;
import snownee.kiwi.config.KiwiConfig.Path;
import snownee.kiwi.config.KiwiConfig.Range;

@KiwiConfig
public final class KaleidoCommonConfig {

	public static List<String> ignoredNamespaces = Collections.EMPTY_LIST;
	public static boolean autoUnlock = true; //TODO change to false
	@Path("debugWorld.patch")
	public static boolean patchDebugWorld = true;
	@Path("debugWorld.spacing")
	@Range(min = 1, max = 10)
	public static int debugWorldSpacing = 3;

}
