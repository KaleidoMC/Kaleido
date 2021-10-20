package snownee.kaleido.util.data;

import java.util.List;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;

import net.minecraft.item.Food;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kaleido.util.KaleidoUtil;

public final class FoodSerializer {

	@SuppressWarnings("deprecation")
	public static Food fromJson(JsonObject json) {
		Food.Builder builder = new Food.Builder();
		builder.nutrition(JSONUtils.getAsInt(json, "nutrition"));
		builder.saturationMod(JSONUtils.getAsFloat(json, "saturation"));
		if (JSONUtils.getAsBoolean(json, "fast", false)) {
			builder.fast();
		}
		if (JSONUtils.getAsBoolean(json, "alwaysEat", false)) {
			builder.alwaysEat();
		}
		if (JSONUtils.getAsBoolean(json, "meat", false)) {
			builder.meat();
		}
		if (json.has("effect")) {
			KaleidoUtil.jsonList(json.get("effect"), $ -> {
				float f = JSONUtils.getAsFloat($.getAsJsonObject(), "probability", 1);
				builder.effect(EffectSerializer.fromJson($.getAsJsonObject()), f);
			});
		}
		return builder.build();
	}

	@OnlyIn(Dist.CLIENT)
	@SuppressWarnings("deprecation")
	public static Food fromNetwork(PacketBuffer buf) {
		Food.Builder builder = new Food.Builder();
		builder.nutrition(buf.readVarInt());
		builder.saturationMod(buf.readFloat());
		if (buf.readBoolean()) {
			builder.fast();
		}
		if (buf.readBoolean()) {
			builder.alwaysEat();
		}
		if (buf.readBoolean()) {
			builder.meat();
		}
		int size = buf.readVarInt();
		for (int i = 0; i < size; i++) {
			float f = buf.readFloat();
			builder.effect(EffectSerializer.fromNetwork(buf), f);
		}
		return builder.build();
	}

	public static void toNetwork(Food food, PacketBuffer buf) {
		buf.writeVarInt(food.getNutrition());
		buf.writeFloat(food.getSaturationModifier());
		buf.writeBoolean(food.isFastFood());
		buf.writeBoolean(food.canAlwaysEat());
		buf.writeBoolean(food.isMeat());
		List<Pair<EffectInstance, Float>> effects = food.getEffects();
		buf.writeVarInt(effects.size());
		for (Pair<EffectInstance, Float> e : effects) {
			buf.writeFloat(e.getSecond());
			EffectSerializer.toNetwork(e.getFirst(), buf);
		}
	}
}
