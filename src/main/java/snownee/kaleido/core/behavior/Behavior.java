package snownee.kaleido.core.behavior;

import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import snownee.kaleido.core.block.entity.MasterBlockEntity;

public interface Behavior {

	Map<String, Function<JsonObject, Behavior>> factories = Maps.newConcurrentMap();

	static void registerFactory(String name, Function<JsonObject, Behavior> factory) {
		factories.put(name, factory);
	}

	static Behavior fromJson(String type, JsonElement json) {
		Function<JsonObject, Behavior> factory = factories.get(type);
		if (factory == null)
			return null;
		JsonObject o = json.isJsonObject() ? json.getAsJsonObject() : new JsonObject();
		return factory.apply(o);
	}

	default boolean isSerializable() {
		return false;
	}

	// need to override isSerializable()
	default <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return LazyOptional.empty();
	}

	// need to override isSerializable()
	default Behavior copy(MasterBlockEntity tile) {
		return this;
	}

	// need to override isSerializable()
	default void load(CompoundNBT data) {
	}

	// need to override isSerializable()
	default CompoundNBT save(CompoundNBT data) {
		return data;
	}

	default boolean syncClient() {
		return false;
	}

	// need to override syncClient()
	default void toNetwork(PacketBuffer buf) {
	}

	// need to override syncClient()
	default void fromNetwork(PacketBuffer buf) {
	}

}
