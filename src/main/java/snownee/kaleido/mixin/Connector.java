package snownee.kaleido.mixin;

import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

import snownee.kaleido.Kaleido;

public class Connector implements IMixinConnector {

    @Override
    public void connect() {
        Kaleido.logger.info("Invoking Mixin Connector");
        Mixins.addConfiguration("assets/kaleido/kaleido.mixins.json");
    }

}
