package snownee.kaleido.scope.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import snownee.kaleido.core.definition.BlockDefinition;
import snownee.kaleido.scope.ScopeModule;
import snownee.kaleido.scope.client.gui.ScopeScreen;
import snownee.kiwi.block.ModBlock;

public class ScopeBlock extends ModBlock {

	public ScopeBlock() {
		super(AbstractBlock.Properties.of(Material.BUILDABLE_GLASS).noCollission());
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
		TileEntity blockEntity = level.getBlockEntity(pos);
		if (!(blockEntity instanceof ScopeBlockEntity)) {
			return ActionResultType.FAIL;
		}
		ItemStack stack = player.getItemInHand(hand);
		if (stack.isEmpty() && player.getOffhandItem().isEmpty()) {
			if (level.isClientSide) {
				Minecraft.getInstance().setScreen(new ScopeScreen((ScopeBlockEntity) blockEntity));
			}
			return ActionResultType.SUCCESS;
		}
		if (stack.getItem() == ScopeModule.SCOPE.asItem()) {
			return ActionResultType.PASS;
		}
		BlockItemUseContext context = new BlockItemUseContext(player, hand, stack, hitResult);
		BlockDefinition definition = BlockDefinition.fromItem(stack, context);
		if (definition == null) {
			return ActionResultType.PASS;
		}
		if (!level.isClientSide) {
			((ScopeBlockEntity) blockEntity).addStack(definition);
		}
		return ActionResultType.sidedSuccess(level.isClientSide);
	}

	@Override
	public SoundType getSoundType(BlockState state, IWorldReader world, BlockPos pos, Entity entity) {
		TileEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof ScopeBlockEntity) {
			BlockDefinition definition = ((ScopeBlockEntity) blockEntity).getBlockDefinition();
			if (definition != null) {
				return definition.getSoundType();
			}
		}
		return super.getSoundType(state, world, pos, entity);
	}

}
