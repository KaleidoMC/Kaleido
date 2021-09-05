package snownee.kaleido.core.behavior;

import com.google.gson.JsonObject;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import snownee.kaleido.core.tile.MasterTile;

public class LightBehavior implements Behavior {

    public static LightBehavior create(JsonObject obj) {
        return new LightBehavior(JSONUtils.getAsInt(obj, "light", 15));
    }

    private final int light;

    public LightBehavior(int light) {
        this.light = light;
    }

    @Override
    public Behavior copy(MasterTile tile) {
        return this;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        return ActionResultType.PASS;
    }

    @Override
    public int getLightValue() {
        return light;
    }

}
