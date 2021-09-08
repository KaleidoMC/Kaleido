package snownee.kaleido.core.item;

import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
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
	@Nullable
	protected BlockState getPlacementState(BlockItemUseContext ctx) {
		ModelInfo info = KaleidoBlocks.getInfo(ctx.getItemInHand());
		Block block;
		if (info == null || info.template == KaleidoTemplate.item)
			return null;
		block = info.template.bloc;
		BlockState blockstate = block.getStateForPlacement(ctx);
		return blockstate != null && canPlace(ctx, blockstate) ? blockstate : null;
	}

	@Override
	protected boolean canPlace(BlockItemUseContext ctx, BlockState state) {
		ModelInfo info = KaleidoBlocks.getInfo(ctx.getItemInHand());
		if (info != null && info.template == KaleidoTemplate.none) {
			if (info.noCollision)
				return true;
			VoxelShape shape = info.getShape(ctx.getHorizontalDirection());
			BlockPos pos = ctx.getClickedPos();
			return ctx.getLevel().isUnobstructed(null, shape.move(pos.getX(), pos.getY(), pos.getZ()));
		}
		return super.canPlace(ctx, state);
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
