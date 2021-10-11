package snownee.kaleido.chisel.block;

import java.util.List;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kaleido.chisel.ChiselModule;
import snownee.kaleido.core.definition.BlockDefinition;
import snownee.kiwi.block.ModBlock;

public class ChiseledLayersBlock extends LayersBlock {

	public ChiseledLayersBlock() {
		super(AbstractBlock.Properties.copy(Blocks.STONE_SLAB));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return ChiselModule.CHISELED.create();
	}

	@Override
	public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hitResult) {
		return ChiseledBlocks.use(state, level, pos, player, hand, hitResult);
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		ItemStack stack = ModBlock.pickBlock(state, target, world, pos, player);
		int i = state.getValue(LAYERS);
		if (i > 1)
			stack.getOrCreateTag().putInt("Layers", i);
		return stack;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, IBlockReader level, List<ITextComponent> components, ITooltipFlag flag) {
		ChiseledBlocks.appendHoverText(stack, level, components, flag);
	}

	@Override
	public SoundType getSoundType(BlockState state, IWorldReader level, BlockPos pos, Entity entity) {
		return ChiseledBlocks.getSoundType(level, pos);
	}

	@Override
	public boolean matchesItem(World level, BlockPos pos, ItemStack stack) {
		return ChiseledBlocks.getSupplierIfSame(level, pos, stack) != null;
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
