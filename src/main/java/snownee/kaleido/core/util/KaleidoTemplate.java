package snownee.kaleido.core.util;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import snownee.kaleido.core.CoreModule;

public enum KaleidoTemplate {
	/* off */
	none(CoreModule.STUFF, false),
	block(CoreModule.HORIZONTAL, true),
	horizontal(CoreModule.HORIZONTAL, true),
	item(Blocks.AIR, true);
	/* on */

	public final boolean solid;
	public final Block bloc;

	private KaleidoTemplate(Block block, boolean solid) {
		this.bloc = block;
		this.solid = solid;
	}
}
