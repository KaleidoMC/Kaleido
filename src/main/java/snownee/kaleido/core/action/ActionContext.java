package snownee.kaleido.core.action;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;

public class ActionContext extends BlockItemUseContext {

	private final BlockRayTraceResult hitResult;

	public ActionContext(PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult hitResult) {
		super(player, hand, stack, hitResult);
		this.hitResult = hitResult;
	}

	public BlockRayTraceResult getHit() {
		return hitResult;
	}

	public BlockPos getBlockPos() {
		return hitResult.getBlockPos();
	}

}
