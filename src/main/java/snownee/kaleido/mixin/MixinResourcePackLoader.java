package snownee.kaleido.mixin;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.resources.ResourcePackList;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.packs.ModFileResourcePack;
import net.minecraftforge.fml.packs.ResourcePackLoader;
import net.minecraftforge.fml.packs.ResourcePackLoader.IPackInfoFinder;
import snownee.kaleido.Hooks;

@Mixin(value = ResourcePackLoader.class, remap = false)
public class MixinResourcePackLoader {

	//TODO: 1.17: AddPackFindersEvent
	@Inject(at = @At("HEAD"), method = "loadResourcePacks")
	private static void kaleido_loadResourcePacks(ResourcePackList resourcePacks, BiFunction<Map<ModFile, ? extends ModFileResourcePack>, BiConsumer<? super ModFileResourcePack, ResourcePackInfo>, IPackInfoFinder> packFinder, CallbackInfo ci) {
		Hooks.loadResourcePacks(resourcePacks);
	}

}
