package snownee.kaleido.chisel.block;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kaleido.core.definition.BlockDefinition;

public class ChiseledSlabBlock extends SlabBlock implements ChiseledBlock {

	public ChiseledSlabBlock() {
		super(AbstractBlock.Properties.copy(Blocks.OAK_SLAB));
	}

	@Override
	public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hitResult) {
		return ChiseledBlock.use(state, level, pos, player, hand, hitResult);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, IBlockReader level, List<ITextComponent> components, ITooltipFlag flag) {
		ChiseledBlock.appendHoverText(stack, level, components, flag);
	}

	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockPos blockpos = context.getClickedPos();
		World level = context.getLevel();
		BlockState blockstate = level.getBlockState(blockpos);
		if (blockstate.is(this)) {
			BlockDefinition supplier = ChiseledBlock.getSupplierIfSame(level, blockpos, context.getItemInHand());
			if (supplier == null)
				return null;
			return supplier.getBlockState();
		}
		FluidState fluidstate = context.getLevel().getFluidState(blockpos);
		BlockState blockstate1 = defaultBlockState().setValue(TYPE, SlabType.BOTTOM).setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
		Direction direction = context.getClickedFace();
		return direction != Direction.DOWN && (direction == Direction.UP || (context.getClickLocation().y - blockpos.getY() <= 0.5D)) ? blockstate1 : blockstate1.setValue(TYPE, SlabType.TOP);
	}

	@Override
	public boolean canBeReplaced(BlockState state, BlockItemUseContext context) {
		ItemStack itemstack = context.getItemInHand();
		SlabType slabtype = state.getValue(TYPE);
		if (slabtype != SlabType.DOUBLE && itemstack.getItem() == asItem() && ChiseledBlock.getSupplierIfSame(context.getLevel(), context.getClickedPos(), itemstack) != null) {
			if (context.replacingClickedOnBlock()) {
				boolean flag = context.getClickLocation().y - context.getClickedPos().getY() > 0.5D;
				Direction direction = context.getClickedFace();
				if (slabtype == SlabType.BOTTOM) {
					return direction == Direction.UP || flag && direction.getAxis().isHorizontal();
				} else {
					return direction == Direction.DOWN || !flag && direction.getAxis().isHorizontal();
				}
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

}
