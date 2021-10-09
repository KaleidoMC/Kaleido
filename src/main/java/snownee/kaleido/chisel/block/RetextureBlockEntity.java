package snownee.kaleido.chisel.block;

import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import snownee.kaleido.chisel.client.model.RetextureModel;
import snownee.kaleido.core.supplier.BlockStateModelSupplier;
import snownee.kaleido.core.supplier.ModelSupplier;
import snownee.kiwi.tile.BaseTile;
import snownee.kiwi.util.NBTHelper;
import snownee.kiwi.util.NBTHelper.NBT;

public abstract class RetextureBlockEntity extends BaseTile {
	@Nullable
	protected Map<String, ModelSupplier> textures;
	protected IModelData modelData = EmptyModelData.INSTANCE;

	public RetextureBlockEntity(TileEntityType<?> tileEntityTypeIn, String... textureKeys) {
		super(tileEntityTypeIn);
		persistData = true;
		textures = textureKeys.length == 0 ? null : Maps.newHashMapWithExpectedSize(textureKeys.length);
		for (String key : textureKeys) {
			textures.put(key, null);
		}
		if (textures != null && EffectiveSide.get().isClient()) {
			modelData = new ModelDataMap.Builder().withInitial(RetextureModel.TEXTURES, textures).build();
		}
	}

	public static void setTexture(Map<String, String> textures, String key, String path) {
		if (!textures.containsKey(key)) {
			return;
		}
		textures.put(key, path);
	}

	public void setTexture(String key, ModelSupplier modelSupplier) {
		if (modelSupplier != null && !isValidTexture(modelSupplier))
			return;
		setTexture(textures, key, modelSupplier);
	}

	public boolean isValidTexture(ModelSupplier modelSupplier) {
		return true;
	}

	public static void setTexture(Map<String, ModelSupplier> textures, String key, ModelSupplier modelSupplier) {
		if (textures == null || !textures.containsKey(key)) {
			return;
		}
		textures.put(key, modelSupplier);
	}

	public static void setTexture(Map<String, ModelSupplier> textures, String key, Item item) {
		Block block = Block.byItem(item);
		if (block != null) {
			setTexture(textures, key, BlockStateModelSupplier.of(block.defaultBlockState()));
		}
	}

	@Override
	public void refresh() {
		if (level != null && level.isClientSide) {
			requestModelDataUpdate();
		} else {
			super.refresh();
		}
	}

	@Override
	public void onLoad() {
		super.requestModelDataUpdate();
	}

	@Override
	public void requestModelDataUpdate() {
		if (textures == null) {
			return;
		}
		super.requestModelDataUpdate();
		if (!remove && level != null && level.isClientSide) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 8);
		}
	}

	@Override
	protected void readPacketData(CompoundNBT data) {
		if (!data.contains("Overrides", NBT.COMPOUND)) {
			return;
		}
		boolean shouldRefresh = readTextures(textures, data.getCompound("Overrides"), this::isValidTexture);
		if (shouldRefresh) {
			refresh();
		}
	}

	public static boolean readTextures(Map<String, ModelSupplier> textures, CompoundNBT data, Predicate<ModelSupplier> validator) {
		if (textures == null) {
			return false;
		}
		boolean shouldRefresh = false;
		NBTHelper helper = NBTHelper.of(data);
		for (String k : textures.keySet()) {
			CompoundNBT v = helper.getTag(k);
			if (v == null)
				continue;
			ModelSupplier supplier = ModelSupplier.fromNBT(v);
			if (supplier != null && !validator.test(supplier))
				continue;
			if (!Objects.equals(textures.get(k), supplier)) {
				shouldRefresh = true;
				textures.put(k, supplier);
			}
		}
		return shouldRefresh;
	}

	@Override
	protected CompoundNBT writePacketData(CompoundNBT data) {
		writeTextures(textures, data);
		return data;
	}

	public static CompoundNBT writeTextures(Map<String, ModelSupplier> textures, CompoundNBT data) {
		if (textures != null) {
			NBTHelper tag = NBTHelper.of(data);
			textures.forEach((k, v) -> {
				if (v == null)
					return;
				CompoundNBT compound = new CompoundNBT();
				compound.putString("Type", v.getType());
				v.save(compound);
				tag.setTag("Overrides." + k, compound);
			});
		}
		return data;
	}

	@Override
	public IModelData getModelData() {
		return modelData;
	}

	@OnlyIn(Dist.CLIENT)
	public int getColor(int index) {
		return RetextureModel.getColor(textures, blockState, level, worldPosition, index);
	}
}
