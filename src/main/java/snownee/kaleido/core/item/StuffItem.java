package snownee.kaleido.core.item;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
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
import snownee.kaleido.core.behavior.Behavior;
import snownee.kaleido.core.behavior.FoodBehavior;
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

	public Food getFoodProperties(ItemStack stack) {
		ModelInfo info = KaleidoBlock.getInfo(stack);
		Behavior behavior = info.behaviors.get("food");
		return behavior == null ? null : ((FoodBehavior) behavior).food;
	}

	public boolean isEdible(ItemStack stack) {
		return getFoodProperties(stack) != null;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack pStack, World pLevel, LivingEntity pEntityLiving) {
		return isEdible(pStack) ? pEntityLiving.eat(pLevel, pStack) : pStack;
	}

	@Override
	public UseAction getUseAnimation(ItemStack pStack) {
		return isEdible(pStack) ? UseAction.EAT : UseAction.NONE;
	}

	@Override
	public int getUseDuration(ItemStack pStack) {
		Food food = getFoodProperties(pStack);
		if (food != null) {
			return food.isFastFood() ? 16 : 32;
		} else {
			return 0;
		}
	}

	@Override
	public ActionResult<ItemStack> use(World pLevel, PlayerEntity pPlayer, Hand pHand) {
		ItemStack stack = pPlayer.getItemInHand(pHand);
		Food food = getFoodProperties(stack);
		if (food != null) {
			if (pPlayer.canEat(food.canAlwaysEat())) {
				pPlayer.startUsingItem(pHand);
				return ActionResult.consume(stack);
			} else {
				return ActionResult.fail(stack);
			}
		} else {
			return ActionResult.pass(stack);
		}
	}

	@Override
	public ActionResultType useOn(ItemUseContext pContext) {
		ActionResultType actionresulttype = place(new BlockItemUseContext(pContext));
		return !actionresulttype.consumesAction() && isEdible(pContext.getItemInHand()) ? use(pContext.getLevel(), pContext.getPlayer(), pContext.getHand()).getResult() : actionresulttype;
	}

}
