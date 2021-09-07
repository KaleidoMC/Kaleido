package snownee.kaleido.core.behavior;

import com.google.gson.JsonObject;

import net.minecraft.util.JSONUtils;
import snownee.kaleido.core.block.entity.MasterBlockEntity;

public class LightBehavior implements Behavior {

	public static LightBehavior create(JsonObject obj) {
		return new LightBehavior(JSONUtils.getAsInt(obj, "light", 15));
	}

	private final int light;

	public LightBehavior(int light) {
		this.light = light;
	}

	@Override
	public Behavior copy(MasterBlockEntity tile) {
		return this;
	}

	@Override
	public int getLightValue() {
		return light;
	}

}
