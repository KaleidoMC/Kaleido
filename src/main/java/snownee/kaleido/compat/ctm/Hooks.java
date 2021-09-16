package snownee.kaleido.compat.ctm;

import java.util.Objects;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;
import snownee.kaleido.Kaleido;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.block.entity.MasterBlockEntity;
import team.chisel.ctm.client.util.CTMLogic;

public final class Hooks {

	private static final Minecraft mc = Minecraft.getInstance();

	public static boolean stateComparator(CTMLogic ctmLogic, BlockPos fromPos, BlockPos toPos, BlockState from, BlockState to, Direction dir) {
		if (!ctmLogic.ignoreStates() && from != to)
			return false;
		return Objects.equals(getId(mc.level, fromPos, from), getId(mc.level, toPos, to));
	}

	private static ResourceLocation getId(World level, BlockPos pos, BlockState state) {
		if (Kaleido.isKaleidoBlock(state)) {
			IModelData data = ModelDataManager.getModelData(level, pos);
			if (data != null) {
				ModelInfo info = data.getData(MasterBlockEntity.MODEL);
				if (info != null) {
					return info.id;
				}
			}
		}
		return state.getBlock().getRegistryName();
	}

}
