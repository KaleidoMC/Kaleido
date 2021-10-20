package snownee.kaleido.util.data;

import com.google.gson.JsonObject;

import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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

	@OnlyIn(Dist.CLIENT)
	public static EffectInstance fromNetwork(PacketBuffer buf) {
		Effect pEffect = buf.readRegistryIdUnsafe(ForgeRegistries.POTIONS);
		int pDuration = buf.readVarInt();
		int pAmplifier = buf.readVarInt();
		boolean pAmbient = buf.readBoolean();
		boolean pVisible = buf.readBoolean();
		return new EffectInstance(pEffect, pDuration, pAmplifier, pAmbient, pVisible);
	}

	public static void toNetwork(EffectInstance effect, PacketBuffer buf) {
		buf.writeRegistryIdUnsafe(ForgeRegistries.POTIONS, effect.getEffect());
		buf.writeVarInt(effect.getDuration());
		buf.writeVarInt(effect.getAmplifier());
		buf.writeBoolean(effect.isAmbient());
		buf.writeBoolean(effect.isVisible());
	}
}
