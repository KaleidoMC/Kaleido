package snownee.kaleido.core.util;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelRotation;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelLoader;
import snownee.kaleido.core.CoreModule;
import snownee.kaleido.core.ModelInfo;

public enum KaleidoTemplate {
	/* off */
	none(CoreModule.STUFF, false, 4, 2),
	block(CoreModule.HORIZONTAL, true, 1, 0),
	horizontal(CoreModule.HORIZONTAL, true, 4, 2),
	directional(CoreModule.DIRECTIONAL, true, 6, 2), //TODO nonSolid
	pillar(CoreModule.PILLAR, true, 3, 1),
	item(Blocks.AIR, true, 1, 0);
	/* on */

	public final boolean solid;
	public final Block bloc;
	public final int states;
	public final int defaultState;

	KaleidoTemplate(Block block, boolean solid, int states, int defaultState) {
		bloc = block;
		this.solid = solid;
		this.states = states;
		this.defaultState = defaultState;
		if (block != Blocks.AIR) {
			CoreModule.ALL_MASTER_BLOCKS.add(block);
		}
	}

	public int getState(@Nullable BlockState state) {
		switch (this) {
		case none:
		case horizontal:
			if (!state.hasProperty(HorizontalBlock.FACING))
				break;
			return state.getValue(HorizontalBlock.FACING).get2DDataValue();
		case directional:
			if (!state.hasProperty(DirectionalBlock.FACING))
				break;
			return state.getValue(DirectionalBlock.FACING).get3DDataValue();
		case pillar:
			if (!state.hasProperty(RotatedPillarBlock.AXIS))
				break;
			return state.getValue(RotatedPillarBlock.AXIS).ordinal();
		default:
			break;
		}
		return defaultState;
	}

	@OnlyIn(Dist.CLIENT)
	@Nullable
	public IBakedModel loadModel(ModelLoader modelLoader, ModelInfo info, int variant) {
		ModelRotation transform = ModelRotation.X0_Y0;
		if (this == none || this == horizontal) {
			if (variant == Direction.SOUTH.get2DDataValue()) {
				transform = ModelRotation.X0_Y180;
			} else if (variant == Direction.WEST.get2DDataValue()) {
				transform = ModelRotation.X0_Y270;
			} else if (variant == Direction.EAST.get2DDataValue()) {
				transform = ModelRotation.X0_Y90;
			}
		} else if (this == directional) {
			if (variant == Direction.DOWN.get3DDataValue()) {
				transform = ModelRotation.X180_Y0;
			} else if (variant == Direction.EAST.get3DDataValue()) {
				transform = ModelRotation.X90_Y90;
			} else if (variant == Direction.NORTH.get3DDataValue()) {
				transform = ModelRotation.X90_Y0;
			} else if (variant == Direction.SOUTH.get3DDataValue()) {
				transform = ModelRotation.X90_Y180;
			} else if (variant == Direction.WEST.get3DDataValue()) {
				transform = ModelRotation.X90_Y270;
			}
		} else if (this == pillar) {
			if (variant == Direction.Axis.X.ordinal()) {
				transform = ModelRotation.X90_Y90;
			} else if (variant == Direction.Axis.Z.ordinal()) {
				transform = ModelRotation.X90_Y0;
			}
		}
		return modelLoader.getBakedModel(getModelLocation(info, variant), transform, modelLoader.getSpriteMap()::getSprite);
	}

	public ResourceLocation getModelLocation(ModelInfo info, int variant) {
		return new ResourceLocation(info.id.getNamespace(), "kaleido/" + info.id.getPath());
	}
}
