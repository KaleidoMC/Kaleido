package snownee.kaleido.core.definition;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import snownee.kaleido.chisel.ChiselModule;
import snownee.kaleido.chisel.block.ChiseledBlockEntity;

public class DynamicBlockDefinition extends SimpleBlockDefinition {

	public static final String TYPE = "Dynamic";

	public static enum Factory implements BlockDefinition.Factory<DynamicBlockDefinition> {
		INSTANCE;

		@SuppressWarnings("deprecation")
		@Override
		public DynamicBlockDefinition fromNBT(CompoundNBT tag) {
			BlockState state = NBTUtil.readBlockState(tag.getCompound(SimpleBlockDefinition.TYPE));
			if (state.isAir())
				return null;
			CompoundNBT tileData = tag.getCompound("TileData");
			TileEntity blockEntity = TileEntity.loadStatic(state, tileData);
			if (blockEntity == null)
				return null;
			blockEntity.load(state, tileData);
			return new DynamicBlockDefinition(state, blockEntity);
		}

		@Override
		public DynamicBlockDefinition fromBlock(BlockState state, TileEntity blockEntity, IWorldReader level, BlockPos pos) {
			if (!(blockEntity instanceof ChiseledBlockEntity))
				return null; //TODO
			return new DynamicBlockDefinition(state, blockEntity);
		}

		@Override
		public DynamicBlockDefinition fromItem(ItemStack stack, BlockItemUseContext context) {
			CompoundNBT tag = stack.getTagElement("BlockEntityTag");
			if (tag == null || !ChiselModule.CHISELED_BLOCKS.contains(Block.byItem(stack.getItem())))
				return null; //TODO
			BlockState state = getStateForPlacement(stack, context);
			TileEntity blockEntity = state.createTileEntity(context.getLevel());
			if (blockEntity == null)
				return null;
			blockEntity.load(state, tag);
			return new DynamicBlockDefinition(state, blockEntity);
		}

		@Override
		public String getId() {
			return TYPE;
		}

	}

	public final TileEntity blockEntity;

	protected DynamicBlockDefinition(BlockState state, TileEntity blockEntity) {
		super(state);
		this.blockEntity = blockEntity;
	}

	@Override
	public BlockDefinition.Factory<?> getFactory() {
		return Factory.INSTANCE;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public IModelData modelData() {
		return blockEntity.getModelData();
	}

	@Override
	public void save(CompoundNBT tag) {
		super.save(tag);
		CompoundNBT tileData = blockEntity.save(new CompoundNBT());
		tileData.remove("x");
		tileData.remove("y");
		tileData.remove("z");
		tag.put("TileData", tileData);
	}

}
