package snownee.kaleido.carpentry.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kaleido.carpentry.client.gui.CarpentryCraftingScreen;

public class WoodworkingBenchBlock extends HorizontalBlock {

    public WoodworkingBenchBlock(Properties builder) {
        super(builder);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing());
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (worldIn.isRemote) {
            openScreen(worldIn, pos, player);
        } else {
            //player.addStat(Stats.INTERACT_WITH_CRAFTING_TABLE); //TODO
        }
        return ActionResultType.SUCCESS;
    }

    @OnlyIn(Dist.CLIENT)
    private void openScreen(World worldIn, BlockPos pos, PlayerEntity player) {
        Minecraft.getInstance().displayGuiScreen(new CarpentryCraftingScreen(getNameTextComponent(), worldIn, pos));
    }

}
