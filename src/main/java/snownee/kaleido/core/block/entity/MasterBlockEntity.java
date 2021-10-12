package snownee.kaleido.core.block.entity;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import snownee.kaleido.core.CoreModule;
import snownee.kaleido.core.KaleidoDataManager;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.action.ActionContext;
import snownee.kaleido.core.behavior.Behavior;
import snownee.kaleido.core.client.model.KaleidoModel;
import snownee.kaleido.core.definition.BlockDefinition;
import snownee.kaleido.core.definition.KaleidoBlockDefinition;
import snownee.kiwi.tile.BaseTile;
import snownee.kiwi.util.NBTHelper.NBT;
import snownee.kiwi.util.Util;

public class MasterBlockEntity extends BaseTile {

	public ImmutableList<Behavior> behaviors = ImmutableList.of();
	private IModelData modelData = EmptyModelData.INSTANCE;

	private ResourceLocation modelId;
	private ModelInfo modelInfo;

	public MasterBlockEntity() {
		super(CoreModule.MASTER);
		persistData = true;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		/* off */
		return behaviors.stream()
				.map($ -> $.getCapability(cap, side))
				.filter(LazyOptional::isPresent)
				.findFirst()
				.orElseGet(() -> super.getCapability(cap, side));
		/* on */
	}

	@Override
	public IModelData getModelData() {
		return modelData;
	}

	public ModelInfo getModelInfo() {
		if (modelInfo != null && modelInfo.expired) {
			ModelInfo newInfo = KaleidoDataManager.get(modelInfo.id);
			if (newInfo != null) {
				modelInfo = newInfo;
			}
		}
		return modelInfo;
	}

	@Override
	public void load(BlockState state, CompoundNBT compound) {
		loadInternal(compound);
		super.load(state, compound);
	}

	@Override
	protected void readPacketData(CompoundNBT data) {
		loadInternal(data);
		if (modelInfo != null && level != null) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 8);
		}
	}

	private void loadInternal(CompoundNBT data) {
		if (data.contains("Overrides")) {
			BlockDefinition supplier = BlockDefinition.fromNBT(data.getCompound("Overrides").getCompound("0"));
			if (supplier instanceof KaleidoBlockDefinition) {
				modelId = ((KaleidoBlockDefinition) supplier).getModelInfo().id;
			}
		} else {
			modelId = Util.RL(data.getString("Model"));
		}
		if (modelId != null) {
			ModelInfo info = KaleidoDataManager.get(modelId);
			if (info != null) {
				setModelInfo(info);
				int i = 0;
				for (INBT nbt : data.getList("SubTiles", NBT.COMPOUND)) {
					CompoundNBT subtile = (CompoundNBT) nbt;
					if (behaviors.size() <= i)
						break;
					behaviors.get(i).load(subtile);
					++i;
				}
			}
		}
	}

	@Override
	public void onChunkUnloaded() {
		invalidate();
		super.onChunkUnloaded();
	}

	@Override
	public void setRemoved() {
		invalidate();
		super.setRemoved();
	}

	@Override
	public void setLevelAndPosition(World p_226984_1_, BlockPos p_226984_2_) {
		invalidate();
		super.setLevelAndPosition(p_226984_1_, p_226984_2_);
	}

	public void invalidate() {
		if (level != null && modelInfo != null) {
			ModelInfo.invalidateCache(level, worldPosition);
		}
	}

	public void setModelInfo(ModelInfo modelInfo) {
		this.modelInfo = modelInfo;
		modelId = modelInfo.id;
		if (!modelInfo.behaviors.isEmpty()) {
			ImmutableList.Builder<Behavior> list = ImmutableList.builder();
			for (Behavior behavior : modelInfo.behaviors) {
				list.add(behavior.copy(this));
			}
			behaviors = list.build();
		}
		if (level != null) {
			if (getLightValue() > 0) {
				level.getLightEngine().checkBlock(worldPosition);
			}
			if (level.isClientSide) {
				if (modelData == EmptyModelData.INSTANCE)
					modelData = modelInfo.createModelData();
				else
					modelData.setData(KaleidoModel.MODEL, modelInfo);
				requestModelDataUpdate();
			}
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT compound) {
		writePacketData(compound);
		return super.save(compound);
	}

	@Override
	protected CompoundNBT writePacketData(CompoundNBT data) {
		if (modelId != null) {
			data.putString("Model", modelId.toString());
			if (!behaviors.isEmpty()) {
				ListNBT list = new ListNBT();
				for (Behavior behavior : behaviors) {
					list.add(behavior.save(new CompoundNBT()));
				}
				data.put("SubTiles", list);
			}
		}
		return data;
	}

	public boolean isValid() {
		return getModelInfo() != null;
	}

	public int getLightValue() {
		return behaviors.stream().mapToInt(Behavior::getLightValue).max().orElse(0);
	}

	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		if (!isValid())
			return ActionResultType.PASS;
		ItemStack stack = player.getItemInHand(handIn);
		ActionContext context = new ActionContext(player, handIn, stack, hit, modelInfo);
		for (Behavior behavior : behaviors) {
			ActionResultType resultType = behavior.use(context);
			if (resultType.consumesAction()) {
				return resultType;
			}
		}
		if (stack.isEmpty() && modelInfo.group != null) {
			if (cycleModels()) {
				return ActionResultType.SUCCESS;
			}
		}
		return ActionResultType.PASS;
	}

	public boolean cycleModels() {
		List<ModelInfo> infos = modelInfo.group.infos;
		if (infos.size() < 2)
			return false;
		int i = infos.indexOf(modelInfo);
		if (i < 0)
			return false;
		++i;
		if (i >= infos.size())
			i = 0;
		ModelInfo newInfo = infos.get(i);
		setModelInfo(newInfo);
		refresh();
		return true;
	}

}
