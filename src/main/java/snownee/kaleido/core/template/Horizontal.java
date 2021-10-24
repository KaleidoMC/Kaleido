package snownee.kaleido.core.template;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;

public class Horizontal extends KaleidoTemplate {

	public Horizontal(String name, Block block, boolean solid, int metaCount) {
		super(name, block, solid, metaCount);
	}

	@Override
	protected int _toMeta(BlockState state) {
		return state.getValue(HorizontalBlock.FACING).get2DDataValue();
	}

}
