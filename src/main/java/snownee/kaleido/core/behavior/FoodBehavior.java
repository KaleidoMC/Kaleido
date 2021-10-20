package snownee.kaleido.core.behavior;

import java.util.Locale;

import com.google.gson.JsonObject;

import net.minecraft.item.Food;
import net.minecraft.item.UseAction;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import snownee.kaleido.util.data.FoodSerializer;

public class FoodBehavior implements Behavior {

	public Food food;
	public UseAction animation = UseAction.EAT;

	public FoodBehavior(JsonObject obj) {
		if (obj == null) // from network
			return;
		food = FoodSerializer.fromJson(obj);
		if (obj.has("animation")) {
			animation = UseAction.valueOf(JSONUtils.getAsString(obj, "animation").toUpperCase(Locale.ENGLISH));
		}
	}

	@Override
	public boolean syncClient() {
		return true;
	}

	@Override
	public void fromNetwork(PacketBuffer buf) {
	}

	@Override
	public void toNetwork(PacketBuffer buf) {
	}

}
