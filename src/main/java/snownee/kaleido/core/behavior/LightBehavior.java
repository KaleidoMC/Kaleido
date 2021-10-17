package snownee.kaleido.core.behavior;

import com.google.gson.JsonObject;

import net.minecraft.util.JSONUtils;

public class LightBehavior implements Behavior {

	public static LightBehavior create(JsonObject obj) {
		return new LightBehavior(JSONUtils.getAsInt(obj, "light", 15));
	}

	private final int light;

	public LightBehavior(int light) {
		this.light = light;
	}

	@Override
	public int getLightValue() {
		return light;
	}

}
