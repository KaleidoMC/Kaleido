package snownee.kaleido.core.client.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

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
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.block.KaleidoBlock;
import snownee.kaleido.core.client.KaleidoClient;

@OnlyIn(Dist.CLIENT)
public class KaleidoModel implements IDynamicBakedModel {
	public static final ModelProperty<ModelInfo> MODEL = new ModelProperty<>();

	public static class Geometry implements IModelGeometry<Geometry> {

		@Override
		public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
			if (INSTANCE == null) {
				INSTANCE = new KaleidoModel();
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
			ModelInfo info = KaleidoBlock.getInfo(stack);
			if (info == null || Minecraft.getInstance().overlay != null) {
				return null;
			}
			return KaleidoClient.getModel(info, null);
		}
	}

	public static KaleidoModel INSTANCE;

	private static Lazy<IBakedModel> missingno = missingno();

	private static Lazy<IBakedModel> missingno() {
		return Lazy.of(Minecraft.getInstance().getModelManager()::getMissingModel);
	}

	private final Lazy<ItemOverrideList> overrides = Lazy.of(OverrideList::new);

	@Nullable
	public static IBakedModel getModel(ModelInfo info, @Nullable BlockState state, boolean checkLayer) {
		IBakedModel model = null;
		if (info != null) {
			RenderType layer = MinecraftForgeClient.getRenderLayer();
			if (!checkLayer || layer == null || info.canRenderInLayer(layer)) {
				model = KaleidoClient.getModel(info, state);
			} else
				return null;
		}
		return model != null ? model : missingno.get();
	}

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
		return getModel(extraData.getData(MODEL), null, false).getParticleTexture(extraData);
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, Direction side, Random rand, IModelData extraData) {
		if (state == null) {
			return missingno.get().getQuads(null, side, rand, extraData);
		}
		IBakedModel model = getModel(extraData.getData(MODEL), state, true);
		return model == null ? Collections.EMPTY_LIST : model.getQuads(state, side, rand, extraData);
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
