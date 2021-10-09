package snownee.kaleido.core.supplier;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.ints.AbstractInt2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.data.EmptyModelData;
import snownee.kaleido.core.KaleidoDataManager;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.block.entity.MasterBlockEntity;
import snownee.kaleido.core.client.KaleidoClient;
import snownee.kiwi.util.Util;

public class KaleidoModelSupplier implements ModelSupplier {

	public static final String TYPE = "Kaleido";
	public static final Map<Int2ObjectMap.Entry<ModelInfo>, KaleidoModelSupplier> MAP = Maps.newHashMap();

	public static KaleidoModelSupplier of(ModelInfo info, int state) {
		Int2ObjectMap.Entry<ModelInfo> entry = new AbstractInt2ObjectMap.BasicEntry<>(state, info);
		return MAP.computeIfAbsent(entry, KaleidoModelSupplier::new);
	}

	private final Int2ObjectMap.Entry<ModelInfo> entry;
	private ModelInfo modelInfo;

	private KaleidoModelSupplier(Int2ObjectMap.Entry<ModelInfo> entry) {
		this.entry = entry;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public IBakedModel model() {
		return KaleidoClient.getModel(getModelInfo(), entry.getIntKey());
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

	public static KaleidoModelSupplier load(CompoundNBT tag) {
		ResourceLocation id = Util.RL(tag.getString("Id"));
		ModelInfo info = KaleidoDataManager.get(id);
		if (info == null)
			return null;
		return of(info, tag.getInt("State"));
	}

	@Override
	public String toString() {
		return getModelInfo().id + "#" + entry.getIntKey();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public int getColor(BlockState blockState, IBlockDisplayReader level, BlockPos worldPosition, int index) {
		return -1;
	}

	@Override
	public ITextComponent getDescription() {
		return getModelInfo().getDescription();
	}

	@Override
	public void place(World level, BlockPos pos) {
		BlockState state = getBlockState();
		if (state != null) {
			level.setBlockAndUpdate(pos, state);
			TileEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity instanceof MasterBlockEntity) {
				((MasterBlockEntity) blockEntity).setModelInfo(getModelInfo());
			}
		}
	}

	@Override
	public BlockState getBlockState() {
		return getModelInfo().template.fromMeta(entry.getIntKey());
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
