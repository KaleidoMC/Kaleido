package snownee.kaleido.core.block.entity;

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
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.loading.FMLEnvironment;
import snownee.kaleido.core.CoreModule;
import snownee.kaleido.core.KaleidoDataManager;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.action.ActionContext;
import snownee.kaleido.core.behavior.Behavior;
import snownee.kiwi.tile.BaseTile;
import snownee.kiwi.util.NBTHelper.NBT;
import snownee.kiwi.util.Util;

public class MasterBlockEntity extends BaseTile {

	public static final ModelProperty<ModelInfo> MODEL = new ModelProperty<>();

	public ImmutableList<Behavior> behaviors = ImmutableList.of();
	private IModelData modelData = FMLEnvironment.dist.isClient() ? new ModelDataMap.Builder().build() : EmptyModelData.INSTANCE;

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
	public void onLoad() {
		super.onLoad();
		//world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 3);
	}

	public void setModelInfo(ModelInfo modelInfo) {
		this.modelInfo = modelInfo;
		if (!modelInfo.behaviors.isEmpty()) {
			ImmutableList.Builder<Behavior> list = ImmutableList.builder();
			for (Behavior behavior : modelInfo.behaviors) {
				list.add(behavior.copy(this));
			}
			behaviors = list.build();
		}
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
		if (getModelInfo() != null) {
			data.putString("Model", modelInfo.id.toString());
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
		ItemStack stack = player.getItemInHand(handIn);
		ActionContext context = new ActionContext(player, handIn, stack, hit);
		for (Behavior behavior : behaviors) {
			ActionResultType resultType = behavior.use(context);
			if (resultType.consumesAction()) {
				return resultType;
			}
		}
		return ActionResultType.PASS;
	}

}
