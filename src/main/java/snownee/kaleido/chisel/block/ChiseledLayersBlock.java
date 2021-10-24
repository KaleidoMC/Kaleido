package snownee.kaleido.chisel.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import snownee.kaleido.chisel.block.entity.ChiseledBlockEntity;
import snownee.kaleido.core.definition.BlockDefinition;
import snownee.kiwi.block.ModBlock;

public class ChiseledLayersBlock extends LayersBlock implements ChiseledBlock {

	public ChiseledLayersBlock() {
		super(AbstractBlock.Properties.copy(Blocks.OAK_SLAB));
	}

	@Override
	public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hitResult) {
		return ChiseledBlock.use(state, level, pos, player, hand, hitResult);
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		ItemStack stack = ModBlock.pickBlock(state, target, world, pos, player);
		if (player.isCreative()) {
			int i = state.getValue(LAYERS);
			if (i > 1)
				stack.getOrCreateTag().putInt("Layers", i);
		}
		return stack;
	}

	@Override
	public boolean matchesItem(World level, BlockPos pos, ItemStack stack) {
		return ChiseledBlock.getSupplierIfSame(level, pos, stack) != null;
	}

	@Override
	public BlockState getFullState(BlockState blockstate, World level, BlockPos pos) {
		TileEntity blockEntity = level.getBlockEntity(pos);
		if (!(blockEntity instanceof ChiseledBlockEntity))
			return null;
		BlockDefinition supplier = ((ChiseledBlockEntity) blockEntity).getTexture();
		if (supplier == null)
			return null;
		return supplier.getBlockState();
	}
}
