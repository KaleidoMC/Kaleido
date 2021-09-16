package snownee.kaleido.compat.buildinggadgets;

import java.util.function.Supplier;

import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileDataFactory;
import com.direwolf20.buildinggadgets.common.tainted.registry.TopologicalRegistryBuilder;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.KiwiModule.Subscriber.Bus;

@KiwiModule(value = "buildinggadgets", dependencies = "buildinggadgets")
@KiwiModule.Subscriber(Bus.MOD)
public class BuildingGadgetsModule extends AbstractModule {

	public static final KaleidoTileData.Serializer SERIALIZER = new KaleidoTileData.Serializer();

	@SubscribeEvent
	public void register(InterModEnqueueEvent event) {
		TopologicalRegistryBuilder<ITileDataFactory> builder = TopologicalRegistryBuilder.create();
		builder.addValue(KaleidoTileData.ID, new KaleidoTileData.Factory());
		builder.addDependency(KaleidoTileData.ID, Reference.MARKER_AFTER_RL);
		InterModComms.sendTo(Reference.MODID, Reference.TileDataFactoryReference.IMC_METHOD_TILEDATA_FACTORY, (Supplier<Supplier<TopologicalRegistryBuilder<ITileDataFactory>>>) () -> () -> builder);
	}

}
