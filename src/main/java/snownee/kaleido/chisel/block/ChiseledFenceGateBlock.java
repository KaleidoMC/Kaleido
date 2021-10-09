package snownee.kaleido.chisel.block;

import java.util.List;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.client.util.ITooltipFlag;
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
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kaleido.chisel.ChiselModule;
import snownee.kiwi.block.ModBlock;

public class ChiseledFenceGateBlock extends FenceGateBlock {

	public ChiseledFenceGateBlock() {
		super(AbstractBlock.Properties.copy(Blocks.OAK_FENCE_GATE));
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
		ActionResultType resultType = ChiseledBlocks.use(state, level, pos, player, hand, hitResult);
		if (!resultType.consumesAction()) {
			resultType = super.use(state, level, pos, player, hand, hitResult);
		}
		return resultType;
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		return ModBlock.pickBlock(state, target, world, pos, player);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, IBlockReader level, List<ITextComponent> components, ITooltipFlag flag) {
		ChiseledBlocks.appendHoverText(stack, level, components, flag);
	}
}
