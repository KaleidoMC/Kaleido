package snownee.kaleido.core.action;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.util.JSONUtils;
import snownee.kaleido.util.KaleidoUtil;

public interface Action {

	Map<String, Function<JsonObject, Consumer<ActionContext>>> factories = Maps.newConcurrentMap();

	static void registerFactory(String name, Function<JsonObject, Consumer<ActionContext>> factory) {
		Preconditions.checkArgument(name.length() <= 32);
		factories.put(name, factory);
	}

	static Consumer<ActionContext> fromJson(JsonElement json) {
		if (json != null) {
			if (json.isJsonObject()) {
				JsonObject object = json.getAsJsonObject();
				Function<JsonObject, Consumer<ActionContext>> factory = factories.get(JSONUtils.getAsString(object, "type"));
				if (factory != null) {
					return factory.apply(object);
				}
			} else if (json.isJsonPrimitive()) {
				String s = json.getAsString();
				Function<JsonObject, Consumer<ActionContext>> factory = factories.get(s);
				if (factory != null) {
					return factory.apply(new JsonObject());
				}
			}
		}
		throw new JsonSyntaxException(Objects.toString(json));
	}

	static List<Consumer<ActionContext>> list(JsonObject json) {
		ImmutableList.Builder<Consumer<ActionContext>> builder = ImmutableList.builder();
		KaleidoUtil.jsonList(json.get("action"), $ -> builder.add(fromJson($)));
		return builder.build();
	}
}
