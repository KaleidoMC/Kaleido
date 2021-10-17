package snownee.kaleido.core.behavior;

import com.google.gson.JsonObject;

import snownee.kaleido.core.action.ActionContext;

public class OnProjectileHitBehavior extends EventBehavior {

	public static OnProjectileHitBehavior create(JsonObject obj) {
		return new OnProjectileHitBehavior(obj);
	}

	public OnProjectileHitBehavior(JsonObject obj) {
		super(obj);
	}

	@Override
	public void onProjectileHit(ActionContext context) {
		run(context);
	}

}
