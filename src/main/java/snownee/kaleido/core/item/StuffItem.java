package snownee.kaleido.core.item;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kaleido.core.CoreModule;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.block.KaleidoBlock;
import snownee.kaleido.core.util.KaleidoTemplate;
import snownee.kiwi.item.ModBlockItem;

public class StuffItem extends ModBlockItem {

	public StuffItem(Block block, Properties builder) {
		super(block, builder);
	}

	@Override
	public String getDescriptionId(ItemStack stack) {
		ModelInfo info = KaleidoBlock.getInfo(stack);
		if (info != null) {
			return info.getDescription().getKey();
		}
		return super.getDescriptionId(stack);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (flagIn.isAdvanced()) {
			ModelInfo info = KaleidoBlock.getInfo(stack);
			if (info != null && info.group != null) {
				int i = info.group.infos.indexOf(info);
				tooltip.add(new TranslationTextComponent("tip.kaleido.group", info.group.id.toString(), i).withStyle(TextFormatting.GRAY));
			}
		}
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
	}

	@Override
	@Nullable
	protected BlockState getPlacementState(BlockItemUseContext ctx) {
		ModelInfo info = KaleidoBlock.getInfo(ctx.getItemInHand());
		Block block;
		if (info == null || info.template == KaleidoTemplate.item)
			return null;
		block = info.template.bloc;
		BlockState blockstate = block.getStateForPlacement(ctx);
		return blockstate != null && canPlace(ctx, blockstate) ? blockstate : null;
	}

	@Override
	protected boolean canPlace(BlockItemUseContext ctx, BlockState state) {
		ModelInfo info = KaleidoBlock.getInfo(ctx.getItemInHand());
		if (info != null) {
			if (info.noCollision)
				return true;
			BlockPos pos = ctx.getClickedPos();
			VoxelShape shape = info.getShape(state, pos);
			return ctx.getLevel().isUnobstructed(null, shape.move(pos.getX(), pos.getY(), pos.getZ()));
		}
		return super.canPlace(ctx, state);
	}

	@Override
	public void registerBlocks(Map<Block, Item> map, Item item) {
		for (Block block : CoreModule.MASTER_BLOCKS) {
			map.put(block, item);
		}
	}

	@Override
	public void removeFromBlockToItemMap(Map<Block, Item> map, Item item) {
		for (Block block : CoreModule.MASTER_BLOCKS) {
			map.remove(block, item);
		}
	}

}
