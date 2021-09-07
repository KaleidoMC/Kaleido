package snownee.kaleido.core.item;

import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import snownee.kaleido.core.CoreModule;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.block.MasterBlock;
import snownee.kaleido.core.util.KaleidoTemplate;
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

	@Nullable
	protected BlockState getPlacementState(BlockItemUseContext ctx) {
		ModelInfo info = MasterBlock.getInfo(ctx.getItemInHand());
		Block block = CoreModule.STUFF;
		if (info == null || info.template == KaleidoTemplate.item)
			return null;
		block = info.template.bloc;
		BlockState blockstate = block.getStateForPlacement(ctx);
		return blockstate != null && this.canPlace(ctx, blockstate) ? blockstate : null;
	}

	@Override
	public void registerBlocks(Map<Block, Item> map, Item item) {
		super.registerBlocks(map, item);
		map.put(CoreModule.HORIZONTAL, item);
	}

	@Override
	public void removeFromBlockToItemMap(Map<Block, Item> blockToItemMap, Item itemIn) {
		super.removeFromBlockToItemMap(blockToItemMap, itemIn);
		blockToItemMap.remove(CoreModule.HORIZONTAL);
	}

}
