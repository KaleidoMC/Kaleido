package snownee.kaleido.chisel.block;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import snownee.kaleido.chisel.ChiselModule;
import snownee.kaleido.core.supplier.BlockStateModelSupplier;
import snownee.kaleido.core.supplier.ModelSupplier;

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
		for (ModelSupplier supplier : textures.values()) {
			if (supplier != null && !supplier.canOcclude()) {
				return false;
			}
		}
		return true;
	}

	public ModelSupplier getTexture() {
		return textures.get("0");
	}

	@Override
	public boolean isValidTexture(ModelSupplier modelSupplier) {
		if (modelSupplier instanceof BlockStateModelSupplier) {
			BlockState state = ((BlockStateModelSupplier) modelSupplier).state;
			if (ChiselModule.CHISELED_BLOCKS.contains(state.getBlock()))
				return false;
		}
		return true;
	}

}
