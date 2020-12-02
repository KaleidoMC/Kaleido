package snownee.kaleido;

import net.minecraftforge.fml.config.ModConfig;
import snownee.kiwi.config.KiwiConfig;

@KiwiConfig(type = ModConfig.Type.CLIENT)
public final class KaleidoClientConfig {
    public static boolean previewAllBlocks = false;
    public static float previewAlpha = 0.5F;
    public static boolean previewEnabled = true;
}
