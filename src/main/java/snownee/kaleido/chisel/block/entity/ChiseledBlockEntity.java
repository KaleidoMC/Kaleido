package snownee.kaleido.chisel.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import snownee.kaleido.chisel.ChiselModule;
import snownee.kaleido.core.block.entity.BELightManager;
import snownee.kaleido.core.definition.BlockDefinition;

public class ChiseledBlockEntity extends RetextureBlockEntity {

	public ChiseledBlockEntity() {
		super(ChiselModule.CHISELED, "0");
	}

	@Override
	public CompoundNBT save(CompoundNBT data) {
		writePacketData(data);
		return super.save(data);
	}

	@Override
	public void load(BlockState state, CompoundNBT data) {
		readPacketData(data);
		super.load(state, data);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		lightManager = new BELightManager(level, worldPosition, this::getLightRaw);
		lightManager.update();
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
		BlockState state = modelSupplier.getBlockState();
		return !ChiselModule.CHISELED_BLOCKS.contains(state.getBlock());
	}

}
