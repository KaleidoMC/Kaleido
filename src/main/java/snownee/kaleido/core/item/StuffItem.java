package snownee.kaleido.core.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.block.MasterBlock;
import snownee.kiwi.item.ModBlockItem;

public class StuffItem extends ModBlockItem {

	public StuffItem(Block block, Properties builder) {
		super(block, builder);
	}

	@Override
	public String getDescriptionId(ItemStack stack) {
		ModelInfo info = MasterBlock.getInfo(stack);
		if (info != null) {
			return info.getDescriptionId();
		}
		return super.getDescriptionId(stack);
	}

}
