package snownee.kaleido.util.data;

import com.google.gson.JsonObject;

import net.minecraft.item.Food;
import net.minecraft.util.JSONUtils;

public final class FoodSerializer {

	@SuppressWarnings("deprecation")
	public static Food fromJson(JsonObject json) {
		Food.Builder builder = new Food.Builder();
		builder.nutrition(JSONUtils.getAsInt(json, "nutrition"));
		builder.saturationMod(JSONUtils.getAsFloat(json, "saturation"));
		if (JSONUtils.getAsBoolean(json, "fast", false)) {
			builder.fast();
		}
		if (JSONUtils.getAsBoolean(json, "alwaysEat", false)) {
			builder.alwaysEat();
		}
		if (JSONUtils.getAsBoolean(json, "meat", false)) {
			builder.meat();
		}
		if (json.has("effect")) {
			float f = JSONUtils.getAsFloat(json.getAsJsonObject("effect"), "probability", 1);
			builder.effect(EffectSerializer.fromJson(json.getAsJsonObject("effect")), f);
		}
		return builder.build();
	}

}
