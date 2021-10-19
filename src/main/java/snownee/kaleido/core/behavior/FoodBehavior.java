package snownee.kaleido.core.behavior;

import com.google.gson.JsonObject;

import net.minecraft.item.Food;
import snownee.kaleido.util.data.FoodSerializer;

public class FoodBehavior implements Behavior {

	public Food food;

	public FoodBehavior(JsonObject obj) {
		food = FoodSerializer.fromJson(obj);
	}

}
