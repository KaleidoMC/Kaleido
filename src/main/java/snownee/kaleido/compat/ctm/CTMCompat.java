package snownee.kaleido.compat.ctm;

import java.io.IOException;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import snownee.kaleido.core.ModelInfo;
import team.chisel.ctm.client.model.AbstractCTMBakedModel;
import team.chisel.ctm.client.model.ModelBakedCTM;
import team.chisel.ctm.client.model.ModelCTM;
import team.chisel.ctm.client.texture.IMetadataSectionCTM;
import team.chisel.ctm.client.util.ResourceUtil;

public class CTMCompat {

	public static final Set<ResourceLocation> wrappedModels = Sets.newHashSet();

	// TextureMetadataHandler
	public static IBakedModel tryWrap(ModelInfo info, BlockState state, IBakedModel bakedModel, ModelLoader modelLoader) {
		if (bakedModel instanceof AbstractCTMBakedModel || bakedModel.isCustomRenderer()) { // Nothing we can add to builtin models
			return bakedModel;
		}
		ResourceLocation location = info.template.getModelLocation(info, state);
		IUnbakedModel unbakedModel = modelLoader.getModel(location);
		boolean shouldWrap = wrappedModels.contains(location);
		// Breadth-first loop through dependencies, exiting as soon as a CTM texture is found, and skipping duplicates/cycles
		if (!shouldWrap) {
			Set<RenderMaterial> textures = Sets.newHashSet(unbakedModel.getMaterials(modelLoader::getModel, Sets.newHashSet()));
			for (RenderMaterial tex : textures) {
				IMetadataSectionCTM meta = null;
				// Cache all dependent texture metadata
				try {
					meta = ResourceUtil.getMetadata(ResourceUtil.spriteToAbsolute(tex.texture()));
				} catch (IOException e) {
				} // Fallthrough
				if (meta != null) {
					// At least one texture has CTM metadata, so we should wrap this model
					shouldWrap = true;
					wrappedModels.add(location);
					break;
				}
			}
		}
		if (shouldWrap) {
			ModelCTM modelchisel = new ModelCTM(unbakedModel);
			modelchisel.initializeTextures(modelLoader, m -> Minecraft.getInstance().getTextureAtlas(m.atlasLocation()).apply(m.texture()));
			bakedModel = new ModelBakedCTM(modelchisel, bakedModel);
		}
		return bakedModel;
	}

}
