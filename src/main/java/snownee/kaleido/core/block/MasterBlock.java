package snownee.kaleido.core.block;

import com.google.common.collect.Streams;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import snownee.kaleido.core.KaleidoDataManager;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.tile.MasterTile;
import snownee.kiwi.RenderLayer;
import snownee.kiwi.RenderLayer.Layer;
import snownee.kiwi.util.NBTHelper;
import snownee.kiwi.util.Util;

@RenderLayer(Layer.TRANSLUCENT)
public class MasterBlock extends HorizontalBlock {

    public static final BooleanProperty AO = BooleanProperty.create("ao");

    public static final String NBT_ID = "Kaleido.Id";

    public static ModelInfo getInfo(ItemStack stack) {
        NBTHelper data = NBTHelper.of(stack);
        ResourceLocation modelId = Util.RL(data.getString(NBT_ID));
        if (modelId == null || modelId.getPath().isEmpty()) {
            return null;
        }
        return KaleidoDataManager.INSTANCE.get(modelId);
    }

    public MasterBlock(Properties builder) {
        super(builder);
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new MasterTile();
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        KaleidoDataManager.INSTANCE.allPacks.values().stream().flatMap(pack -> Streams.concat(pack.normalInfos.stream(), pack.rewardInfos.stream())).map(ModelInfo::makeItem).forEach(items::add);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_FACING, AO);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        ItemStack stack = new ItemStack(this);
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof MasterTile) {
            ModelInfo info = ((MasterTile) tile).getModelInfo();
            if (info != null) {
                NBTHelper.of(stack).setString(NBT_ID, info.id.toString());
            }
        }
        return stack;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        ModelInfo info = getInfo(context.getItem());
        if (info == null) {
            return null;
        }
        Direction direction = context.getPlacementHorizontalFacing();
        if (info.opposite) {
            direction = direction.getOpposite();
        }
        return this.getDefaultState().with(HORIZONTAL_FACING, direction).with(AO, info.useAO);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof MasterTile) {
            return ((MasterTile) tile).behavior.onBlockActivated(state, worldIn, pos, player, handIn, hit);
        }
        return ActionResultType.PASS;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        ModelInfo info = getInfo(stack);
        if (info == null) {
            worldIn.destroyBlock(pos, true);
            return;
        }
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof MasterTile) {
            ((MasterTile) tile).setModelInfo(info);
            if (info.behavior.getLightValue() > 0) {
                worldIn.getLightManager().checkBlock(pos);
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        //worldIn.getTileEntity(pos);
        // TODO Auto-generated method stub
        return super.getShape(state, worldIn, pos, context);
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof MasterTile && ((MasterTile) tile).behavior != null) {
            return ((MasterTile) tile).behavior.getLightValue();
        }
        return super.getLightValue(state, world, pos);
    }

}
