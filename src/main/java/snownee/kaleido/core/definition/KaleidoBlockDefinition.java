package snownee.kaleido.core.definition;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.ints.AbstractInt2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import snownee.kaleido.core.CoreModule;
import snownee.kaleido.core.KaleidoDataManager;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.block.KaleidoBlock;
import snownee.kaleido.core.block.entity.MasterBlockEntity;
import snownee.kaleido.core.client.KaleidoClient;
import snownee.kiwi.util.NBTHelper;
import snownee.kiwi.util.Util;

public class KaleidoBlockDefinition implements BlockDefinition {

	public enum Factory implements BlockDefinition.Factory<KaleidoBlockDefinition> {
		INSTANCE;

		@Override
		public KaleidoBlockDefinition fromNBT(CompoundNBT tag) {
			ResourceLocation id = Util.RL(tag.getString("Id"));
			ModelInfo info = KaleidoDataManager.get(id);
			if (info == null)
				return null;
			return of(info, tag.getInt("State"));
		}

		@Override
		public KaleidoBlockDefinition fromBlock(BlockState state, TileEntity blockEntity, IWorldReader level, BlockPos pos) {
			ModelInfo info = null;
			if (blockEntity instanceof MasterBlockEntity) {
				info = ((MasterBlockEntity) blockEntity).getModelInfo();
			}
			if (info == null)
				return null;
			return of(info, info.template.toMeta(state));
		}

		@Override
		public KaleidoBlockDefinition fromItem(ItemStack stack, BlockItemUseContext context) {
			ModelInfo info = KaleidoBlock.getInfo(stack);
			if (info == null)
				return null;
			BlockState state = info.template.bloc.getStateForPlacement(context);
			if (state == null) {
				return null;
			}
			return of(info, info.template.toMeta(state));
		}

		@Override
		public String getId() {
			return TYPE;
		}

	}

	public static final String TYPE = "Kaleido";
	public static final Map<Int2ObjectMap.Entry<ModelInfo>, KaleidoBlockDefinition> MAP = Maps.newHashMap();

	public static KaleidoBlockDefinition of(ModelInfo info, int state) {
		Int2ObjectMap.Entry<ModelInfo> entry = new AbstractInt2ObjectMap.BasicEntry<>(state, info);
		return MAP.computeIfAbsent(entry, KaleidoBlockDefinition::new);
	}

	private final Int2ObjectMap.Entry<ModelInfo> entry;
	private ModelInfo modelInfo;
	@OnlyIn(Dist.CLIENT)
	private IModelData modelData;

	private KaleidoBlockDefinition(Int2ObjectMap.Entry<ModelInfo> entry) {
		this.entry = entry;
		modelInfo = entry.getValue();
	}

	@Override
	public BlockDefinition.Factory<?> getFactory() {
		return Factory.INSTANCE;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public IBakedModel model() {
		return KaleidoClient.getModel(getModelInfo(), entry.getIntKey());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public IModelData modelData() {
		if (modelData == null) {
			modelData = getModelInfo().createModelData();
		}
		return modelData;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public RenderMaterial renderMaterial(Direction direction) {
		IBakedModel model = model();
		Random random = new Random();
		random.setSeed(42L);
		ResourceLocation particleIcon = model.getParticleTexture(EmptyModelData.INSTANCE).getName();
		ResourceLocation sprite = particleIcon;
		if (direction != null) {
			List<BakedQuad> quads = model.getQuads(null, direction, random, EmptyModelData.INSTANCE);
			if (quads.isEmpty())
				quads = model.getQuads(null, null, random, EmptyModelData.INSTANCE);
			for (BakedQuad quad : quads) {
				if (quad.getDirection() != direction)
					continue;
				sprite = quad.getSprite().getName();
				if (sprite.equals(particleIcon)) {
					break;
				}
			}
		}
		return ModelLoaderRegistry.blockMaterial(sprite);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean canRenderInLayer(RenderType layer) {
		return getModelInfo().canRenderInLayer(layer);
	}

	@Override
	public boolean canOcclude() {
		return getModelInfo().template.solid;
	}

	@Override
	public void save(CompoundNBT tag) {
		tag.putString("Id", getModelInfo().id.toString());
		tag.putInt("State", entry.getIntKey());
	}

	@Override
	public String toString() {
		return getModelInfo().id + "#" + entry.getIntKey();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public int getColor(BlockState blockState, IBlockDisplayReader level, BlockPos worldPosition, int index) {
		return getModelInfo().getBlockColor(blockState, level, worldPosition, index);
	}

	@Override
	public ITextComponent getDescription() {
		return getModelInfo().getDescription();
	}

	@Override
	public boolean place(World level, BlockPos pos) {
		BlockState state = getBlockState();
		if (state != null) {
			level.setBlockAndUpdate(pos, state);
			TileEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity instanceof MasterBlockEntity) {
				((MasterBlockEntity) blockEntity).setModelInfo(getModelInfo());
				return true;
			}
		}
		return false;
	}

	@Override
	public ItemStack createItem(RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		NBTHelper data = NBTHelper.of(new ItemStack(CoreModule.STUFF_ITEM));
		data.setString("Kaleido.Id", getModelInfo().id.toString());
		return data.getItem();
	}

	@Override
	public BlockState getBlockState() {
		return getModelInfo().template.fromMeta(entry.getIntKey());
	}

	@Override
	public SoundType getSoundType() {
		return getModelInfo().soundType.soundType;
	}

	@Override
	public int getLightValue(IBlockReader level, BlockPos pos) {
		return getModelInfo().lightEmission;
	}

	public ModelInfo getModelInfo() {
		if (modelInfo.expired) {
			ModelInfo info = KaleidoDataManager.get(entry.getValue().id);
			if (info != null) {
				modelInfo = info;
			}
		}
		return modelInfo;
	}

	public static void reload() {
		MAP.clear();
	}

}
