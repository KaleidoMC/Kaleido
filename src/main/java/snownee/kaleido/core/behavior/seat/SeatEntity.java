package snownee.kaleido.core.behavior.seat;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import snownee.kaleido.core.CoreModule;

public class SeatEntity extends Entity {

    private Block block;

    public SeatEntity(World world) {
        super(CoreModule.SEAT, world);
    }

    public SeatEntity(World world, Vec3d pos) {
        this(world);
        setPosition(pos.x, pos.y + 0.001, pos.z);
        block = world.getBlockState(getPosition()).getBlock();
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {}

    @Override
    protected void registerData() {}

    @Override
    public void tick() {
        if (world.isRemote) {
            return;
        }
        if (this.getPosY() < -64.0D) {
            this.outOfWorld();
        }

        BlockPos pos = getPosition();
        if (pos == null || getEntityWorld().getBlockState(pos).getBlock() != block) {
            remove();
            return;
        }

        List<Entity> passangers = getPassengers();
        for (Entity e : passangers)
            if (!e.isAlive())
                e.stopRiding();
        if (passangers.isEmpty()) {
            if (++portalCounter > 5) {
                remove();
            }
        } else {
            portalCounter = 0;
        }

        this.firstUpdate = false;
    }

    @Override
    protected void writeAdditional(CompoundNBT p_213281_1_) {}
}
