package snownee.modname;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;

public final class ModNameCommonConfig {

    public static final ForgeConfigSpec spec;

    public static boolean debug = true;

    private static BooleanValue debugVal;

    static {
        spec = new ForgeConfigSpec.Builder().configure(ModNameCommonConfig::new).getRight();
    }

    private ModNameCommonConfig(ForgeConfigSpec.Builder builder) {
        debugVal = builder.define("debugMode", debug);
    }

    public static void refresh() {
        debug = debugVal.get();
    }

    @SubscribeEvent
    public static void onFileChange(ModConfig.Reloading event) {
        ((CommentedFileConfig) event.getConfig().getConfigData()).load();
        refresh();
    }
}
