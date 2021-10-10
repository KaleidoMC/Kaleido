package snownee.kaleido;

import net.minecraftforge.fml.config.ModConfig;
import snownee.kiwi.config.KiwiConfig;
import snownee.kiwi.config.KiwiConfig.Path;
import snownee.kiwi.config.KiwiConfig.Range;

@KiwiConfig(type = ModConfig.Type.CLIENT)
public final class KaleidoClientConfig {
	@Path("preview.enabled")
	public static boolean previewEnabled = true;
	@Path("preview.previewAllBlocks")
	public static boolean previewAllBlocks = false;
	@Range(min = 0, max = 1)
	@Path("preview.alpha")
	public static float previewAlpha = 0.5F;
	public static boolean showInJEI = true;
}
