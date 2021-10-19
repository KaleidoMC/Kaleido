package snownee.kaleido.chisel.block;

import java.util.List;
import java.util.Objects;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeBlock;
import snownee.kaleido.chisel.ChiselModule;
import snownee.kaleido.chisel.block.entity.ChiseledBlockEntity;
import snownee.kaleido.chisel.client.model.RetextureModel;
import snownee.kaleido.core.definition.BlockDefinition;
import snownee.kiwi.block.ModBlock;
import snownee.kiwi.util.NBTHelper;

public interface ChiseledBlock extends IForgeBlock {

	static ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hitResult) {
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
	static void appendHoverText(ItemStack stack, IBlockReader level, List<ITextComponent> components, ITooltipFlag flag) {
		NBTHelper data = NBTHelper.of(stack);
		CompoundNBT tag = data.getTag("BlockEntityTag.Overrides.0");
		BlockDefinition supplier = BlockDefinition.fromNBT(tag);
		if (supplier != null) {
			ITextComponent component = components.get(0);
			component = new TranslationTextComponent("block.kaleido.chiseled", component, supplier.getDescription());
			components.set(0, component);
		}
	}

	static BlockDefinition getSupplierIfSame(World level, BlockPos pos, ItemStack stack) {
		TileEntity blockEntity = level.getBlockEntity(pos);
		if (!(blockEntity instanceof ChiseledBlockEntity))
			return null;
		BlockDefinition supplier0 = ((ChiseledBlockEntity) blockEntity).getTexture();
		BlockDefinition supplier1 = RetextureModel.OverrideList.overridesFromItem(stack).get("0");
		return Objects.equals(supplier0, supplier1) ? supplier0 : null;
	}

	@Override
	default boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	default TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return ChiselModule.CHISELED.create();
	}

	@Override
	default ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		return ModBlock.pickBlock(state, target, world, pos, player);
	}

	@Override
	default SoundType getSoundType(BlockState state, IWorldReader level, BlockPos pos, Entity entity) {
		BlockDefinition supplier = null;
		TileEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof ChiseledBlockEntity)
			supplier = ((ChiseledBlockEntity) blockEntity).getTexture();
		return supplier == null ? SoundType.WOOD : supplier.getSoundType();
	}

	@Override
	default int getLightValue(BlockState state, IBlockReader level, BlockPos pos) {
		TileEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof ChiseledBlockEntity)
			return ((ChiseledBlockEntity) blockEntity).getLight();
		return 0;
	}

}
