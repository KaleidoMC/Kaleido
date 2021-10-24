package snownee.kaleido.core.template;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RotatedPillarBlock;

public class Pillar extends KaleidoTemplate {

	public Pillar(String name, Block block, boolean solid, int metaCount) {
		super(name, block, solid, metaCount);
	}

	@Override
	protected int _toMeta(BlockState state) {
		return state.getValue(RotatedPillarBlock.AXIS).ordinal();
	}

}
