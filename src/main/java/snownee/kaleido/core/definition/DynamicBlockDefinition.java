package snownee.kaleido.core.definition;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import snownee.kaleido.Hooks;
import snownee.kaleido.chisel.ChiselModule;
import snownee.kaleido.chisel.block.entity.ChiseledBlockEntity;
import snownee.kaleido.scope.ScopeModule;
import snownee.kaleido.scope.block.ScopeBlockEntity;

public class DynamicBlockDefinition extends SimpleBlockDefinition {

	public static final String TYPE = "Dynamic";

	public enum Factory implements BlockDefinition.Factory<DynamicBlockDefinition> {
		INSTANCE;

		@SuppressWarnings("deprecation")
		@Override
		public DynamicBlockDefinition fromNBT(CompoundNBT tag) {
			BlockState state = NBTUtil.readBlockState(tag.getCompound(SimpleBlockDefinition.TYPE));
			if (state.isAir() || !state.hasTileEntity())
				return null;
			CompoundNBT tileData = tag.getCompound("TileData");
			TileEntity blockEntity = TileEntity.loadStatic(state, tileData);
			if (blockEntity == null)
				return null;
			return new DynamicBlockDefinition(state, blockEntity);
		}

		@Override
		public DynamicBlockDefinition fromBlock(BlockState state, TileEntity blockEntity, IWorldReader level, BlockPos pos) {
			if (!(blockEntity instanceof ChiseledBlockEntity) && !(blockEntity instanceof ScopeBlockEntity))
				return null; //TODO
			return new DynamicBlockDefinition(state, blockEntity);
		}

		@Override
		public DynamicBlockDefinition fromItem(ItemStack stack, BlockItemUseContext context) {
			CompoundNBT tag = stack.getTagElement("BlockEntityTag");
			if (tag == null || !acceptItem(stack.getItem()))
				return null; //TODO
			BlockState state = getStateForPlacement(stack, context);
			TileEntity blockEntity = state.createTileEntity(context.getLevel());
			if (blockEntity == null)
				return null;
			blockEntity.load(state, tag);
			return new DynamicBlockDefinition(state, blockEntity);
		}

		public boolean acceptItem(Item item) {
			if (Hooks.chiselEnabled && ChiselModule.CHISELED_BLOCKS.contains(Block.byItem(item))) {
				return true;
			}
			if (Hooks.scopeEnabled && item == ScopeModule.SCOPE.asItem()) {
				return true;
			}
			return false;
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

	@Override
	public boolean place(World level, BlockPos pos) {
		if (!super.place(level, pos))
			return false;
		TileEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity != null) {
			CompoundNBT tileData = this.blockEntity.save(new CompoundNBT());
			tileData.putInt("x", pos.getX());
			tileData.putInt("y", pos.getY());
			tileData.putInt("z", pos.getZ());
			blockEntity.load(getBlockState(), tileData);
			return true;
		}
		return false;
	}

	@Override
	public BlockDefinition getCamoDefinition() {
		if (blockEntity instanceof ChiseledBlockEntity) {
			return ((ChiseledBlockEntity) blockEntity).getTexture();
		}
		if (blockEntity instanceof ScopeBlockEntity) {
			return ((ScopeBlockEntity) blockEntity).getBlockDefinition();
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	@Override
	public BlockDefinition rotate(Rotation rotation) {
		return new DynamicBlockDefinition(getBlockState().rotate(rotation), blockEntity);
	}

	@Override
	public BlockDefinition mirror(Mirror mirror) {
		return new DynamicBlockDefinition(getBlockState().mirror(mirror), blockEntity);
	}

}
