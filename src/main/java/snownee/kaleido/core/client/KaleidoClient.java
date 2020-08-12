package snownee.kaleido.core.client;

import java.util.Collection;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelRotation;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(bus = Bus.MOD, value = Dist.CLIENT)
public class KaleidoClient {

    @Nullable
    public static IBakedModel getModel(ResourceLocation id, Direction direction) {
        ModelLoader modelLoader = ModelLoader.instance();
        if (modelLoader == null) {
            return null;
        }
        ModelRotation transform = ModelRotation.X0_Y0;
        if (direction == Direction.SOUTH) {
            transform = ModelRotation.X0_Y180;
        } else if (direction == Direction.WEST) {
            transform = ModelRotation.X0_Y270;
        } else if (direction == Direction.EAST) {
            transform = ModelRotation.X0_Y90;
        }
        return modelLoader.getBakedModel(new ResourceLocation(id.getNamespace(), "kaleido/" + id.getPath()), transform, modelLoader.getSpriteMap()::getSprite);
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        IResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        Collection<ResourceLocation> locations = resourceManager.getAllResourceLocations("models/kaleido", s -> s.endsWith(".json"));
        locations.stream().map(KaleidoClient::resolveLocation).forEach(ModelLoader::addSpecialModel);
    }

    private static ResourceLocation resolveLocation(ResourceLocation location) {
        String path = location.getPath();
        return new ResourceLocation(location.getNamespace(), path.substring(6, path.length() - 5));
    }

}
