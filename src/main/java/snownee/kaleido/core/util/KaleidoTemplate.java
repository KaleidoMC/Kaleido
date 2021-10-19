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
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelLoader;
import snownee.kaleido.core.CoreModule;
import snownee.kaleido.core.ModelInfo;

public enum KaleidoTemplate {
	none(CoreModule.STUFF, false, 4, 2, RenderTypeEnum.solid),
	block(CoreModule.HORIZONTAL, true, 1, 0, RenderTypeEnum.solid),
	horizontal(CoreModule.HORIZONTAL, true, 4, 2, RenderTypeEnum.solid),
	directional(CoreModule.DIRECTIONAL, true, 6, 2, RenderTypeEnum.solid), //TODO nonSolid
	pillar(CoreModule.PILLAR, true, 3, 1, RenderTypeEnum.solid),
	leaves(CoreModule.LEAVES, false, 4, 2, RenderTypeEnum.cutoutMipped),
	plant(CoreModule.PLANT, false, 4, 2, RenderTypeEnum.cutout),
	item(Blocks.AIR, true, 1, 0, RenderTypeEnum.solid);

	public final boolean solid;
	public final Block bloc;
	public final int metaCount;
	public final int defaultMeta;
	public final byte defaultRenderTypeFlags;

	KaleidoTemplate(Block block, boolean solid, int metaCount, int defaultMeta, RenderTypeEnum defaultRenderType) {
		bloc = block;
		this.solid = solid;
		this.metaCount = metaCount;
		this.defaultMeta = defaultMeta;
		this.defaultRenderTypeFlags = (byte) (1 << defaultRenderType.ordinal());
		if (block != Blocks.AIR) {
			CoreModule.MASTER_BLOCKS.add(block);
		}
	}

	public int toMeta(@Nullable BlockState state) {
		switch (this) {
		case none:
		case horizontal:
		case leaves:
		case plant:
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
		return defaultMeta;
	}

	@Nullable
	public BlockState fromMeta(int meta) {
		if (meta < 0)
			return null;
		switch (this) {
		case block:
			return bloc.defaultBlockState();
		case none:
		case horizontal:
		case leaves:
		case plant:
			return bloc.defaultBlockState().setValue(HorizontalBlock.FACING, Direction.from2DDataValue(meta));
		case directional:
			return bloc.defaultBlockState().setValue(DirectionalBlock.FACING, Direction.from3DDataValue(meta));
		case pillar:
			return bloc.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.values()[meta % 3]);
		default:
			break;
		}
		return null;
	}

	@OnlyIn(Dist.CLIENT)
	@Nullable
	public IBakedModel loadModel(ModelLoader modelLoader, ModelInfo info, int variant) {
		ModelRotation transform = ModelRotation.X0_Y0;
		if (this == none || this == horizontal || this == leaves || this == plant) {
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
		ResourceLocation modelLocation = getModelLocation(info, variant);
		return modelLoader.getBakedModel(modelLocation, transform, modelLoader.getSpriteMap()::getSprite);
	}

	public ResourceLocation getModelLocation(ModelInfo info, int variant) {
		return new ResourceLocation(info.id.getNamespace(), "kaleido/" + info.id.getPath());
	}

	public boolean allowsCustomShape() {
		return this == none || this == leaves || this == plant;
	}

	public VoxelShape getShape(BlockState state) {
		return VoxelShapes.block();
	}

	public SoundTypeEnum defaultSoundType() {
		if (this == leaves || this == plant)
			return SoundTypeEnum.grass;
		return SoundTypeEnum.wood;
	}

}
