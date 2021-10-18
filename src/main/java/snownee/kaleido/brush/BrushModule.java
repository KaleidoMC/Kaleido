package snownee.kaleido.brush;

import net.minecraft.client.renderer.color.ItemColors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import snownee.kaleido.brush.item.BrushItem;
import snownee.kaleido.core.client.KaleidoClient;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.KiwiModule.Subscriber.Bus;

@KiwiModule("brush")
@KiwiModule.Optional
@KiwiModule.Subscriber(Bus.MOD)
public class BrushModule extends AbstractModule {

	public static final BrushItem BRUSH = new BrushItem(itemProp());

	@Override
	protected void init(FMLCommonSetupEvent event) {
		MinecraftForge.EVENT_BUS.register(BrushItem.class);
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void itemColors(ColorHandlerEvent.Item event) {
		ItemColors itemColors = event.getItemColors();
		itemColors.register((stack, i) -> {
			if (i == 0 && stack.hasTag()) {
				String key = BrushItem.getTint(stack);
				if (key != null) {
					return KaleidoClient.ITEM_COLORS.getColor(key, stack, BrushItem.getIndex(stack));
				}
			}
			return -1;
		}, BRUSH);
	}
}
