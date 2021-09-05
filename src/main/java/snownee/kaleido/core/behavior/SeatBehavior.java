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
import net.minecraft.util.math.vector.Vector3d;
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
            return new SeatBehavior(new Vector3d[] { vecFromJson(obj.getAsJsonObject("seat")) });
        }
        return new SeatBehavior(new Vector3d[] { new Vector3d(0.5, 0.25, 0.5) });
    }

    public static Vector3d vecFromJson(JsonObject o) {
        return new Vector3d(JSONUtils.getAsFloat(o, "x", 0.5f), JSONUtils.getAsFloat(o, "y", 0.5f), JSONUtils.getAsFloat(o, "z", 0.5f));
    }

    private final Vector3d[] seats;

    public SeatBehavior(Vector3d[] seats) {
        this.seats = seats;
    }

    @Override
    public Behavior copy(MasterTile tile) {
        return this;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (seats.length == 0 || player instanceof FakePlayer || player.getVehicle() != null)
            return ActionResultType.FAIL;
        ItemStack stack1 = player.getMainHandItem();
        ItemStack stack2 = player.getOffhandItem();
        if (!stack1.isEmpty() || !stack2.isEmpty())
            return ActionResultType.FAIL;
        Vector3d vec = seats[0];
        vec = vec.add(pos.getX(), pos.getY(), pos.getZ());

        double maxDist = 2;
        if ((vec.x - player.getX()) * (vec.x - player.getX()) + (vec.y - player.getY()) * (vec.y - player.getY()) + (vec.z - player.getZ()) * (vec.z - player.getZ()) > maxDist * maxDist)
            return ActionResultType.FAIL;

        List<SeatEntity> seats = worldIn.getEntitiesOfClass(SeatEntity.class, new AxisAlignedBB(pos));

        if (!seats.isEmpty()) {
            return ActionResultType.FAIL;
        }
        SeatEntity seat = new SeatEntity(worldIn, vec);
        worldIn.addFreshEntity(seat);
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
