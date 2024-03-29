package snownee.kaleido.core.behavior;

import java.util.List;
import java.util.function.Consumer;

import com.google.gson.JsonObject;

import net.minecraft.util.ActionResultType;
import snownee.kaleido.core.action.Action;
import snownee.kaleido.core.action.ActionContext;

public class EventBehavior implements Behavior {

	public final List<Consumer<ActionContext>> actions;

	public EventBehavior(JsonObject obj) {
		this(Action.list(obj));
	}

	public EventBehavior(List<Consumer<ActionContext>> actions) {
		this.actions = actions;
	}

	public ActionResultType run(ActionContext context) {
		if (actions.isEmpty())
			return ActionResultType.PASS;
		actions.forEach($ -> $.accept(context));
		return ActionResultType.SUCCESS;
	}

}
