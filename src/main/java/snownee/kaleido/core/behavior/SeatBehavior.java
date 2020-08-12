package snownee.kaleido.core.behavior;

import java.util.List;

import com.google.gson.JsonObject;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.fml.LogicalSide;
import snownee.kaleido.core.behavior.seat.SeatEntity;
import snownee.kaleido.core.tile.MasterTile;
import snownee.kiwi.schedule.Scheduler;
import snownee.kiwi.schedule.impl.SimpleGlobalTask;

public class SeatBehavior implements Behavior {

    public static SeatBehavior create(JsonObject obj) {
        if (obj.has("seat")) {
            return new SeatBehavior(new Vec3d[] { vecFromJson(obj.getAsJsonObject("seat")) });
        }
        return new SeatBehavior(new Vec3d[] { new Vec3d(0.5, 0.25, 0.5) });
    }

    public static Vec3d vecFromJson(JsonObject o) {
        return new Vec3d(JSONUtils.getFloat(o, "x", 0.5f), JSONUtils.getFloat(o, "y", 0.5f), JSONUtils.getFloat(o, "z", 0.5f));
    }

    private final Vec3d[] seats;

    public SeatBehavior(Vec3d[] seats) {
        this.seats = seats;
    }

    @Override
    public Behavior copy(MasterTile tile) {
        return this;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (seats.length == 0 || player instanceof FakePlayer || player.getRidingEntity() != null)
            return ActionResultType.FAIL;
        ItemStack stack1 = player.getHeldItemMainhand();
        ItemStack stack2 = player.getHeldItemOffhand();
        if (!stack1.isEmpty() || !stack2.isEmpty())
            return ActionResultType.FAIL;
        Vec3d vec = seats[0];
        vec = vec.add(pos.getX(), pos.getY(), pos.getZ());

        double maxDist = 2;
        if ((vec.x - player.getPosX()) * (vec.x - player.getPosX()) + (vec.y - player.getPosY()) * (vec.y - player.getPosY()) + (vec.z - player.getPosZ()) * (vec.z - player.getPosZ()) > maxDist * maxDist)
            return ActionResultType.FAIL;

        List<SeatEntity> seats = worldIn.getEntitiesWithinAABB(SeatEntity.class, new AxisAlignedBB(pos, pos.add(1, 1, 1)));

        if (!seats.isEmpty()) {
            return ActionResultType.FAIL;
        }
        SeatEntity seat = new SeatEntity(worldIn, vec);
        worldIn.addEntity(seat);
        Scheduler.add(new SimpleGlobalTask(LogicalSide.SERVER, Phase.END, i -> {
            if (player.isPassenger()) {
                return true;
            }
            if (i > 3) {
                player.startRiding(seat);
                return true;
            }
            return false;
        }));
        return ActionResultType.SUCCESS;

    }

}
