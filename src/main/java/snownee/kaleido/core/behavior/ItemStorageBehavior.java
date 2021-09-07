package snownee.kaleido.core.behavior;

import com.google.gson.JsonObject;

import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import snownee.kaleido.core.action.ActionContext;
import snownee.kaleido.core.block.entity.MasterBlockEntity;
import snownee.kiwi.inventory.InvHandlerWrapper;

public class ItemStorageBehavior implements Behavior {

	public static ItemStorageBehavior create(JsonObject obj) {
		return new ItemStorageBehavior(JSONUtils.getAsInt(obj, "rows", 3));
	}

	private LazyOptional<ItemStackHandler> handler = LazyOptional.empty();
	private int rows;
	private ITextComponent title;

	public ItemStorageBehavior(int rows) {
		this.rows = rows;
	}

	@Override
	public Behavior copy(MasterBlockEntity tile) {
		ItemStorageBehavior copy = new ItemStorageBehavior(rows);
		copy.handler = LazyOptional.of(() -> new ItemStackHandler(rows * 9));
		if (tile.getModelInfo() != null) {
			copy.title = new TranslationTextComponent(tile.getModelInfo().getDescriptionId());
		}
		return copy;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return handler.cast();
		}
		return LazyOptional.empty();
	}

	@Override
	public ActionResultType use(ActionContext context) {
		if (title != null && !context.getLevel().isClientSide) {
			context.getPlayer().openMenu(new SimpleNamedContainerProvider((id, playerInventory, player2) -> {
				if (rows == 6) {
					return ChestContainer.sixRows(id, playerInventory, new InvHandlerWrapper(handler.orElse(null)));
				} else {
					return ChestContainer.threeRows(id, playerInventory, new InvHandlerWrapper(handler.orElse(null)));
				}
			}, title));
		}
		return ActionResultType.SUCCESS;
	}

	@Override
	public void load(CompoundNBT data) {
		handler.ifPresent($ -> $.deserializeNBT(data));
	}

	@Override
	public CompoundNBT save(CompoundNBT data) {
		if (handler.isPresent()) {
			return handler.orElseGet(ItemStackHandler::new).serializeNBT();
		}
		return data;
	}

}
