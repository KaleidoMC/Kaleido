package snownee.kaleido.core.block;

import com.google.common.collect.Streams;

import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import snownee.kaleido.core.CoreModule;
import snownee.kaleido.core.KaleidoDataManager;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.block.entity.MasterBlockEntity;
import snownee.kiwi.util.NBTHelper;
import snownee.kiwi.util.Util;

public final class KaleidoBlocks {

	public static final String NBT_ID = "Kaleido.Id";

	public static ModelInfo getInfo(ItemStack stack) {
		if (stack.getItem() != CoreModule.STUFF_ITEM)
			return null;
		NBTHelper data = NBTHelper.of(stack);
		ResourceLocation modelId = Util.RL(data.getString(NBT_ID));
		if (modelId == null || modelId.getPath().isEmpty()) {
			return null;
		}
		return KaleidoDataManager.get(modelId);
	}

	public static ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		ItemStack stack = new ItemStack(CoreModule.STUFF_ITEM);
		TileEntity tile = world.getBlockEntity(pos);
		if (tile instanceof MasterBlockEntity) {
			ModelInfo info = ((MasterBlockEntity) tile).getModelInfo();
			if (info != null) {
				NBTHelper.of(stack).setString(NBT_ID, info.id.toString());
			}
		}
		return stack;
	}

	public static int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
		TileEntity tile = world.getBlockEntity(pos);
		if (tile instanceof MasterBlockEntity) {
			return ((MasterBlockEntity) tile).getLightValue();
		}
		return 0;
	}

	public static void setPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		ModelInfo info = getInfo(stack);
		if (info == null) {
			worldIn.destroyBlock(pos, true);
			return;
		}
		TileEntity tile = worldIn.getBlockEntity(pos);
		if (tile instanceof MasterBlockEntity) {
			((MasterBlockEntity) tile).setModelInfo(info);
			if (((MasterBlockEntity) tile).getLightValue() > 0) {
				worldIn.getLightEngine().checkBlock(pos);
			}
		}
	}

	public static ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		TileEntity tile = worldIn.getBlockEntity(pos);
		if (tile instanceof MasterBlockEntity) {
			return ((MasterBlockEntity) tile).use(state, worldIn, pos, player, handIn, hit);
		}
		return ActionResultType.PASS;
	}

	public static VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		if (state.getBlock() != CoreModule.STUFF) {
			return VoxelShapes.block();
		}
		TileEntity tile = worldIn.getBlockEntity(pos);
		if (tile instanceof MasterBlockEntity) {
			ModelInfo info = ((MasterBlockEntity) tile).getModelInfo();
			if (info != null) {
				VoxelShape shape = info.getShape(state.getValue(HorizontalBlock.FACING));
				if (!shape.isEmpty()) {
					return shape;
				}
			}
		}
		return VoxelShapes.block();
	}

	public static VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		if (state.getBlock() != CoreModule.STUFF) {
			return VoxelShapes.block();
		}
		TileEntity tile = worldIn.getBlockEntity(pos);
		if (tile instanceof MasterBlockEntity) {
			ModelInfo info = ((MasterBlockEntity) tile).getModelInfo();
			if (info != null && !info.noCollision)
				return info.getShape(state.getValue(HorizontalBlock.FACING));
		}
		return VoxelShapes.empty();
	}

	public static void fillItemCategory(ItemGroup group, NonNullList<ItemStack> items) {
		KaleidoDataManager.INSTANCE.allPacks.values().stream().flatMap(pack -> Streams.concat(pack.normalInfos.stream(), pack.rewardInfos.stream())).map(ModelInfo::makeItem).forEach(items::add);
	}
}
