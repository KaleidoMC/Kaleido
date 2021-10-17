package snownee.kaleido.core.behavior;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import snownee.kaleido.core.KaleidoDataManager;
import snownee.kaleido.core.action.ActionContext;
import snownee.kaleido.core.block.entity.MasterBlockEntity;

public interface Behavior {

	public enum Deserializer implements JsonDeserializer<Behavior> {
		INSTANCE;

		private static final Map<String, Function<JsonObject, Behavior>> factories = Maps.newHashMap();

		public static synchronized void registerFactory(String name, Function<JsonObject, Behavior> factory) {
			factories.put(name, factory);
		}

		@Override
		public Behavior deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			if (json.isJsonObject()) {
				JsonObject object = json.getAsJsonObject();
				Function<JsonObject, Behavior> factory = factories.get(JSONUtils.getAsString(object, "type"));
				return factory.apply(object);
			} else {
				Function<JsonObject, Behavior> factory = factories.get(json.getAsString());
				return factory.apply(new JsonObject());
			}
		}

	}

	static Behavior fromJson(JsonElement json) {
		return KaleidoDataManager.GSON.fromJson(json, Behavior.class);
	}

	default Behavior copy(MasterBlockEntity tile) {
		return this;
	}

	default <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return LazyOptional.empty();
	}

	default ActionResultType use(ActionContext context) {
		return ActionResultType.PASS;
	}

	default void attack(ActionContext context) {
	}

	default void onProjectileHit(ActionContext context) {
	}

	default void load(CompoundNBT data) {
	}

	default CompoundNBT save(CompoundNBT data) {
		return data;
	}

	default int getLightValue() {
		return 0;
	}

}
