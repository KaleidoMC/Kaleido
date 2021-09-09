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
import net.minecraftforge.fml.ModList;
import snownee.kaleido.compat.ctm.CTMCompat;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.util.KaleidoTemplate;

@OnlyIn(Dist.CLIENT)
public class KaleidoClient {

	public static final Map<ModelInfo, IBakedModel[]> MODEL_MAP = Maps.newIdentityHashMap();
	public static boolean ctm = ModList.get().isLoaded("ctm");

	@Nullable
	public static synchronized void loadModel(IBakedModel[] models, ModelInfo info, int variant) {
		ModelLoader modelLoader = ModelLoader.instance();
		if (modelLoader == null) {
			return;
		}
		if (info.template != KaleidoTemplate.item && ctm) {
			if (variant == info.template.states) {
				variant = info.template.defaultState;
			}
		}
		IBakedModel bakedModel = info.template.loadModel(modelLoader, info, variant);
		if (info.template != KaleidoTemplate.item && ctm) {
			if (info.template.defaultState == variant)
				models[info.template.states] = bakedModel;
			bakedModel = CTMCompat.tryWrap(info, variant, bakedModel, modelLoader);
		}
		models[variant] = bakedModel;
	}

	@Nullable
	public static IBakedModel getModel(ModelInfo info, @Nullable BlockState state) {
		int i = state == null ? info.template.defaultState : info.template.getState(state);
		return getModel(info, i);
	}

	@Nullable
	public static IBakedModel getModel(ModelInfo info, int i) {
		if (i == -1) {
			return null;
		}
		int states = ctm ? info.template.states + 1 : info.template.states;
		IBakedModel[] bakedModel = MODEL_MAP.computeIfAbsent(info, $ -> new IBakedModel[states]);
		if (bakedModel[i] == null) {
			loadModel(bakedModel, info, i);
		}
		return bakedModel[i];
	}

	public static void registerModels(Consumer<ResourceLocation> consumer) {
		if (ctm) {
			CTMCompat.wrappedModels.clear();
		}
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
