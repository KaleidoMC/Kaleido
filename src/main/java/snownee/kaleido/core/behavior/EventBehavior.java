package snownee.kaleido.core.behavior;

import java.util.List;
import java.util.function.Consumer;

import com.google.gson.JsonObject;

import snownee.kaleido.core.action.ActionContext;
import snownee.kaleido.core.action.ActionDeserializer;

public abstract class EventBehavior implements Behavior {

	public final List<Consumer<ActionContext>> actions;

	public EventBehavior(JsonObject obj) {
		this(ActionDeserializer.INSTANCE.list(obj));
	}

	public EventBehavior(List<Consumer<ActionContext>> actions) {
		this.actions = actions;
	}

	public void run(ActionContext context) {
		actions.forEach($ -> $.accept(context));
	}

}