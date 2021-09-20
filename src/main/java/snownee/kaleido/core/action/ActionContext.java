package snownee.kaleido.core.action;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import snownee.kaleido.core.ModelInfo;

public class ActionContext extends BlockItemUseContext {

	private final BlockRayTraceResult hitResult;
	private final ModelInfo modelInfo;

	public ActionContext(PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult hitResult, ModelInfo modelInfo) {
		super(player, hand, stack, hitResult);
		this.hitResult = hitResult;
		this.modelInfo = modelInfo;
	}

	public BlockRayTraceResult getHit() {
		return hitResult;
	}

	public BlockPos getBlockPos() {
		return hitResult.getBlockPos();
	}

	public ModelInfo getModelInfo() {
		return modelInfo;
	}

}
