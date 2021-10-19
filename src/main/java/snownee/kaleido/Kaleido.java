package snownee.kaleido;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraftforge.fml.common.Mod;
import snownee.kaleido.chisel.block.ChiseledBlock;
import snownee.kaleido.core.block.KaleidoBlock;

@Mod(Kaleido.MODID)
public final class Kaleido {

	public static Logger logger = LogManager.getLogger("Kaleido");
	public static final String MODID = "kaleido";

	public static boolean isKaleidoBlock(BlockState state) {
		Block block = state.getBlock();
		return block instanceof KaleidoBlock || block instanceof ChiseledBlock;
	}
}
