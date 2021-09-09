package snownee.kaleido.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DownloadingPackFinder;
import net.minecraft.resources.IPackNameDecorator;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.ResourcePackList;
import net.minecraftforge.fml.client.ClientModLoader;
import snownee.kaleido.resources.JarPackFinder;

@Mixin(value = ClientModLoader.class, remap = false)
public class MixinClientModLoader {

	@Inject(at = @At("HEAD"), method = "begin")
	private static void kaleido_begin(final Minecraft minecraft, final ResourcePackList defaultResourcePacks, final IReloadableResourceManager mcResourceManager, DownloadingPackFinder metadataSerializer, CallbackInfo ci) {
		defaultResourcePacks.addPackFinder(new JarPackFinder(minecraft.getResourcePackDirectory(), IPackNameDecorator.DEFAULT));
	}

}
