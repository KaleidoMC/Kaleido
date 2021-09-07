package snownee.kaleido.core;

import com.google.common.collect.ImmutableSet;
import com.google.common.hash.HashCode;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.block.AbstractBlock.OffsetType;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kaleido.Kaleido;
import snownee.kaleido.KaleidoCommonConfig;
import snownee.kaleido.core.behavior.Behavior;
import snownee.kaleido.core.behavior.NoneBehavior;
import snownee.kaleido.core.block.MasterBlock;
import snownee.kaleido.core.util.KaleidoTemplate;
import snownee.kaleido.core.util.RenderTypeEnum;
import snownee.kaleido.util.KaleidoUtil;
import snownee.kaleido.util.ShapeCache;
import snownee.kiwi.Kiwi;
import snownee.kiwi.util.NBTHelper;

public class ModelInfo implements Comparable<ModelInfo> {

	public ResourceLocation id;
	public KaleidoTemplate template = KaleidoTemplate.none;
	public Behavior behavior = NoneBehavior.INSTANCE;
	private boolean locked = true;
	public int price = 1;
	public boolean reward;
	private String descriptionId;
	public boolean expired;
	public OffsetType offset = OffsetType.NONE;
	public boolean noCollision;
	private HashCode shape;
	private VoxelShape[] shapeCache = ShapeCache.fallback;

	private static final ImmutableSet<RenderType> defaultRenderTypes = ImmutableSet.of(RenderType.solid());
	public ImmutableSet<RenderType> renderTypes = defaultRenderTypes;

	public boolean opposite; // temp

	public ResourceLocation getAdvancementId() {
		return new ResourceLocation(Kaleido.MODID, id.toString().replace(':', '/'));
	}

	public String getDescriptionId() {
		if (descriptionId == null) {
			descriptionId = Util.makeDescriptionId("kaleido.decor", id);
		}
		return descriptionId;
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
		return locked;
	}

	public boolean isLockedServer(ServerPlayerEntity player) {
		if (Kiwi.getServer().isSingleplayerOwner(player.getGameProfile())) {
			return locked;
		} else {
			return !(KaleidoCommonConfig.autoUnlock || isAdvancementDone(player));
		}
	}

	public ItemStack makeItem() {
		return makeItem(1);
	}

	public ItemStack makeItem(int size) {
		NBTHelper data = NBTHelper.of(new ItemStack(CoreModule.STUFF, size));
		data.setString(MasterBlock.NBT_ID, id.toString());
		return data.getItem();
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public void toNetwork(PacketBuffer buf, ServerPlayerEntity player) {
		buf.writeResourceLocation(id);
		buf.writeEnum(template);
		buf.writeBoolean(isLockedServer(player));
		buf.writeBoolean(reward);
		buf.writeByte(price);
		buf.writeEnum(offset);
		if (!template.solid) {
			buf.writeByteArray(shape.asBytes());
			buf.writeBoolean(noCollision);
			buf.writeByte(renderTypes.size());
			for (RenderType renderType : renderTypes) {
				buf.writeByte(RenderTypeEnum.fromType(renderType).ordinal());
			}
		}

		buf.writeBoolean(opposite);
	}

	@OnlyIn(Dist.CLIENT)
	public static ModelInfo fromNetwork(PacketBuffer buf) {
		ModelInfo info = new ModelInfo();
		info.id = buf.readResourceLocation();
		info.template = buf.readEnum(KaleidoTemplate.class);
		info.setLocked(buf.readBoolean());
		info.reward = buf.readBoolean();
		info.price = buf.readByte();
		info.offset = buf.readEnum(OffsetType.class);
		if (!info.template.solid) {
			info.shape = HashCode.fromBytes(buf.readByteArray());
			info.noCollision = buf.readBoolean();
			byte size = buf.readByte();
			ImmutableSet.Builder<RenderType> set = ImmutableSet.builder();
			for (byte i = 0; i < size; i++) {
				set.add(RenderTypeEnum.VALUES[buf.readByte()].renderType);
			}
			info.renderTypes = set.build();
		}

		info.opposite = buf.readBoolean();
		return info;
	}

	public static ModelInfo fromJson(JsonObject json) {
		ModelInfo info = new ModelInfo();
		if (info != null) {
			if (json.has("template"))
				info.template = KaleidoTemplate.valueOf(JSONUtils.getAsString(json, "template"));
			if (json.has("behavior")) {
				info.behavior = KaleidoDataManager.GSON.fromJson(json.get("behavior"), Behavior.class);
			}
			info.reward = JSONUtils.getAsBoolean(json, "reward", false);
			info.price = JSONUtils.getAsInt(json, "price", 1);
			if (json.has("offset"))
				info.offset = OffsetType.valueOf(JSONUtils.getAsString(json, "offset"));
			if (!info.template.solid) {
				if (json.has("shape")) {
					info.shape = KaleidoDataManager.INSTANCE.shapeSerializer.fromJson(json.get("shape"));
					if (info.shape != null) {
						info.shapeCache = null;
					}
				}
				info.noCollision = JSONUtils.getAsBoolean(json, "noCollision", false);
				if (json.has("renderType")) {
					info.renderTypes = ImmutableSet.of(RenderTypeEnum.valueOf(JSONUtils.getAsString(json, "renderType")).renderType);
				} else if (json.has("renderTypes")) {
					JsonArray array = JSONUtils.getAsJsonArray(json, "renderTypes");
					ImmutableSet.Builder<RenderType> set = ImmutableSet.builder();
					for (JsonElement e : array) {
						set.add(RenderTypeEnum.valueOf(e.getAsString()).renderType);
					}
					info.renderTypes = set.build();
				}
			}

			info.opposite = JSONUtils.getAsBoolean(json, "opposite", false);
		}
		return info;
	}

	public VoxelShape getShape(Direction direction) {
		if (shapeCache == null && shapeCache == null) {
			shapeCache = KaleidoDataManager.INSTANCE.shapeCache.get(shape);
		}
		if (direction == null) {
			direction = Direction.NORTH;
		}
		int i = direction.get2DDataValue();
		if (shapeCache[i] == null) {
			KaleidoDataManager.INSTANCE.shapeCache.update(shape, direction);
		}
		return shapeCache[i];
	}

	@Override
	public int compareTo(ModelInfo o) {
		return KaleidoUtil.friendlyCompare(id.getPath(), o.id.getPath());
	}

}
