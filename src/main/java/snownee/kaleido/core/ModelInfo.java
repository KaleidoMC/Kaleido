package snownee.kaleido.core;

import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.hash.HashCode;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractBlock.OffsetType;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.common.util.JsonUtils;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import snownee.kaleido.Kaleido;
import snownee.kaleido.KaleidoCommonConfig;
import snownee.kaleido.core.action.ActionContext;
import snownee.kaleido.core.behavior.Behavior;
import snownee.kaleido.core.behavior.EventBehavior;
import snownee.kaleido.core.client.KaleidoClient;
import snownee.kaleido.core.client.model.KaleidoModel;
import snownee.kaleido.core.template.KaleidoTemplate;
import snownee.kaleido.core.util.RenderTypeEnum;
import snownee.kaleido.core.util.SoundTypeEnum;
import snownee.kaleido.util.BitBufferHelper;
import snownee.kaleido.util.EnumUtil;
import snownee.kaleido.util.KaleidoUtil;
import snownee.kaleido.util.data.RotatedShapeCache;
import snownee.kiwi.util.NBTHelper;

public class ModelInfo implements Comparable<ModelInfo> {

	public boolean expired;
	public ResourceLocation id;
	public ModelGroup group;
	public KaleidoTemplate template = KaleidoTemplate.NONE;
	public SoundTypeEnum soundType = template.defaultSound;
	public byte renderTypeFlags = template.defaultRenderTypeFlags;
	public ImmutableMap<String, Behavior> behaviors = ImmutableMap.of();
	private boolean locked = true;
	public int price = 1;
	public boolean reward;
	private TranslationTextComponent description;
	public OffsetType offset = OffsetType.NONE;
	public boolean noCollision;
	public boolean glass;
	public String[] tint;
	private RotatedShapeCache.Instance shapes = KaleidoDataManager.INSTANCE.shapeCache.empty();
	public CompoundNBT nbt;
	public byte lightEmission;
	private boolean simple;
	public boolean hide;
	public boolean uvLock;

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
		Advancement advancement = ServerLifecycleHooks.getCurrentServer().getAdvancements().getAdvancement(getAdvancementId());
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
		Advancement advancement = ServerLifecycleHooks.getCurrentServer().getAdvancements().getAdvancement(getAdvancementId());
		return player.getAdvancements().getOrStartProgress(advancement).isDone(); //FIXME do not start progress
	}

	@OnlyIn(Dist.CLIENT)
	public boolean isLocked() {
		return !KaleidoCommonConfig.autoUnlock() && locked;
	}

	public boolean isLockedServer(ServerPlayerEntity player) {
		if (player == null) {
			return false;
		} else if (ServerLifecycleHooks.getCurrentServer().isSingleplayerOwner(player.getGameProfile())) {
			return locked;
		} else {
			return !KaleidoCommonConfig.autoUnlock() && !isAdvancementDone(player);
		}
	}

	public ItemStack makeItem() {
		return makeItem(1);
	}

	public ItemStack makeItem(int size) {
		return makeItemStack(size, id, this);
	}

	public static ItemStack makeItemStack(int size, ResourceLocation id, @Nullable ModelInfo info) {
		ItemStack stack = new ItemStack(CoreModule.STUFF_ITEM, size);
		if (info != null && info.nbt != null)
			stack.setTag(info.nbt.copy());
		NBTHelper data = NBTHelper.of(stack);
		data.setString("Kaleido.Id", id.toString());
		return data.getItem();
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public void toNetwork(PacketBuffer buf, ServerPlayerEntity player, BitBufferHelper bitHelper) {
		buf.writeResourceLocation(id);
		bitHelper.writeBoolean(isLockedServer(player));
		bitHelper.writeBoolean(simple);
		bitHelper.writeBoolean(hide);
		bitHelper.writeBoolean(uvLock);
		bitHelper.writeBits(renderTypeFlags, 4);
		bitHelper.end();

		if (simple) {
			return;
		}

		bitHelper.writeBoolean(noCollision);
		bitHelper.writeBoolean(glass);
		bitHelper.writeBits(offset.ordinal(), 2);
		bitHelper.writeBits(lightEmission, 4);
		bitHelper.end();

		bitHelper.writeBoolean(reward);
		bitHelper.writeBits(price, 7);
		bitHelper.end();

		buf.writeVarInt(template.index);
		buf.writeEnum(soundType);

		buf.writeNbt(nbt);
		if (tint == null) {
			buf.writeVarInt(0);
		} else {
			buf.writeVarInt(tint.length);
			for (String s : tint) {
				buf.writeUtf(s, 64);
			}
		}
		if (template.allowsCustomShape()) {
			buf.writeByteArray(shapes.hashCode.asBytes());
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static ModelInfo fromNetwork(PacketBuffer buf, BitBufferHelper bitHelper) {
		ModelInfo info = new ModelInfo();
		info.id = buf.readResourceLocation();
		info.setLocked(bitHelper.readBoolean());
		info.simple = bitHelper.readBoolean();
		info.hide = bitHelper.readBoolean();
		info.uvLock = bitHelper.readBoolean();
		info.renderTypeFlags = (byte) bitHelper.readBits(4);
		bitHelper.end();

		if (info.simple) {
			return info;
		}

		info.noCollision = bitHelper.readBoolean();
		info.glass = bitHelper.readBoolean();
		info.offset = EnumUtil.OFFSET_TYPES[bitHelper.readBits(2)];
		info.lightEmission = (byte) bitHelper.readBits(4);
		bitHelper.end();

		info.reward = bitHelper.readBoolean();
		info.price = bitHelper.readBits(7);
		bitHelper.end();

		info.template = KaleidoTemplate.VALUES.get(buf.readVarInt());
		info.soundType = buf.readEnum(SoundTypeEnum.class);

		info.nbt = buf.readNbt();
		int l = buf.readVarInt();
		if (l > 0) {
			info.tint = new String[l];
			for (int i = 0; i < l; i++) {
				info.tint[i] = buf.readUtf(64);
			}
		}
		if (info.template.allowsCustomShape()) {
			info.shapes = KaleidoDataManager.INSTANCE.shapeCache.get(HashCode.fromBytes(buf.readByteArray()));
		}
		return info;
	}

	public static ModelInfo fromJson(JsonObject json) {
		ModelInfo info = new ModelInfo();
		if (json.size() == 0) {
			info.simple = true;
			return info;
		}
		if (json.has("template")) {
			info.template = KaleidoTemplate.valueOf(JSONUtils.getAsString(json, "template"));
			info.soundType = info.template.defaultSound;
			info.renderTypeFlags = info.template.defaultRenderTypeFlags;
		}
		ImmutableMap.Builder<String, Behavior> behaviors = ImmutableMap.builder();
		for (Entry<String, JsonElement> entry : json.entrySet()) {
			JsonElement v = entry.getValue();
			switch (entry.getKey()) {
			case "template":
				break;
			case "group":
				info.group = KaleidoDataManager.getGroup(new ResourceLocation(v.getAsString()));
				info.group.infos.add(info);
				break;
			case "sound":
				info.soundType = SoundTypeEnum.valueOf(v.getAsString());
				break;
			case "light":
				info.lightEmission = (byte) v.getAsInt();
				Preconditions.checkArgument(info.lightEmission >= 0 && info.lightEmission <= 15, "light");
				break;
			case "reward":
				info.reward = v.getAsBoolean();
				break;
			case "price":
				info.price = v.getAsInt();
				Preconditions.checkArgument(info.price > 0 && info.price <= 128, "price");
				break;
			case "nbt":
				info.nbt = JsonUtils.readNBT(json, "nbt");
				break;
			case "tint":
				if (v.isJsonPrimitive()) {
					info.tint = new String[] { v.getAsString() };
				} else {
					List<String> tint = Lists.newArrayList();
					for (JsonElement e : v.getAsJsonArray()) {
						tint.add(e.getAsString());
					}
					if (!tint.isEmpty()) {
						info.tint = tint.toArray(new String[0]);
					}
				}
				break;
			case "shape":
				Preconditions.checkArgument(info.template.allowsCustomShape(), "shape");
				info.shapes = KaleidoDataManager.INSTANCE.shapeSerializer.fromJson(v);
				break;
			case "noCollision":
				Preconditions.checkArgument(info.template.allowsCustomShape(), "noCollision");
				info.noCollision = v.getAsBoolean();
				break;
			case "glass":
				Preconditions.checkArgument(!info.template.solid, "glass");
				info.glass = v.getAsBoolean();
				break;
			case "renderType":
				Preconditions.checkArgument(!info.template.solid, "renderType");
				info.renderTypeFlags = 0;
				KaleidoUtil.jsonList(v, $ -> {
					info.renderTypeFlags |= 1 << RenderTypeEnum.valueOf($.getAsString()).ordinal();
				});
				break;
			case "offset":
				Preconditions.checkArgument(!info.template.solid, "offset");
				info.offset = OffsetType.valueOf(v.getAsString().toUpperCase(Locale.ENGLISH));
				break;
			case "uvlock":
				info.uvLock = v.getAsBoolean();
				break;
			default:
				String k = entry.getKey();
				if (k.startsWith("_"))
					break;
				Behavior behavior = Behavior.fromJson(k, v);
				if (behavior != null)
					behaviors.put(k, behavior);
				break;
			}
		}
		info.behaviors = behaviors.build();
		info.simple = info.checkSimple();
		return info;
	}

	private boolean checkSimple() {
		if (reward || price != 1 || lightEmission != 0 || soundType != SoundTypeEnum.wood) {
			return false;
		}
		if (group != null || nbt != null || tint != null || offset != OffsetType.NONE || template != KaleidoTemplate.NONE) {
			return false;
		}
		if (!behaviors.isEmpty() || !shapes.isEmpty()) {
			return false;
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	public VoxelShape getShape(IBlockReader level, BlockState state, BlockPos pos) {
		if (!template.allowsCustomShape())
			return template.getBlock().getShape(state, level, pos, ISelectionContext.empty());
		Direction direction = null;
		if (state.hasProperty(HorizontalBlock.FACING)) {
			direction = state.getValue(HorizontalBlock.FACING);
		} else if (state.hasProperty(DirectionalBlock.FACING)) {
			direction = state.getValue(DirectionalBlock.FACING);
		} else {
			direction = Direction.NORTH;
		}

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
		int i;
		if (layer == RenderType.solid()) {
			i = 0;
		} else if (layer == RenderType.cutout()) {
			i = 1;
		} else if (layer == RenderType.cutoutMipped()) {
			i = 2;
		} else if (layer == RenderType.translucent()) {
			i = 3;
		} else {
			return false;
		}
		return (renderTypeFlags >> i & 1) == 1;
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
		if (offset == OffsetType.NONE)
			return Vector3d.ZERO;
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

	@OnlyIn(Dist.CLIENT)
	public int getItemColor(ItemStack stack, int index) {
		if (tint == null) {
			return -1;
		}
		if (index < 0 || index >= tint.length) {
			index = 0;
		}
		return KaleidoClient.ITEM_COLORS.getColor(tint[index], stack, index);
	}

	@OnlyIn(Dist.CLIENT)
	public int getBlockColor(BlockState state, IBlockDisplayReader level, BlockPos pos, int index) {
		if (tint == null) {
			return -1;
		}
		if (index < 0 || index >= tint.length) {
			index = 0;
		}
		return KaleidoClient.BLOCK_COLORS.getColor(tint[index], state, level, pos, index);
	}

	public ActionResultType fireEvent(String id, ActionContext ctx) {
		Behavior behavior = behaviors.get(id);
		if (behavior instanceof EventBehavior) {
			return ((EventBehavior) behavior).run(ctx);
		}
		return ActionResultType.PASS;
	}

}
