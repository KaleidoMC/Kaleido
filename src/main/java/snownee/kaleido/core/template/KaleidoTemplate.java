package snownee.kaleido.core.template;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelRotation;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.Half;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelLoader;
import snownee.kaleido.core.CoreModule;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.util.RenderTypeEnum;
import snownee.kaleido.core.util.SoundTypeEnum;
import snownee.kaleido.core.util.UVLockedRotation;

public class KaleidoTemplate {
	private static final Map<String, KaleidoTemplate> NAME_MAP = Maps.newHashMap();
	public static final List<KaleidoTemplate> VALUES = Lists.newArrayList();
	public static final KaleidoTemplate NONE = new Horizontal("none", CoreModule.STUFF, false, 4);
	public static final KaleidoTemplate BLOCK = new KaleidoTemplate("block", CoreModule.HORIZONTAL, true, 1);
	public static final KaleidoTemplate HORIZONTAL = new Horizontal("horizontal", CoreModule.HORIZONTAL, true, 4);
	public static final KaleidoTemplate DIRECTIONAL = new Directional("directional", CoreModule.DIRECTIONAL, true, 6); //TODO nonSolid?
	public static final KaleidoTemplate PILLAR = new Pillar("pillar", CoreModule.PILLAR, true, 3);
	public static final KaleidoTemplate STAIRS = new Stairs("stairs", CoreModule.STAIRS, false, 4 * 2 * 3);
	public static final KaleidoTemplate LEAVES = new Horizontal("leaves", CoreModule.LEAVES, false, 4).sound(SoundTypeEnum.grass).renderType(RenderTypeEnum.cutoutMipped);
	public static final KaleidoTemplate PLANT = new Horizontal("plant", CoreModule.PLANT, false, 4).sound(SoundTypeEnum.grass).renderType(RenderTypeEnum.cutout);
	public static final KaleidoTemplate ITEM = new KaleidoTemplate("item", Blocks.AIR, true, 1);

	public int index;
	public final boolean solid;
	public final StateContainer<Block, BlockState> stateContainer;
	public final int metaCount;
	public final int defaultMeta;
	public byte defaultRenderTypeFlags;
	public SoundTypeEnum defaultSound = SoundTypeEnum.wood;

	public KaleidoTemplate(String name, Block block, boolean solid, int metaCount) {
		index = VALUES.size();
		stateContainer = block.getStateDefinition();
		this.solid = solid;
		this.metaCount = metaCount;
		defaultMeta = toMeta(block.defaultBlockState());
		renderType(RenderTypeEnum.solid);
		if (block != Blocks.AIR) {
			CoreModule.MASTER_BLOCKS.add(block);
		}
		NAME_MAP.put(name, this);
		VALUES.add(this);
	}

	public final int toMeta(BlockState state) {
		if (state.getBlock() != getBlock()) {
			state = getBlock().defaultBlockState();
		}
		return _toMeta(state);
	}

	protected int _toMeta(BlockState state) {
		return 0;
	}

	@Nullable
	public final BlockState fromMeta(int meta) {
		if (meta == defaultMeta || meta < 0 || meta >= metaCount)
			return getBlock().defaultBlockState();
		return _fromMeta(meta);
	}

	@Nullable
	protected BlockState _fromMeta(int meta) {
		return stateContainer.getPossibleStates().get(meta);
	}

	@OnlyIn(Dist.CLIENT)
	@Nullable
	public IBakedModel loadModel(ModelLoader modelLoader, ModelInfo info, BlockState state) {
		//Preconditions.checkArgument(!info.expired);
		ModelRotation transform = ModelRotation.X0_Y0;
		if (this == NONE || this == HORIZONTAL || this == LEAVES || this == PLANT) {
			Direction direction = state.getValue(HorizontalBlock.FACING);
			if (direction == Direction.SOUTH) {
				transform = ModelRotation.X0_Y180;
			} else if (direction == Direction.WEST) {
				transform = ModelRotation.X0_Y270;
			} else if (direction == Direction.EAST) {
				transform = ModelRotation.X0_Y90;
			}
		} else if (this == DIRECTIONAL) {
			Direction direction = state.getValue(DirectionalBlock.FACING);
			if (direction == Direction.DOWN) {
				transform = ModelRotation.X180_Y0;
			} else if (direction == Direction.EAST) {
				transform = ModelRotation.X90_Y90;
			} else if (direction == Direction.NORTH) {
				transform = ModelRotation.X90_Y0;
			} else if (direction == Direction.SOUTH) {
				transform = ModelRotation.X90_Y180;
			} else if (direction == Direction.WEST) {
				transform = ModelRotation.X90_Y270;
			}
		} else if (this == PILLAR) {
			Direction.Axis axis = state.getValue(RotatedPillarBlock.AXIS);
			if (axis == Direction.Axis.X) {
				transform = ModelRotation.X90_Y90;
			} else if (axis == Direction.Axis.Z) {
				transform = ModelRotation.X90_Y0;
			}
		} else if (this == STAIRS) {
			state = Stairs.transform(state);
			Direction direction = state.getValue(StairsBlock.FACING);
			boolean top = state.getValue(StairsBlock.HALF) == Half.TOP;
			int y = 0;
			int x = top ? 180 : 0;
			if (direction == Direction.NORTH) {
				y = 270;
			} else if (direction == Direction.SOUTH) {
				y = 90;
			} else if (direction == Direction.WEST) {
				y = 180;
			}
			transform = ModelRotation.by(x, y);
		}
		ResourceLocation modelLocation = getModelLocation(info, state);
		return modelLoader.getBakedModel(modelLocation, UVLockedRotation.of(transform, info.uvLock), modelLoader.getSpriteMap()::getSprite);
	}

	@OnlyIn(Dist.CLIENT)
	public ResourceLocation getModelLocation(ModelInfo info, BlockState state) {
		String path = "kaleido/" + info.id.getPath();
		return new ResourceLocation(info.id.getNamespace(), path);
	}

	public boolean allowsCustomShape() {
		return this == NONE || this == LEAVES || this == PLANT;
	}

	public Block getBlock() {
		return stateContainer.getOwner();
	}

	public KaleidoTemplate sound(SoundTypeEnum sound) {
		defaultSound = sound;
		return this;
	}

	public KaleidoTemplate renderType(RenderTypeEnum defaultRenderType) {
		defaultRenderTypeFlags = (byte) (1 << defaultRenderType.ordinal());
		return this;
	}

	public static KaleidoTemplate valueOf(String name) {
		return NAME_MAP.getOrDefault(name, NONE);
	}

}
