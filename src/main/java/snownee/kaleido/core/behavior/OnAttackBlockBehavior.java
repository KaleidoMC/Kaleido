package snownee.kaleido.core.behavior;

import com.google.gson.JsonObject;

import snownee.kaleido.core.action.ActionContext;

public class OnAttackBlockBehavior extends EventBehavior {

	public static OnAttackBlockBehavior create(JsonObject obj) {
		return new OnAttackBlockBehavior(obj);
	}

	public OnAttackBlockBehavior(JsonObject obj) {
		super(obj);
	}

	@Override
	public void attack(ActionContext context) {
		run(context);
	}

}
