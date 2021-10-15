package snownee.kaleido.core.client;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.ModList;
import snownee.kaleido.KaleidoCommonConfig;
import snownee.kaleido.compat.ctm.CTMCompat;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.definition.KaleidoBlockDefinition;
import snownee.kaleido.core.definition.SimpleBlockDefinition;
import snownee.kaleido.core.util.KaleidoTemplate;

@SuppressWarnings("deprecation")
@OnlyIn(Dist.CLIENT)
public class KaleidoClient implements IResourceManagerReloadListener {

	public static final Map<ModelInfo, IBakedModel[]> MODEL_MAP = Maps.newIdentityHashMap();
	public static final boolean ctm = ModList.get().isLoaded("ctm");
	public static final Set<RenderType> blockRenderTypes = ImmutableSet.of(RenderType.solid(), RenderType.cutout(), RenderType.cutoutMipped(), RenderType.translucent());

	public static int bgColor = 0xAA212429;

	public static void init() {
		((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(new KaleidoClient());
	}

	@Nullable
	public static synchronized void loadModel(IBakedModel[] models, ModelInfo info, int variant) {
		ModelLoader modelLoader = ModelLoader.instance();
		if (modelLoader == null) {
			return;
		}
		if (info.template != KaleidoTemplate.item && ctm) {
			if (variant == info.template.metaCount) {
				variant = info.template.defaultMeta;
			}
		}
		IBakedModel bakedModel = info.template.loadModel(modelLoader, info, variant);
		if (info.template != KaleidoTemplate.item && ctm) {
			if (info.template.defaultMeta == variant)
				models[info.template.metaCount] = bakedModel;
			bakedModel = CTMCompat.tryWrap(info, variant, bakedModel, modelLoader);
		}
		models[variant] = bakedModel;
	}

	@Nullable
	public static IBakedModel getModel(ModelInfo info, @Nullable BlockState state) {
		int i = state == null ? info.template.defaultMeta : info.template.toMeta(state);
		return getModel(info, i);
	}

	@Nullable
	public static IBakedModel getModel(ModelInfo info, int i) {
		if (i == -1) {
			return null;
		}
		int states = ctm ? info.template.metaCount + 1 : info.template.metaCount;
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
		/* off */
		locations.stream()
				.filter($ -> !KaleidoCommonConfig.ignoredNamespaces.contains($.getNamespace()))
				.map(KaleidoClient::resolveLocation)
				.forEach(consumer::accept);
		/* on */
	}

	private static ResourceLocation resolveLocation(ResourceLocation location) {
		String path = location.getPath();
		// models/*.json
		return new ResourceLocation(location.getNamespace(), path.substring(7, path.length() - 5));
	}

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
		SimpleBlockDefinition.reload();
		KaleidoBlockDefinition.reload();
	}

	public static void fill(MatrixStack pPoseStack, float pMinX, float pMinY, float pMaxX, float pMaxY, int pColor) {
		innerFill(pPoseStack.last().pose(), pMinX, pMinY, pMaxX, pMaxY, pColor);
	}

	private static void innerFill(Matrix4f pMatrix, float pMinX, float pMinY, float pMaxX, float pMaxY, int pColor) {
		if (pMinX < pMaxX) {
			float i = pMinX;
			pMinX = pMaxX;
			pMaxX = i;
		}

		if (pMinY < pMaxY) {
			float j = pMinY;
			pMinY = pMaxY;
			pMaxY = j;
		}

		float f3 = (pColor >> 24 & 255) / 255.0F;
		float f = (pColor >> 16 & 255) / 255.0F;
		float f1 = (pColor >> 8 & 255) / 255.0F;
		float f2 = (pColor & 255) / 255.0F;
		BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
		RenderSystem.enableBlend();
		RenderSystem.disableTexture();
		RenderSystem.defaultBlendFunc();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
		bufferbuilder.vertex(pMatrix, pMinX, pMaxY, 0.0F).color(f, f1, f2, f3).endVertex();
		bufferbuilder.vertex(pMatrix, pMaxX, pMaxY, 0.0F).color(f, f1, f2, f3).endVertex();
		bufferbuilder.vertex(pMatrix, pMaxX, pMinY, 0.0F).color(f, f1, f2, f3).endVertex();
		bufferbuilder.vertex(pMatrix, pMinX, pMinY, 0.0F).color(f, f1, f2, f3).endVertex();
		bufferbuilder.end();
		WorldVertexBufferUploader.end(bufferbuilder);
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}
}
