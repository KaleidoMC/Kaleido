package snownee.kaleido.core.action;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import snownee.kaleido.core.ModelInfo;

public class ActionContext extends BlockItemUseContext {

	private final BlockRayTraceResult hitResult;
	private final ModelInfo modelInfo;
	public Entity entity;

	public ActionContext(World level, PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult hitResult, ModelInfo modelInfo) {
		super(level, player, hand, stack, hitResult);
		this.hitResult = hitResult;
		this.modelInfo = modelInfo;
	}

	public ActionContext(PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult hitResult, ModelInfo modelInfo) {
		this(player.level, player, hand, stack, hitResult, modelInfo);
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
