package snownee.kaleido.util.data;

import com.google.gson.JsonObject;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.kiwi.util.Util;

public final class EffectSerializer {

	public static EffectInstance fromJson(JsonObject json) {
		Effect pEffect = ForgeRegistries.POTIONS.getValue(Util.RL(JSONUtils.getAsString(json, "effect")));
		int pDuration = JSONUtils.getAsInt(json, "duration", 0);
		int pAmplifier = JSONUtils.getAsInt(json, "amplifier", 0);
		boolean pAmbient = JSONUtils.getAsBoolean(json, "ambient", false);
		boolean pVisible = JSONUtils.getAsBoolean(json, "visible", true);
		return new EffectInstance(pEffect, pDuration, pAmplifier, pAmbient, pVisible);
	}

}
