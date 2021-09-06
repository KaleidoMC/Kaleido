package snownee.kaleido.core;

import net.minecraft.block.Block;

public enum KaleidoTemplate {
	/* off */
	none(CoreModule.STUFF, false),
	block(CoreModule.HORIZONTAL, true),
	horizontal(CoreModule.HORIZONTAL, true);
	/* on */

	public final boolean solid;
	public final Block bloc;

	private KaleidoTemplate(Block block, boolean solid) {
		this.bloc = block;
		this.solid = solid;
	}
}
