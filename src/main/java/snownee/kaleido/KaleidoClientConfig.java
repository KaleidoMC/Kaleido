package snownee.kaleido;

import net.minecraftforge.fml.config.ModConfig;
import snownee.kiwi.config.KiwiConfig;
import snownee.kiwi.config.KiwiConfig.Range;

@KiwiConfig(type = ModConfig.Type.CLIENT)
public final class KaleidoClientConfig {
	public static boolean previewAllBlocks = false;
	@Range(min = 0, max = 1)
	public static float previewAlpha = 0.5F;
	public static boolean previewEnabled = true;
	public static boolean showInJEI = true;
}
