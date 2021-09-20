package snownee.kaleido.core.action;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.util.JSONUtils;

public enum ActionDeserializer implements Function<JsonElement, Consumer<ActionContext>> {
	INSTANCE;

	private static final Map<String, Function<JsonObject, Consumer<ActionContext>>> factories = Maps.newHashMap();

	public static synchronized void registerFactory(String name, Function<JsonObject, Consumer<ActionContext>> factory) {
		factories.put(name, factory);
	}

	@Override
	public Consumer<ActionContext> apply(JsonElement json) {
		if (json != null && json.isJsonObject()) {
			JsonObject object = json.getAsJsonObject();
			Function<JsonObject, Consumer<ActionContext>> factory = factories.get(JSONUtils.getAsString(object, "type"));
			if (factory != null) {
				return factory.apply(object);
			}
		}
		throw new JsonSyntaxException(Objects.toString(json));
	}

	public List<Consumer<ActionContext>> list(JsonObject json) {
		ImmutableList.Builder<Consumer<ActionContext>> builder = ImmutableList.builder();
		if (json.has("action")) {
			builder.add(apply(json.get("action")));
		}
		if (json.has("actions")) {
			for (JsonElement e : json.getAsJsonArray("actions")) {
				builder.add(apply(e));
			}
		}
		return builder.build();
	}
}
