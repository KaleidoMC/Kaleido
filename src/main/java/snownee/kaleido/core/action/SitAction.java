package snownee.kaleido.core.action;

import java.util.List;
import java.util.function.Consumer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.fml.LogicalSide;
import snownee.kaleido.core.action.seat.SeatEntity;
import snownee.kiwi.schedule.Scheduler;
import snownee.kiwi.schedule.impl.SimpleGlobalTask;

public class SitAction implements Consumer<ActionContext> {

	public static Vector3d[] vecFromJson(JsonArray jsonArray) {
		Vector3d[] seats = new Vector3d[jsonArray.size()];
		int i = 0;
		for (JsonElement e : jsonArray) {
			JsonArray a = e.getAsJsonArray();
			seats[i] = new Vector3d(a.get(0).getAsDouble(), a.get(1).getAsDouble(), a.get(2).getAsDouble());
			++i;
		}
		return seats;
	}

	private final Vector3d[] seats;

	public SitAction(JsonObject obj) {
		if (obj.has("seats")) {
			seats = vecFromJson(obj.getAsJsonArray("seats"));
		} else {
			seats = new Vector3d[] { new Vector3d(0.5, 0.25, 0.5) };
		}
	}

	@Override
	public void accept(ActionContext context) { //TODO
		PlayerEntity player = context.getPlayer();
		BlockPos pos = context.getBlockPos();
		if (pos == null || seats.length == 0 || player instanceof FakePlayer || player.getVehicle() != null)
			return;
		World worldIn = context.getLevel();
		ItemStack stack1 = player.getMainHandItem();
		ItemStack stack2 = player.getOffhandItem();
		if (!stack1.isEmpty() || !stack2.isEmpty())
			return;
		Vector3d vec = seats[0];
		vec = vec.add(pos.getX(), pos.getY(), pos.getZ());

		double maxDist = 2;
		if ((vec.x - player.getX()) * (vec.x - player.getX()) + (vec.y - player.getY()) * (vec.y - player.getY()) + (vec.z - player.getZ()) * (vec.z - player.getZ()) > maxDist * maxDist)
			return;

		List<SeatEntity> seats = worldIn.getEntitiesOfClass(SeatEntity.class, new AxisAlignedBB(pos));

		//FIXME
		if (!seats.isEmpty()) {
			return;
		}
		if (!worldIn.isClientSide) {
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
		}
	}

}
