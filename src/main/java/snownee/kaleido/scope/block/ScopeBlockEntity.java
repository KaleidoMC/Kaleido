package snownee.kaleido.scope.block;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import snownee.kaleido.scope.ScopeModule;
import snownee.kiwi.tile.BaseTile;

public class ScopeBlockEntity extends BaseTile {

	public ScopeBlockEntity() {
		super(ScopeModule.TILE);
	}

	@Override
	protected void readPacketData(CompoundNBT arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	protected CompoundNBT writePacketData(CompoundNBT data) {
		// TODO Auto-generated method stub
		return data;
	}

	public void addStack(BlockState state) {
		// TODO Auto-generated method stub

	}

}
