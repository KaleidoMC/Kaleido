package snownee.kaleido.core.client;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelRotation;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelLoader;
import snownee.kaleido.core.ModelInfo;

@OnlyIn(Dist.CLIENT)
public class KaleidoClient {

	public static final Map<ModelInfo, IBakedModel[]> MODEL_MAP = Maps.newIdentityHashMap();

	@Nullable
	public static synchronized IBakedModel loadModel(ResourceLocation id, Direction direction) {
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

	@Nullable
	public static IBakedModel getModel(ModelInfo info, Direction direction) {
		int i = direction.get2DDataValue();
		if (i == -1) {
			return null;
		}
		IBakedModel[] bakedModel = MODEL_MAP.computeIfAbsent(info, $ -> new IBakedModel[4]);
		if (bakedModel[i] == null) {
			bakedModel[i] = loadModel(info.id, direction);
		}
		return bakedModel[i];
	}

	public static void registerModels(Consumer<ResourceLocation> consumer) {
		MODEL_MAP.clear();
		IResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
		Collection<ResourceLocation> locations = resourceManager.listResources("models/kaleido", s -> s.endsWith(".json"));
		locations.stream().map(KaleidoClient::resolveLocation).forEach(consumer::accept);
	}

	private static ResourceLocation resolveLocation(ResourceLocation location) {
		String path = location.getPath();
		// models/*.json
		return new ResourceLocation(location.getNamespace(), path.substring(7, path.length() - 5));
	}

}
