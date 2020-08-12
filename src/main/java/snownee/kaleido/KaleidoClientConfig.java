package snownee.kaleido;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;

public final class KaleidoClientConfig {

    public static boolean previewAllBlocks = true;

    private static BooleanValue previewAllBlocksVal;
    public static float previewAlpha = 0.5F;
    private static DoubleValue previewAlphaVal;

    public static boolean previewEnabled = true;
    private static BooleanValue previewEnabledVal;
    public static final ForgeConfigSpec spec;

    static {
        spec = new ForgeConfigSpec.Builder().configure(KaleidoClientConfig::new).getRight();
    }

    @SubscribeEvent
    public static void onFileChange(ModConfig.Reloading event) {
        ((CommentedFileConfig) event.getConfig().getConfigData()).load();
        refresh();
    }

    public static void refresh() {
        previewEnabled = previewEnabledVal.get().booleanValue();
        previewAllBlocks = previewAllBlocksVal.get().booleanValue();
        previewAlpha = previewAlphaVal.get().floatValue();
    }

    private KaleidoClientConfig(ForgeConfigSpec.Builder builder) {
        previewEnabledVal = builder.define("preview.enabled", previewEnabled);
        previewAllBlocksVal = builder.define("preview.allBlocks", previewAllBlocks);
        previewAlphaVal = builder.defineInRange("preview.alpha", 0.5, 0.0, 1.0);
    }
}
