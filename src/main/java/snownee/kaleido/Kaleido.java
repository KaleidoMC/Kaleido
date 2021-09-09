package snownee.kaleido;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.BlockState;
import net.minecraftforge.fml.common.Mod;

@Mod(Kaleido.MODID)
public final class Kaleido {

	public static Logger logger = LogManager.getLogger("Kaleido");
	public static final String MODID = "kaleido";

	public static boolean isKaleidoBlock(BlockState state) {
		return Kaleido.MODID.equals(state.getBlock().getRegistryName().getNamespace());
	}
}
