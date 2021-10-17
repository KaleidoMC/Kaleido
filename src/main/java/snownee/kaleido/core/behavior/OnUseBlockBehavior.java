package snownee.kaleido.core.behavior;

import com.google.gson.JsonObject;

import net.minecraft.util.ActionResultType;
import snownee.kaleido.core.action.ActionContext;

public class OnUseBlockBehavior extends EventBehavior {

	public static OnUseBlockBehavior create(JsonObject obj) {
		return new OnUseBlockBehavior(obj);
	}

	public OnUseBlockBehavior(JsonObject obj) {
		super(obj);
	}

	@Override
	public ActionResultType use(ActionContext context) {
		run(context);
		return ActionResultType.SUCCESS;
	}

}
