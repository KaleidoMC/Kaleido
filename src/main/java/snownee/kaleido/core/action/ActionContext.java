package snownee.kaleido.core.action;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import snownee.kaleido.core.ModelInfo;

public class ActionContext {

	private final ModelInfo modelInfo;
	private final World level;
	public BlockRayTraceResult hitResult;
	public Entity entity;
	@Nullable
	public PlayerEntity player;
	public Hand hand = Hand.MAIN_HAND;
	public ItemStack itemStack;
	public BlockPos relativePos;
	public boolean replaceClicked = true;

	public ActionContext(World level, ModelInfo modelInfo) {
		this.level = level;
		this.modelInfo = modelInfo;
	}

	public ActionContext(PlayerEntity player, ModelInfo modelInfo) {
		this(player.level, modelInfo);
		this.player = player;
	}

	public World getLevel() {
		return level;
	}

	public PlayerEntity getPlayer() {
		return player;
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
