package snownee.kaleido.hammer;

import snownee.kaleido.hammer.item.HammerItem;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.KiwiModule.Subscriber.Bus;

@KiwiModule("hammer")
@KiwiModule.Optional
@KiwiModule.Subscriber(Bus.MOD)
public class HammerModule extends AbstractModule {

	public static final HammerItem HAMMER = new HammerItem(itemProp());

}
