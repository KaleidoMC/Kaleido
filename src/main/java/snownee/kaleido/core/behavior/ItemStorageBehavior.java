package snownee.kaleido.core.behavior;

import com.google.gson.JsonObject;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import snownee.kaleido.core.tile.MasterTile;
import snownee.kiwi.inventory.InvHandlerWrapper;

public class ItemStorageBehavior implements Behavior {

    public static ItemStorageBehavior create(JsonObject obj) {
        return new ItemStorageBehavior(JSONUtils.getInt(obj, "rows", 3));
    }

    private LazyOptional<ItemStackHandler> handler = LazyOptional.empty();
    private int rows;
    private ITextComponent title;

    public ItemStorageBehavior(int rows) {
        this.rows = rows;
    }

    @Override
    public Behavior copy(MasterTile tile) {
        ItemStorageBehavior copy = new ItemStorageBehavior(rows);
        copy.handler = LazyOptional.of(() -> new ItemStackHandler(rows * 9));
        if (tile.getModelInfo() != null) {
            copy.title = new TranslationTextComponent(tile.getModelInfo().getTranslationKey());
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
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (title != null && !worldIn.isRemote) {
            player.openContainer(new SimpleNamedContainerProvider((id, playerInventory, player2) -> {
                if (rows == 6) {
                    return ChestContainer.createGeneric9X6(id, playerInventory, new InvHandlerWrapper(handler.orElse(null)));
                } else {
                    return ChestContainer.createGeneric9X3(id, playerInventory, new InvHandlerWrapper(handler.orElse(null)));
                }
            }, title));
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void read(CompoundNBT data) {
        handler.ifPresent($ -> $.deserializeNBT(data));
    }

    @Override
    public CompoundNBT write(CompoundNBT data) {
        if (handler.isPresent()) {
            return handler.orElseGet(ItemStackHandler::new).serializeNBT();
        }
        return data;
    }

}
