package snownee.kaleido.core;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.HashCode;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractBlock.OffsetType;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.common.util.JsonUtils;
import net.minecraftforge.fml.loading.FMLEnvironment;
import snownee.kaleido.Kaleido;
import snownee.kaleido.KaleidoCommonConfig;
import snownee.kaleido.core.behavior.Behavior;
import snownee.kaleido.core.block.KaleidoBlocks;
import snownee.kaleido.core.client.model.KaleidoModel;
import snownee.kaleido.core.util.KaleidoTemplate;
import snownee.kaleido.core.util.RenderTypeEnum;
import snownee.kaleido.core.util.SoundTypeEnum;
import snownee.kaleido.util.KaleidoUtil;
import snownee.kaleido.util.ShapeCache;
import snownee.kiwi.Kiwi;
import snownee.kiwi.util.NBTHelper;

public class ModelInfo implements Comparable<ModelInfo> {

	public ResourceLocation id;
	public ModelGroup group;
	public KaleidoTemplate template = KaleidoTemplate.none;
	public ImmutableList<Behavior> behaviors = ImmutableList.of();
	private boolean locked = true;
	public int price = 1;
	public boolean reward;
	private TranslationTextComponent description;
	public boolean expired;
	public OffsetType offset = OffsetType.NONE;
	public boolean noCollision;
	public boolean glass;
	private ShapeCache.Instance shapes = KaleidoDataManager.INSTANCE.shapeCache.empty();
	public CompoundNBT nbt;
	public SoundTypeEnum soundType = SoundTypeEnum.wood;

	private static final EnumSet<RenderTypeEnum> defaultRenderTypes = EnumSet.of(RenderTypeEnum.solid);
	public EnumSet<RenderTypeEnum> renderTypes = defaultRenderTypes;

	public ResourceLocation getAdvancementId() {
		return new ResourceLocation(Kaleido.MODID, id.toString().replace(':', '/'));
	}

	public TranslationTextComponent getDescription() {
		if (description == null) {
			String descriptionId = Util.makeDescriptionId("kaleido.decor", id);
			if (FMLEnvironment.dist.isClient()) {
				if (!I18n.exists(descriptionId)) {
					String path = id.getPath();
					int p = path.lastIndexOf('/');
					if (p != -1) {
						path = path.substring(p + 1);
					}
					descriptionId = capitaliseAllWords(path.replace('_', ' ').trim());
				}
			}
			description = new TranslationTextComponent(descriptionId);
		}
		return description;
	}

	public static String capitaliseAllWords(String str) {
		int sz = str.length();
		StringBuilder buffer = new StringBuilder(sz);
		boolean space = true;
		for (int i = 0; i < sz; i++) {
			char ch = str.charAt(i);
			if (Character.isWhitespace(ch)) {
				buffer.append(ch);
				space = true;
			} else if (space) {
				buffer.append(Character.toTitleCase(ch));
				space = false;
			} else {
				buffer.append(ch);
			}
		}
		return buffer.toString();
	}

	public boolean grant(ServerPlayerEntity player) {
		Advancement advancement = Kiwi.getServer().getAdvancements().getAdvancement(getAdvancementId());
		if (advancement != null) {
			PlayerAdvancements playerAdvancements = player.getAdvancements();
			AdvancementProgress progress = playerAdvancements.getOrStartProgress(advancement);
			for (String s : progress.getRemainingCriteria()) {
				playerAdvancements.award(advancement, s);
			}
			return true;
		}
		return false;
	}

	public boolean isAdvancementDone(ServerPlayerEntity player) {
		Advancement advancement = Kiwi.getServer().getAdvancements().getAdvancement(getAdvancementId());
		return player.getAdvancements().getOrStartProgress(advancement).isDone(); //FIXME do not start progress
	}

	@OnlyIn(Dist.CLIENT)
	public boolean isLocked() {
		return !KaleidoCommonConfig.autoUnlock && locked;
	}

	public boolean isLockedServer(ServerPlayerEntity player) {
		if (Kiwi.getServer().isSingleplayerOwner(player.getGameProfile())) {
			return locked;
		} else {
			return !KaleidoCommonConfig.autoUnlock && !isAdvancementDone(player);
		}
	}

	public ItemStack makeItem() {
		return makeItem(1);
	}

	public ItemStack makeItem(int size) {
		ItemStack stack = new ItemStack(CoreModule.STUFF_ITEM, size);
		if (nbt != null)
			stack.setTag(nbt.copy());
		NBTHelper data = NBTHelper.of(stack);
		data.setString(KaleidoBlocks.NBT_ID, id.toString());
		return data.getItem();
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public void toNetwork(PacketBuffer buf, ServerPlayerEntity player) {
		buf.writeResourceLocation(id);
		buf.writeEnum(template);
		buf.writeEnum(soundType);
		buf.writeBoolean(isLockedServer(player));
		buf.writeBoolean(reward);
		buf.writeByte(price);
		buf.writeNbt(nbt);
		if (!template.solid) {
			buf.writeByteArray(shapes.hashCode.asBytes());
			buf.writeBoolean(noCollision);
			buf.writeBoolean(glass);
			buf.writeEnum(offset);
			buf.writeByte(renderTypes.size());
			for (RenderTypeEnum renderType : renderTypes) {
				buf.writeEnum(renderType);
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static ModelInfo fromNetwork(PacketBuffer buf) {
		ModelInfo info = new ModelInfo();
		info.id = buf.readResourceLocation();
		info.template = buf.readEnum(KaleidoTemplate.class);
		info.soundType = buf.readEnum(SoundTypeEnum.class);
		info.setLocked(buf.readBoolean());
		info.reward = buf.readBoolean();
		info.price = buf.readByte();
		info.nbt = buf.readNbt();
		if (!info.template.solid) {
			info.shapes = KaleidoDataManager.INSTANCE.shapeCache.get(HashCode.fromBytes(buf.readByteArray()));
			info.noCollision = buf.readBoolean();
			info.glass = buf.readBoolean();
			info.offset = buf.readEnum(OffsetType.class);
			byte size = buf.readByte();
			info.renderTypes = EnumSet.noneOf(RenderTypeEnum.class);
			for (byte i = 0; i < size; i++) {
				info.renderTypes.add(buf.readEnum(RenderTypeEnum.class));
			}
		}
		return info;
	}

	public static ModelInfo fromJson(JsonObject json) {
		ModelInfo info = new ModelInfo();
		if (json.has("group")) {
			info.group = KaleidoDataManager.getGroup(new ResourceLocation(JSONUtils.getAsString(json, "group")));
			info.group.infos.add(info);
		}
		if (json.has("template"))
			info.template = KaleidoTemplate.valueOf(JSONUtils.getAsString(json, "template"));
		if (json.has("sound"))
			info.soundType = SoundTypeEnum.valueOf(JSONUtils.getAsString(json, "sound"));
		ImmutableList.Builder<Behavior> behaviors = ImmutableList.builder();
		if (json.has("behavior")) {
			behaviors.add(Behavior.fromJson(json.get("behavior")));
		}
		if (json.has("behaviors")) {
			for (JsonElement e : json.getAsJsonArray("behaviors")) {
				behaviors.add(Behavior.fromJson(e));
			}
		}
		info.behaviors = behaviors.build();
		info.reward = JSONUtils.getAsBoolean(json, "reward", false);
		info.price = JSONUtils.getAsInt(json, "price", 1);
		info.nbt = JsonUtils.readNBT(json, "nbt");
		if (!info.template.solid) {
			if (json.has("shape")) {
				info.shapes = KaleidoDataManager.INSTANCE.shapeSerializer.fromJson(json.get("shape"));
			}
			info.noCollision = JSONUtils.getAsBoolean(json, "noCollision", false);
			info.glass = JSONUtils.getAsBoolean(json, "glass", false);
			if (json.has("renderType")) {
				info.renderTypes = EnumSet.of(RenderTypeEnum.valueOf(JSONUtils.getAsString(json, "renderType")));
			} else if (json.has("renderTypes")) {
				JsonArray array = JSONUtils.getAsJsonArray(json, "renderTypes");
				info.renderTypes = EnumSet.noneOf(RenderTypeEnum.class);
				for (JsonElement e : array) {
					info.renderTypes.add(RenderTypeEnum.valueOf(e.getAsString()));
				}
			}
			if (json.has("offset"))
				info.offset = OffsetType.valueOf(JSONUtils.getAsString(json, "offset"));
		}
		return info;
	}

	public VoxelShape getShape(Direction direction, BlockPos pos) {
		if (template.solid)
			return VoxelShapes.block();
		if (direction == null)
			direction = Direction.NORTH;

		VoxelShape shape = shapes.get(direction);
		if (!shape.isEmpty() && offset != OffsetType.NONE) {
			Vector3d offset = getOffset(pos);
			shape = shape.move(offset.x, offset.y, offset.z);
		}
		return shape;
	}

	@Override
	public int compareTo(ModelInfo o) {
		return KaleidoUtil.friendlyCompare(id.getPath(), o.id.getPath());
	}

	@OnlyIn(Dist.CLIENT)
	public boolean canRenderInLayer(RenderType layer) {
		for (RenderTypeEnum e : renderTypes)
			if (e.renderType.get().get() == layer)
				return true;
		return false;
	}

	@OnlyIn(Dist.CLIENT)
	public IModelData createModelData() {
		return new ModelDataMap.Builder().withInitial(KaleidoModel.MODEL, this).build();
	}

	public static final Cache<GlobalPos, ModelInfo> cache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();

	public static void invalidateCache(World level, BlockPos pos) {
		cache.invalidate(GlobalPos.of(level.dimension(), pos.immutable()));
	}

	public Vector3d getOffset(BlockPos pos) {
		long i = MathHelper.getSeed(pos.getX(), 0, pos.getZ());
		return new Vector3d(((i & 15L) / 15.0F - 0.5D) * 0.5D, offset == AbstractBlock.OffsetType.XYZ ? ((i >> 4 & 15L) / 15.0F - 1.0D) * 0.2D : 0.0D, ((i >> 8 & 15L) / 15.0F - 0.5D) * 0.5D);
	}

	public boolean outOfBlock() {
		return shapes.outOfBlock;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ModelInfo))
			return false;
		return id.equals(((ModelInfo) obj).id);
	}

}
