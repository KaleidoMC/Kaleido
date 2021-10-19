package snownee.kaleido.compat.buildinggadgets;

import javax.annotation.Nullable;

import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileDataFactory;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileDataSerializer;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileEntityData;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.UniqueItem;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.UniqueItem.ComparisonMode;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.registries.ForgeRegistryEntry;
import snownee.kaleido.Kaleido;
import snownee.kaleido.chisel.block.entity.ChiseledBlockEntity;
import snownee.kaleido.core.block.entity.MasterBlockEntity;
import snownee.kaleido.core.definition.BlockDefinition;
import snownee.kaleido.core.definition.DynamicBlockDefinition;
import snownee.kaleido.scope.block.ScopeBlockEntity;

public class KaleidoTileData implements ITileEntityData {

	public static final ResourceLocation ID = new ResourceLocation(Kaleido.MODID, "main");
	private BlockDefinition blockDefinition;

	public KaleidoTileData(BlockDefinition blockDefinition) {
		this.blockDefinition = blockDefinition;
	}

	@OnlyIn(Dist.CLIENT)
	public IModelData getModelData() {
		return blockDefinition.modelData();
	}

	@Override
	public ITileDataSerializer getSerializer() {
		return BuildingGadgetsModule.SERIALIZER;
	}

	@Override
	public boolean placeIn(BuildContext ctx, BlockState state, BlockPos pos) {
		blockDefinition.place(ctx.getServerWorld(), pos);
		return true;
	}

	@Override
	public MaterialList getRequiredItems(BuildContext ctx, BlockState state, @Nullable RayTraceResult hit, @Nullable BlockPos pos) {
		ItemStack stack = blockDefinition.createItem(hit, ctx.getWorld(), pos, ctx.getPlayer());
		if (stack.isEmpty())
			return MaterialList.empty();
		UniqueItem item = new UniqueItem(stack.getItem(), stack.getTag(), blockDefinition.getClass() == DynamicBlockDefinition.class ? ComparisonMode.EXACT_MATCH : ComparisonMode.SUB_TAG_MATCH);
		return MaterialList.of(item);
	}

	public static class Serializer extends ForgeRegistryEntry<ITileDataSerializer> implements ITileDataSerializer {

		@Override
		public ITileEntityData deserialize(CompoundNBT tagCompound, boolean persisted) {
			BlockDefinition definition = BlockDefinition.fromNBT(tagCompound.getCompound("Def"));
			return definition == null ? TileSupport.dummyTileEntityData() : new KaleidoTileData(definition);
		}

		@Override
		public CompoundNBT serialize(ITileEntityData data, boolean persisted) {
			CompoundNBT tag = new CompoundNBT();
			CompoundNBT def = new CompoundNBT();
			BlockDefinition definition = ((KaleidoTileData) data).blockDefinition;
			definition.save(def);
			def.putString("Type", definition.getFactory().getId());
			tag.put("Def", def);
			return tag;
		}

	}

	public static class Factory implements ITileDataFactory {

		@Override
		public ITileEntityData createDataFor(TileEntity blockEntity) {
			if (!blockEntity.hasLevel())
				return null;
			if (blockEntity instanceof MasterBlockEntity || blockEntity instanceof ChiseledBlockEntity || blockEntity instanceof ScopeBlockEntity) {
				BlockDefinition blockDefinition = BlockDefinition.fromBlock(blockEntity.getBlockState(), blockEntity, blockEntity.getLevel(), blockEntity.getBlockPos());
				if (blockDefinition != null) {
					return new KaleidoTileData(blockDefinition);
				}
			}
			return null;
		}

	}

}
