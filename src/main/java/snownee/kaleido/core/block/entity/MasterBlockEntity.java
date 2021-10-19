package snownee.kaleido.core.block.entity;

import java.util.List;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import snownee.kaleido.brush.item.BrushItem;
import snownee.kaleido.core.CoreModule;
import snownee.kaleido.core.KaleidoDataManager;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.action.ActionContext;
import snownee.kaleido.core.behavior.Behavior;
import snownee.kaleido.core.client.KaleidoClient;
import snownee.kaleido.core.client.model.KaleidoModel;
import snownee.kaleido.core.definition.BlockDefinition;
import snownee.kaleido.core.definition.KaleidoBlockDefinition;
import snownee.kaleido.util.KaleidoUtil;
import snownee.kiwi.tile.BaseTile;
import snownee.kiwi.util.NBTHelper.NBT;
import snownee.kiwi.util.Util;

public class MasterBlockEntity extends BaseTile {

	public ImmutableList<Behavior> behaviors = ImmutableList.of();
	private IModelData modelData = EmptyModelData.INSTANCE;
	protected BELightManager lightManager;

	private ResourceLocation modelId;
	private ModelInfo modelInfo;
	public String[] tint;

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
		if (data.contains("Tint")) {
			ListNBT list = data.getList("Tint", NBT.STRING);
			if (!list.isEmpty()) {
				tint = new String[list.size()]; //TODO don't always create new array
				for (int i = 0; i < tint.length; i++) {
					String s = list.getString(i);
					if (!s.isEmpty()) {
						tint[i] = s;
						KaleidoClient.BLOCK_COLORS.ensureCache(s);
					}
				}
			}
		} else {
			tint = null;
		}
	}

	@Override
	public void onLoad() {
		super.onLoad();
		lightManager = new BELightManager(level, worldPosition, this::getLightRaw);
		lightManager.update();
	}

	protected int getLightRaw() {
		return getModelInfo() == null ? 0 : getModelInfo().lightEmission;
	}

	@Override
	protected void invalidateCaps() {
		invalidate();
		super.invalidateCaps();
		lightManager = null;
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
		if (modelInfo == this.modelInfo)
			return;
		this.modelInfo = modelInfo;
		modelId = modelInfo.id;
		tint = null;
		if (level != null) {
			if (!modelInfo.behaviors.isEmpty()) {
				ImmutableList.Builder<Behavior> list = ImmutableList.builder();
				for (Behavior behavior : modelInfo.behaviors) {
					list.add(behavior.copy(this));
				}
				behaviors = list.build();
			}
			if (lightManager != null) {
				lightManager.set(modelInfo.lightEmission);
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
		if (tint != null) {
			ListNBT list = new ListNBT();
			for (String s : tint) {
				if (s == null)
					s = "";
				list.add(StringNBT.valueOf(s));
			}
			data.put("Tint", list);
		}
		return data;
	}

	public boolean isValid() {
		return getModelInfo() != null;
	}

	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		if (!isValid())
			return ActionResultType.PASS;
		ItemStack stack = player.getItemInHand(handIn);
		if (stack.getItem() instanceof BrushItem) {
			String key = BrushItem.getTint(stack);
			if (Strings.isNullOrEmpty(key)) {
				KaleidoUtil.displayClientMessage(player, true, "msg.kaleido.brushNoColor");
				return ActionResultType.FAIL;
			}
			if (modelInfo.tint == null) {
				KaleidoUtil.displayClientMessage(player, true, "msg.kaleido.brushBlockNotDyeable");
				return ActionResultType.FAIL;
			}
			if (!worldIn.isClientSide) {
				int i = BrushItem.getIndex(stack);
				i = MathHelper.clamp(i, 0, modelInfo.tint.length - 1); //TODO select ui
				if (tint == null) {
					tint = new String[modelInfo.tint.length];
				}
				if (!Objects.equal(tint[i], key)) {
					tint[i] = key;
					refresh();
				}
			}
			return ActionResultType.SUCCESS;
		}
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

	public void attack(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity player) {
		if (!isValid())
			return;
		ItemStack stack = player.getMainHandItem();
		BlockRayTraceResult hit = new BlockRayTraceResult(Vector3d.ZERO, Direction.DOWN, pPos, false);
		ActionContext context = new ActionContext(player, Hand.MAIN_HAND, stack, hit, modelInfo);
		for (Behavior behavior : behaviors) {
			behavior.attack(context);
		}
	}

	public void onProjectileHit(World pLevel, BlockState pState, BlockRayTraceResult pHit, ProjectileEntity pProjectile) {
		if (!isValid())
			return;
		ActionContext context = new ActionContext(pLevel, null, Hand.MAIN_HAND, ItemStack.EMPTY, pHit, modelInfo);
		context.entity = pProjectile;
		for (Behavior behavior : behaviors) {
			behavior.onProjectileHit(context);
		}
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

	public int getColor(BlockState state, IBlockDisplayReader level, BlockPos pos, int i) {
		ModelInfo info = getModelInfo();
		if (info == null) {
			return -1;
		}
		if (tint != null) {
			if (i < 0 || i >= tint.length) {
				i = 0;
			}
			return KaleidoClient.BLOCK_COLORS.getColor(tint[i], state, level, pos, i);
		}
		return info.getBlockColor(state, level, pos, i);
	}

}
