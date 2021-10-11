package snownee.kaleido.scope.block;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import snownee.kaleido.core.definition.BlockDefinition;
import snownee.kaleido.scope.ScopeModule;
import snownee.kaleido.scope.ScopeStack;
import snownee.kiwi.tile.BaseTile;
import snownee.kiwi.util.NBTHelper.NBT;

public class ScopeBlockEntity extends BaseTile {

	public List<ScopeStack> stacks = Lists.newArrayList();

	public ScopeBlockEntity() {
		super(ScopeModule.TILE);
	}

	@Override
	protected void readPacketData(CompoundNBT data) {
		stacks.clear();
		for (INBT tag : data.getList("Stacks", NBT.COMPOUND)) {
			ScopeStack stack = ScopeStack.load((CompoundNBT) tag);
			if (stack != null)
				stacks.add(stack);
		}
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

}
