package snownee.kaleido.core.action.seat;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import snownee.kaleido.core.CoreModule;

public class SeatEntity extends Entity {

	private Block block;

	public SeatEntity(World world) {
		super(CoreModule.SEAT, world);
	}

	public SeatEntity(World world, Vector3d pos) {
		this(world);
		setPos(pos.x, pos.y + 0.001, pos.z);
		block = world.getBlockState(blockPosition()).getBlock();
	}

	@Override
	public IPacket<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	protected void readAdditionalSaveData(CompoundNBT compound) {
	}

	@Override
	protected void defineSynchedData() {
	}

	@Override
	public void tick() {
		if (level.isClientSide) {
			return;
		}
		if (this.getY() < -64.0D) {
			outOfWorld();
		}

		BlockPos pos = blockPosition();
		if (pos == null || level.getBlockState(pos).getBlock() != block) {
			remove();
			return;
		}

		List<Entity> passangers = getPassengers();
		for (Entity e : passangers)
			if (!e.isAlive())
				e.stopRiding();
		if (passangers.isEmpty()) {
			if (++portalTime > 5) {
				remove();
			}
		} else {
			portalTime = 0;
		}

		firstTick = false;
	}

	@Override
	protected void addAdditionalSaveData(CompoundNBT p_213281_1_) {
	}

}
