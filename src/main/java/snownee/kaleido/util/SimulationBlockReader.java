package snownee.kaleido.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;

public class SimulationBlockReader extends WrappedBlockReader {

	private TileEntity simulatedBlockEntity;
	private BlockPos simulatedPos;
	private boolean useSelfLight;
	private int globalLight = -1;

	public void setBlockEntity(TileEntity blockEntity) {
		simulatedBlockEntity = blockEntity;
	}

	public void setPos(BlockPos pos) {
		simulatedPos = pos;
	}

	@Override
	public TileEntity getBlockEntity(BlockPos pos) {
		if (simulatedBlockEntity != null && pos.equals(simulatedPos)) {
			return simulatedBlockEntity;
		}
		return super.getBlockEntity(pos);
	}

	public void useSelfLight(boolean useSelfLight) {
		this.useSelfLight = useSelfLight;
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		if (globalLight != -1 && simulatedPos != pos) {
			return Blocks.AIR.defaultBlockState();
		}
		if (useSelfLight && simulatedPos != null) {
			if (simulatedPos.distManhattan(pos) < 3) {
				return Blocks.AIR.defaultBlockState();
			}
		}
		return super.getBlockState(pos);
	}

	@Override
	public int getBrightness(LightType lightType, BlockPos pos) {
		if (globalLight != -1) {
			return globalLight;
		}
		if (useSelfLight && simulatedPos != null) {
			if (simulatedPos.distManhattan(pos) < 3) {
				pos = simulatedPos;
			}
		}
		return super.getBrightness(lightType, pos);
	}

	public void setOverrideLight(int i) {
		globalLight = i;
	}

}
