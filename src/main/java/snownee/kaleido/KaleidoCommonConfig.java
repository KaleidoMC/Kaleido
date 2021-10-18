package snownee.kaleido;

import java.util.Collections;
import java.util.List;

import snownee.kiwi.config.KiwiConfig;
import snownee.kiwi.config.KiwiConfig.Comment;
import snownee.kiwi.config.KiwiConfig.Path;
import snownee.kiwi.config.KiwiConfig.Range;

@KiwiConfig
public final class KaleidoCommonConfig {

	public static List<String> ignoredNamespaces = Collections.EMPTY_LIST;

	@Comment("Disable generating advancements to accelerate loading and save memories.")
	public static boolean disableAdvancements = false;

	@Path("carpentry.autoUnlock")
	@Comment("Only available if disableAdvancements is false")
	public static boolean autoUnlock = true; //TODO change to false

	public static boolean autoUnlock() {
		return !Hooks.carpentryEnabled || autoUnlock || disableAdvancements;
	}

	@Path("debugWorld.patch")
	public static boolean patchDebugWorld = true;
	@Path("debugWorld.spacing")
	@Range(min = 1, max = 10)
	public static int debugWorldSpacing = 3;

	@Path("scope.stackLimit")
	@Range(min = 1)
	public static int scopeStackLimit = 8;

}
