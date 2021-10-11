package snownee.kaleido.chisel.block;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import snownee.kaleido.chisel.ChiselModule;
import snownee.kaleido.core.definition.BlockDefinition;
import snownee.kaleido.core.definition.SimpleBlockDefinition;

public class ChiseledBlockEntity extends RetextureBlockEntity {

	public ChiseledBlockEntity() {
		super(ChiselModule.CHISELED, "0");
	}

	@Override
	public CompoundNBT save(CompoundNBT p_189515_1_) {
		writePacketData(p_189515_1_);
		return super.save(p_189515_1_);
	}

	@Override
	public void load(BlockState p_230337_1_, CompoundNBT p_230337_2_) {
		readPacketData(p_230337_2_);
		super.load(p_230337_1_, p_230337_2_);
	}

	public boolean canOcclude() {
		for (BlockDefinition supplier : textures.values()) {
			if (supplier != null && !supplier.canOcclude()) {
				return false;
			}
		}
		return true;
	}

	public BlockDefinition getTexture() {
		return textures.get("0");
	}

	@Override
	public boolean isValidTexture(BlockDefinition modelSupplier) {
		if (modelSupplier instanceof SimpleBlockDefinition) {
			BlockState state = ((SimpleBlockDefinition) modelSupplier).state;
			if (ChiselModule.CHISELED_BLOCKS.contains(state.getBlock()))
				return false;
		}
		return true;
	}

}
