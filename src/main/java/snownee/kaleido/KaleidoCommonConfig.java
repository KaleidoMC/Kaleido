package snownee.kaleido;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;

public final class KaleidoCommonConfig {

    public static boolean debug = true;

    private static BooleanValue debugVal;

    public static final ForgeConfigSpec spec;

    static {
        spec = new ForgeConfigSpec.Builder().configure(KaleidoCommonConfig::new).getRight();
    }

    @SubscribeEvent
    public static void onFileChange(ModConfig.Reloading event) {
        ((CommentedFileConfig) event.getConfig().getConfigData()).load();
        refresh();
    }

    public static void refresh() {
        debug = debugVal.get();
    }

    private KaleidoCommonConfig(ForgeConfigSpec.Builder builder) {
        debugVal = builder.define("debugMode", debug);
    }
}
