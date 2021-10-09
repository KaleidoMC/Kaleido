package snownee.kaleido.chisel.block;

import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kaleido.core.supplier.ModelSupplier;
import snownee.kiwi.util.NBTHelper;

public final class ChiseledBlocks {

	public static ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hitResult) {
		//		ItemStack stack = player.getItemInHand(hand);
		//		if (!(stack.getItem() instanceof BlockItem)) {
		//			return ActionResultType.PASS;
		//		}
		//		BlockItemUseContext context = new BlockItemUseContext(player, hand, stack, hitResult);
		//		BlockState state2 = ((BlockItem) stack.getItem()).getBlock().getStateForPlacement(context);
		//		if (state2 == null) {
		//			return ActionResultType.FAIL;
		//		}
		//		if (!Block.isShapeFullBlock(state2.getOcclusionShape(level, pos))) {
		//			return ActionResultType.PASS;
		//		}
		//		if (!level.isClientSide) {
		//			TileEntity tile = level.getBlockEntity(pos);
		//			if (tile instanceof RetextureBlockEntity) {
		//				RetextureBlockEntity textureTile = (RetextureBlockEntity) tile;
		//				ModelInfo info = KaleidoBlocks.getInfo(stack);
		//				ModelSupplier supplier;
		//				if (info == null) {
		//					supplier = BlockStateModelSupplier.of(state2);
		//				} else {
		//					supplier = KaleidoModelSupplier.of(info, info.template.toMeta(state2));
		//				}
		//				textureTile.setTexture("0", supplier);
		//				textureTile.refresh();
		//			}
		//		}
		//		return ActionResultType.sidedSuccess(level.isClientSide);
		return ActionResultType.PASS;
	}

	@OnlyIn(Dist.CLIENT)
	public static void appendHoverText(ItemStack stack, IBlockReader level, List<ITextComponent> components, ITooltipFlag flag) {
		NBTHelper data = NBTHelper.of(stack);
		CompoundNBT tag = data.getTag("BlockEntityTag.Overrides.0");
		if (tag != null) {
			ModelSupplier supplier = ModelSupplier.fromNBT(tag);
			if (supplier != null) {
				ITextComponent component = components.get(0);
				component = new TranslationTextComponent("block.kaleido.chiseled", component, supplier.getDescription());
				components.set(0, component);
			}
		}
	}

}
