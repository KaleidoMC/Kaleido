package snownee.kaleido.core.behavior;

import java.util.function.Consumer;

import com.google.gson.JsonObject;

import net.minecraft.util.ActionResultType;
import snownee.kaleido.core.action.ActionContext;
import snownee.kaleido.core.action.ActionDeserializer;
import snownee.kaleido.core.block.entity.MasterBlockEntity;

public class OnUseBlockBehavior implements Behavior {

	public static OnUseBlockBehavior create(JsonObject obj) {
		return new OnUseBlockBehavior(ActionDeserializer.INSTANCE.apply(obj.get("action")));
	}

	private final Consumer<ActionContext> action;

	public OnUseBlockBehavior(Consumer<ActionContext> action) {
		this.action = action;
	}

	@Override
	public Behavior copy(MasterBlockEntity tile) {
		return this;
	}

	@Override
	public ActionResultType use(ActionContext context) {
		action.accept(context);
		return ActionResultType.SUCCESS;
	}

}
