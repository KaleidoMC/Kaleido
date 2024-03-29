package snownee.kaleido.mixin;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.datafixers.util.Function4;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Hand;
import net.minecraft.util.datafix.codec.DatapackCodec;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.storage.IServerConfiguration;
import net.minecraft.world.storage.SaveFormat;
import net.minecraftforge.client.event.InputEvent.ClickInputEvent;
import snownee.kaleido.Hooks;
import snownee.kaleido.brush.item.BrushItem;
import snownee.kaleido.core.KaleidoDataManager;

@Mixin(Minecraft.class)
public class MixinMinecraft {

	@Shadow
	RayTraceResult hitResult;

	@Inject(at = @At("HEAD"), method = "loadWorld", remap = false)
	private void kaleido_loadWorld(String p_238195_1_, DynamicRegistries.Impl p_238195_2_, Function<SaveFormat.LevelSave, DatapackCodec> p_238195_3_, Function4<SaveFormat.LevelSave, DynamicRegistries.Impl, IResourceManager, DatapackCodec, IServerConfiguration> p_238195_4_, boolean p_238195_5_, Minecraft.WorldSelectionType p_238195_6_, boolean creating, CallbackInfo ci) {
		if (!creating)
			KaleidoDataManager.INSTANCE.skipOnce();
	}

	@Inject(at = @At("HEAD"), method = "pickBlock")
	private void kaleido_pickBlock(CallbackInfo ci) {
		if (Hooks.brushEnabled && hitResult != null && hitResult.getType() == RayTraceResult.Type.MISS) {
			BrushItem.pick(new ClickInputEvent(2, null, Hand.MAIN_HAND));
		}
	}

	@Inject(at = @At("TAIL"), method = "resizeDisplay")
	private void kaleido_resizeDisplay(CallbackInfo ci) {
		Hooks.resizeDisplay();
	}

}
