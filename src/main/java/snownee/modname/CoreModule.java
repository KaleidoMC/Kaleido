package snownee.modname;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;

@KiwiModule
public class CoreModule extends AbstractModule {

    public CoreModule() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ModNameCommonConfig.spec);
        modEventBus.register(ModNameCommonConfig.class);
        // if (FMLEnvironment.dist.isClient()) {
        //     ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ModNameClientConfig.spec);
        //     modEventBus.register(ModNameClientConfig.class);
        // }
    }

    @Override
    protected void init(FMLCommonSetupEvent event) {
        ModNameCommonConfig.refresh();
    }

}
