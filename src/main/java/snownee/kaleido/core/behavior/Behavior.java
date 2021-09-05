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

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import snownee.kaleido.core.tile.MasterTile;

public interface Behavior {

	public enum Deserializer implements JsonDeserializer<Behavior> {
		INSTANCE;

		private static final Map<String, Function<JsonObject, Behavior>> factories = Maps.newHashMap();

		public static synchronized void registerFactory(String name, Function<JsonObject, Behavior> factory) {
			factories.put(name, factory);
		}

		@Override
		public Behavior deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			if (json != null) {
				if (json.isJsonObject()) {
					JsonObject object = json.getAsJsonObject();
					Function<JsonObject, Behavior> factory = factories.get(JSONUtils.getAsString(object, "type"));
					if (factory != null) {
						return factory.apply(object);
					}
				} else {
					Function<JsonObject, Behavior> factory = factories.get(json.getAsString());
					if (factory != null) {
						return factory.apply(new JsonObject());
					}
				}
			}
			return NoneBehavior.INSTANCE;
		}

	}

	Behavior copy(MasterTile tile);

	default <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return LazyOptional.empty();
	}

	ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit);

	default void read(CompoundNBT data) {
	}

	default CompoundNBT write(CompoundNBT data) {
		return data;
	}

	default int getLightValue() {
		return 0;
	}

}
