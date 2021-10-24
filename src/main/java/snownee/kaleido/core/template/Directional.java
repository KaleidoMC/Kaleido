package snownee.kaleido.core.template;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;

public class Directional extends KaleidoTemplate {

	public Directional(String name, Block block, boolean solid, int metaCount) {
		super(name, block, solid, metaCount);
	}

	@Override
	protected int _toMeta(BlockState state) {
		return state.getValue(DirectionalBlock.FACING).get3DDataValue();
	}

}
