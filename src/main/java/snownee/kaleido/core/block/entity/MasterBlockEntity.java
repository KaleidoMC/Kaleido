package snownee.kaleido.core.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.loading.FMLEnvironment;
import snownee.kaleido.core.CoreModule;
import snownee.kaleido.core.KaleidoDataManager;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.behavior.Behavior;
import snownee.kaleido.core.behavior.NoneBehavior;
import snownee.kiwi.tile.BaseTile;
import snownee.kiwi.util.Util;

public class MasterBlockEntity extends BaseTile {

	public static final ModelProperty<ModelInfo> MODEL = new ModelProperty<>();

	public Behavior behavior = NoneBehavior.INSTANCE;
	private IModelData modelData = FMLEnvironment.dist.isClient() ? new ModelDataMap.Builder().build() : EmptyModelData.INSTANCE;

	private ModelInfo modelInfo;

	public MasterBlockEntity() {
		super(CoreModule.MASTER);
		persistData = true;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		LazyOptional<T> optional = behavior.getCapability(cap, side);
		if (optional.isPresent()) {
			return optional;
		}
		return super.getCapability(cap, side);
	}

	@Override
	public IModelData getModelData() {
		return modelData;
	}

	public ModelInfo getModelInfo() {
		if (modelInfo != null && modelInfo.expired) {
			modelInfo = KaleidoDataManager.get(modelInfo.id);
		}
		return modelInfo;
	}

	@Override
	public void load(BlockState state, CompoundNBT compound) {
		readPacketData(compound);
		super.load(state, compound);
	}

	@Override
	protected void readPacketData(CompoundNBT data) {
		ResourceLocation id = Util.RL(data.getString("Model"));
		if (id != null) {
			ModelInfo info = KaleidoDataManager.get(id);
			if (info != null) {
				setModelInfo(info);
				if (data.contains("SubTile")) {
					behavior.read(data.getCompound("SubTile"));
				}
			}
		}
	}

	@Override
	public void onLoad() {
		// TODO Auto-generated method stub
		super.onLoad();
		//world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 3);
	}

	public void setModelInfo(ModelInfo modelInfo) {
		this.modelInfo = modelInfo;
		behavior = modelInfo.behavior.copy(this);

		if (level != null && level.isClientSide) {
			modelData.setData(MODEL, modelInfo);
			requestModelDataUpdate();
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT compound) {
		writePacketData(compound);
		return super.save(compound);
	}

	@Override
	protected CompoundNBT writePacketData(CompoundNBT data) {
		if (getModelData() != null) {
			data.putString("Model", modelInfo.id.toString());
			if (behavior != null) {
				data.put("SubTile", behavior.write(new CompoundNBT()));
			}
		}
		return data;
	}

	public boolean isValid() {
		return getModelInfo() != null;
	}

}
