package snownee.kaleido.scope.client.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.common.util.Lazy;
import snownee.kaleido.core.definition.BlockDefinition;
import snownee.kaleido.scope.ScopeStack;

@OnlyIn(Dist.CLIENT)
public class ScopeModel implements IDynamicBakedModel {
	public static ModelProperty<List<ScopeStack>> STACKS = new ModelProperty<>();

	public static class Geometry implements IModelGeometry<Geometry> {

		@Override
		public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
			if (INSTANCE == null) {
				INSTANCE = new ScopeModel();
			}
			return INSTANCE;
		}

		@Override
		public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
			return Collections.EMPTY_LIST;
		}

	}

	public static class Loader implements IModelLoader<Geometry> {

		@SuppressWarnings("hiding")
		public static final Loader INSTANCE = new Loader();

		private Loader() {
		}

		@Override
		public void onResourceManagerReload(IResourceManager resourceManager) {
			missingno = missingno();
		}

		@Override
		public Geometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
			return new Geometry();
		}

	}

	public static class OverrideList extends ItemOverrideList {

		@Nullable
		@Override
		public IBakedModel resolve(IBakedModel model, ItemStack stack, @Nullable ClientWorld worldIn, @Nullable LivingEntity entityIn) {
			return null;
		}
	}

	public static ScopeModel INSTANCE;

	private static Lazy<IBakedModel> missingno = missingno();

	private static Lazy<IBakedModel> missingno() {
		return Lazy.of(Minecraft.getInstance().getModelManager()::getMissingModel);
	}

	private final Lazy<ItemOverrideList> overrides = Lazy.of(OverrideList::new);

	@Override
	public ItemOverrideList getOverrides() {
		return overrides.get();
	}

	@Override
	@SuppressWarnings("deprecation")
	public TextureAtlasSprite getParticleIcon() {
		return missingno.get().getParticleIcon();
	}

	@Override
	public TextureAtlasSprite getParticleTexture(IModelData extraData) {
		List<ScopeStack> stacks = extraData.getData(STACKS);
		if (stacks == null || stacks.isEmpty()) {
			return getParticleIcon();
		}
		BlockDefinition blockDefinition = stacks.get(0).blockDefinition;
		return blockDefinition.model().getParticleTexture(blockDefinition.modelData());
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, Direction side, Random rand, IModelData extraData) {
		List<ScopeStack> stacks = extraData.getData(STACKS);
		if (stacks == null || stacks.isEmpty()) {
			return missingno.get().getQuads(state, side, rand, extraData);
		}
		RenderType layer = MinecraftForgeClient.getRenderLayer();
		List<BakedQuad> quads = Lists.newArrayListWithExpectedSize(stacks.size());
		for (ScopeStack stack : stacks) {
			BlockDefinition definition = stack.blockDefinition;
			if (layer == null || definition.canRenderInLayer(layer))
				quads.addAll(definition.model().getQuads(state, side, rand, definition.modelData()));
		}
		return quads;
	}

	@Override
	public IBakedModel handlePerspective(TransformType cameraTransformType, MatrixStack mat) {
		return IDynamicBakedModel.super.handlePerspective(cameraTransformType, mat);
	}

	@Override
	public boolean useAmbientOcclusion() {
		return true;
	}

	@Override
	public boolean isCustomRenderer() {
		return false;
	}

	@Override
	public boolean isGui3d() {
		return false;
	}

	@Override
	public boolean usesBlockLight() {
		return false;
	}
}
