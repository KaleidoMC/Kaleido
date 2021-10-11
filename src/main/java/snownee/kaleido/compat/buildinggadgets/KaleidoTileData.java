package snownee.kaleido.compat.buildinggadgets;

import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileDataFactory;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileDataSerializer;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileEntityData;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.UniqueItem;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.UniqueItem.ComparisonMode;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.registries.ForgeRegistryEntry;
import snownee.kaleido.Kaleido;
import snownee.kaleido.core.CoreModule;
import snownee.kaleido.core.KaleidoDataManager;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.block.KaleidoBlocks;
import snownee.kaleido.core.block.entity.MasterBlockEntity;
import snownee.kiwi.util.NBTHelper;

public class KaleidoTileData implements ITileEntityData {

	public static final ResourceLocation ID = new ResourceLocation(Kaleido.MODID, "main");
	private final ResourceLocation id;
	private ModelInfo info;
	private IModelData modelData;

	public KaleidoTileData(ResourceLocation id) {
		this.id = id;
	}

	public ModelInfo getInfo() {
		if (info == null || info.expired) {
			info = KaleidoDataManager.get(id);
			modelData = null;
		}
		return info;
	}

	@OnlyIn(Dist.CLIENT)
	public IModelData getModelData() {
		if (modelData != null)
			return modelData;
		ModelInfo info = getInfo();
		if (info == null)
			return modelData = EmptyModelData.INSTANCE;
		return modelData = info.createModelData();
	}

	@Override
	public ITileDataSerializer getSerializer() {
		return BuildingGadgetsModule.SERIALIZER;
	}

	@Override
	public boolean placeIn(BuildContext ctx, BlockState state, BlockPos pos) {
		ModelInfo info = getInfo();
		if (info == null)
			return false;
		ctx.getWorld().setBlock(pos, state, 0);
		TileEntity te = ctx.getWorld().getBlockEntity(pos);
		if (!(te instanceof MasterBlockEntity))
			return false;
		((MasterBlockEntity) te).setModelInfo(info);
		return true;
	}

	@Override
	public MaterialList getRequiredItems(BuildContext ctx, BlockState state, RayTraceResult hit, BlockPos pos) {
		ModelInfo info = getInfo();
		if (info == null)
			return MaterialList.empty();
		NBTHelper data = NBTHelper.create();
		data.setString(KaleidoBlocks.NBT_ID, id.toString());
		UniqueItem item = new UniqueItem(CoreModule.STUFF_ITEM, data.get(), ComparisonMode.SUB_TAG_MATCH);
		return MaterialList.of(item);
	}

	public static class Serializer extends ForgeRegistryEntry<ITileDataSerializer> implements ITileDataSerializer {

		@Override
		public ITileEntityData deserialize(CompoundNBT tagCompound, boolean persisted) {
			return new KaleidoTileData(new ResourceLocation(tagCompound.getString("Model")));
		}

		@Override
		public CompoundNBT serialize(ITileEntityData data, boolean persisted) {
			CompoundNBT tag = new CompoundNBT();
			tag.putString("Model", ((KaleidoTileData) data).id.toString());
			return tag;
		}

	}

	public static class Factory implements ITileDataFactory {

		@Override
		public ITileEntityData createDataFor(TileEntity blockEntity) {
			if (blockEntity instanceof MasterBlockEntity) {
				ModelInfo info = ((MasterBlockEntity) blockEntity).getModelInfo();
				if (info != null) {
					return new KaleidoTileData(info.id);
				}
			}
			return null;
		}

	}

}
