package snownee.kaleido.mallet;

import net.minecraft.block.Block;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import snownee.kaleido.Kaleido;
import snownee.kaleido.mallet.item.MalletItem;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.KiwiModule.Subscriber.Bus;

@KiwiModule("mallet")
@KiwiModule.Optional
@KiwiModule.Subscriber(Bus.MOD)
public class MalletModule extends AbstractModule {

	public static final MalletItem MALLET = new MalletItem(itemProp());

	public static final INamedTag<Block> MALLEABLE = blockTag(Kaleido.MODID, "malleable");

	@Override
	protected void init(FMLCommonSetupEvent event) {
		MinecraftForge.EVENT_BUS.register(MalletItem.class);
	}

}
