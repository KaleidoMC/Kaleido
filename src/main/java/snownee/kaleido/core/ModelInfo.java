package snownee.kaleido.core;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

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
import net.minecraft.block.AbstractBlock.OffsetType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import snownee.kaleido.Kaleido;
import snownee.kaleido.KaleidoCommonConfig;
import snownee.kaleido.core.behavior.Behavior;
import snownee.kaleido.core.block.KaleidoBlocks;
import snownee.kaleido.core.block.entity.MasterBlockEntity;
import snownee.kaleido.core.util.KaleidoTemplate;
import snownee.kaleido.core.util.RenderTypeEnum;
import snownee.kaleido.util.KaleidoUtil;
import snownee.kaleido.util.ShapeCache;
import snownee.kiwi.Kiwi;
import snownee.kiwi.util.NBTHelper;

public class ModelInfo implements Comparable<ModelInfo> {

	public ResourceLocation id;
	public KaleidoTemplate template = KaleidoTemplate.none;
	public ImmutableList<Behavior> behaviors = ImmutableList.of();
	private boolean locked = true;
	public int price = 1;
	public boolean reward;
	private String descriptionId;
	public boolean expired;
	public OffsetType offset = OffsetType.NONE;
	public boolean noCollision;
	public boolean glass;
	private HashCode shape;
	private VoxelShape[] shapeCache = ShapeCache.fallback;

	private static final EnumSet<RenderTypeEnum> defaultRenderTypes = EnumSet.of(RenderTypeEnum.solid);
	public EnumSet<RenderTypeEnum> renderTypes = defaultRenderTypes;

	public ResourceLocation getAdvancementId() {
		return new ResourceLocation(Kaleido.MODID, id.toString().replace(':', '/'));
	}

	public String getDescriptionId() {
		if (descriptionId == null) {
			descriptionId = Util.makeDescriptionId("kaleido.decor", id);
			if (FMLEnvironment.dist.isClient()) {
				if (!I18n.exists(descriptionId)) {
					descriptionId = capitaliseAllWords(id.getPath().replace('_', ' ').trim());
				}
			}
		}
		return descriptionId;
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
			return (!KaleidoCommonConfig.autoUnlock && !isAdvancementDone(player));
		}
	}

	public ItemStack makeItem() {
		return makeItem(1);
	}

	public ItemStack makeItem(int size) {
		NBTHelper data = NBTHelper.of(new ItemStack(CoreModule.STUFF, size));
		data.setString(KaleidoBlocks.NBT_ID, id.toString());
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
		if (!template.solid) {
			buf.writeByteArray(shape.asBytes());
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
		info.setLocked(buf.readBoolean());
		info.reward = buf.readBoolean();
		info.price = buf.readByte();
		if (!info.template.solid) {
			info.shape = HashCode.fromBytes(buf.readByteArray());
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
		if (info != null) {
			if (json.has("template"))
				info.template = KaleidoTemplate.valueOf(JSONUtils.getAsString(json, "template"));
			if (json.has("behavior")) {
				info.behaviors = ImmutableList.of(Behavior.fromJson(json.get("behavior")));
			} else if (json.has("behaviors")) {
				ImmutableList.Builder<Behavior> list = ImmutableList.builder();
				for (JsonElement e : JSONUtils.getAsJsonArray(json, "behaviors")) {
					list.add(Behavior.fromJson(e));
				}
				info.behaviors = list.build();
			}
			info.reward = JSONUtils.getAsBoolean(json, "reward", false);
			info.price = JSONUtils.getAsInt(json, "price", 1);
			if (!info.template.solid) {
				if (json.has("shape")) {
					info.shape = KaleidoDataManager.INSTANCE.shapeSerializer.fromJson(json.get("shape"));
					if (info.shape != null) {
						info.shapeCache = null;
					}
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

	@OnlyIn(Dist.CLIENT)
	public boolean canRenderInLayer(RenderType layer) {
		for (RenderTypeEnum e : renderTypes)
			if (e.renderType.get().get() == layer)
				return true;
		return false;
	}

	public static final Cache<GlobalPos, ModelInfo> cache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();

	@Nullable
	public static ModelInfo get(IBlockReader level, BlockPos pos) {
		ModelInfo info = null;
		if (FMLEnvironment.dist.isClient()) {
			level = Minecraft.getInstance().level;
		}
		if (level instanceof World) {
			GlobalPos globalPos = GlobalPos.of(((World) level).dimension(), pos.immutable());
			info = cache.getIfPresent(globalPos);
			if (info == null) {
				TileEntity blockEntity = level.getBlockEntity(pos);
				if (blockEntity instanceof MasterBlockEntity) {
					info = ((MasterBlockEntity) blockEntity).getModelInfo();
					if (info != null)
						cache.put(globalPos, info);
				}
			}
		} else {
			TileEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity instanceof MasterBlockEntity) {
				info = ((MasterBlockEntity) blockEntity).getModelInfo();
			}
		}
		return info;
	}

	public static void invalidateCache(World level, BlockPos pos) {
		cache.invalidate(GlobalPos.of(level.dimension(), pos.immutable()));
	}

}
