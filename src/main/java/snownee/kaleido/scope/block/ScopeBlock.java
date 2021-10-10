package snownee.kaleido.scope.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import snownee.kaleido.scope.ScopeModule;
import snownee.kiwi.block.ModBlock;

public class ScopeBlock extends ModBlock {

	public ScopeBlock() {
		super(AbstractBlock.Properties.of(Material.STONE));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return ScopeModule.TILE.create();
	}

	@Override
	public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hitResult) {
		// TODO Auto-generated method stub
		return ActionResultType.SUCCESS;
	}

}
