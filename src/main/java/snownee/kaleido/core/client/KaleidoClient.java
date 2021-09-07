package snownee.kaleido.core.client;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelLoader;
import snownee.kaleido.core.ModelInfo;

@OnlyIn(Dist.CLIENT)
public class KaleidoClient {

	public static final Map<ModelInfo, IBakedModel[]> MODEL_MAP = Maps.newIdentityHashMap();

	@Nullable
	public static synchronized IBakedModel loadModel(ModelInfo info, int variant) {
		ModelLoader modelLoader = ModelLoader.instance();
		if (modelLoader == null) {
			return null;
		}
		return info.template.loadModel(modelLoader, info, variant);
	}

	@Nullable
	public static IBakedModel getModel(ModelInfo info, @Nullable BlockState state) {
		int i = state == null ? info.template.defaultState : info.template.getState(state);
		if (i == -1) {
			return null;
		}
		IBakedModel[] bakedModel = MODEL_MAP.computeIfAbsent(info, $ -> new IBakedModel[info.template.states]);
		if (bakedModel[i] == null) {
			bakedModel[i] = loadModel(info, i);
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
