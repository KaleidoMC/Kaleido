package snownee.kaleido.scope.block;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import snownee.kaleido.core.definition.BlockDefinition;
import snownee.kaleido.scope.ScopeModule;
import snownee.kaleido.scope.ScopeStack;
import snownee.kaleido.scope.client.model.ScopeModel;
import snownee.kiwi.tile.BaseTile;
import snownee.kiwi.util.NBTHelper.NBT;

public class ScopeBlockEntity extends BaseTile {

	public List<ScopeStack> stacks = Lists.newArrayList();
	private IModelData modelData = EmptyModelData.INSTANCE;

	public ScopeBlockEntity() {
		super(ScopeModule.TILE);
		persistData = true;
	}

	@Override
	protected void readPacketData(CompoundNBT data) {
		stacks.clear();
		for (INBT tag : data.getList("Stacks", NBT.COMPOUND)) {
			ScopeStack stack = ScopeStack.load((CompoundNBT) tag);
			if (stack != null)
				stacks.add(stack);
		}
		if (hasLevel() && level.isClientSide)
			refresh();
	}

	@Override
	protected CompoundNBT writePacketData(CompoundNBT data) {
		ListNBT list = new ListNBT();
		for (ScopeStack stack : stacks) {
			CompoundNBT tag = new CompoundNBT();
			stack.save(tag);
			list.add(tag);
		}
		data.put("Stacks", list);
		return data;
	}

	@Override
	public void load(BlockState state, CompoundNBT data) {
		readPacketData(data);
		super.load(state, data);
	}

	@Override
	public CompoundNBT save(CompoundNBT data) {
		writePacketData(data);
		return super.save(data);
	}

	public ScopeStack addStack(BlockDefinition definition) {
		ScopeStack stack = new ScopeStack(definition);
		stacks.add(stack);
		refresh();
		return stack;
	}

	@Override
	public IModelData getModelData() {
		if (modelData == EmptyModelData.INSTANCE && EffectiveSide.get().isClient()) {
			modelData = new ModelDataMap.Builder().withInitial(ScopeModel.STACKS, stacks).build();
		}
		return modelData;
	}

	@Override
	public void refresh() {
		super.refresh();
	}

	@Override
	public void requestModelDataUpdate() {
		super.requestModelDataUpdate();
		if (!remove && level != null && level.isClientSide) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 8);
		}
	}

	public BlockDefinition getBlockDefinition() {
		return stacks.isEmpty() ? null : stacks.get(0).blockDefinition;
	}

}
