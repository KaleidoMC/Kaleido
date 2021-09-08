package snownee.kaleido.core.item;

import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import snownee.kaleido.core.CoreModule;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.block.KaleidoBlocks;
import snownee.kaleido.core.util.KaleidoTemplate;
import snownee.kiwi.item.ModBlockItem;

public class StuffItem extends ModBlockItem {

	public StuffItem(Block block, Properties builder) {
		super(block, builder);
	}

	@Override
	public String getDescriptionId(ItemStack stack) {
		ModelInfo info = KaleidoBlocks.getInfo(stack);
		if (info != null) {
			return info.getDescriptionId();
		}
		return super.getDescriptionId(stack);
	}

	@Override
	public ITextComponent getName(ItemStack stack) {
		String descriptionId = getDescriptionId(stack);
		if (!I18n.exists(descriptionId)) {
			ModelInfo info = KaleidoBlocks.getInfo(stack);
			if (info != null) {
				return new StringTextComponent(info.id.getPath());
			}
		}
		return new TranslationTextComponent(descriptionId);
	}

	@Nullable
	protected BlockState getPlacementState(BlockItemUseContext ctx) {
		ModelInfo info = KaleidoBlocks.getInfo(ctx.getItemInHand());
		Block block = CoreModule.STUFF;
		if (info == null || info.template == KaleidoTemplate.item)
			return null;
		block = info.template.bloc;
		BlockState blockstate = block.getStateForPlacement(ctx);
		return blockstate != null && this.canPlace(ctx, blockstate) ? blockstate : null;
	}

	@Override
	public void registerBlocks(Map<Block, Item> map, Item item) {
		for (Block block : CoreModule.ALL_MASTER_BLOCKS) {
			map.put(block, item);
		}
	}

	@Override
	public void removeFromBlockToItemMap(Map<Block, Item> map, Item item) {
		for (Block block : CoreModule.ALL_MASTER_BLOCKS) {
			map.remove(block, item);
		}
	}

}
