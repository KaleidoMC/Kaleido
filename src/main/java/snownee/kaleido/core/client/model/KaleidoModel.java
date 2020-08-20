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
import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.common.util.Lazy;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.block.MasterBlock;
import snownee.kaleido.core.client.KaleidoClient;
import snownee.kaleido.core.tile.MasterTile;

@OnlyIn(Dist.CLIENT)
public class KaleidoModel implements IDynamicBakedModel {

    public static class Geometry implements IModelGeometry<Geometry> {

        @Override
        public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
            if (INSTANCE == null) {
                INSTANCE = new KaleidoModel();
            }
            return INSTANCE;
        }

        @Override
        public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
            return Collections.EMPTY_LIST;
        }

    }

    public static class Loader implements IModelLoader<Geometry> {

        @SuppressWarnings("hiding")
        public static final Loader INSTANCE = new Loader();

        private Loader() {}

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager) {}

        @Override
        public Geometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
            return new Geometry();
        }

    }

    public static class OverrideList extends ItemOverrideList {

        @Nullable
        @Override
        public IBakedModel getModelWithOverrides(IBakedModel model, ItemStack stack, World worldIn, LivingEntity entityIn) {
            ModelInfo info = MasterBlock.getInfo(stack);
            if (Minecraft.getInstance().loadingGui != null) {
                return null;
            }
            return info != null ? KaleidoClient.getModel(info, Direction.NORTH) : null;
        }

    }

    private static KaleidoModel INSTANCE;

    private static final Lazy<IBakedModel> missingno = Lazy.of(Minecraft.getInstance().getModelManager()::getMissingModel);

    private static IBakedModel getModel(IModelData extraData, Direction direction) {
        IBakedModel model = null;
        if (extraData.getData(MasterTile.MODEL) != null) {
            model = KaleidoClient.getModel(extraData.getData(MasterTile.MODEL), direction);
        }
        return model != null ? model : missingno.get();
    }

    @Override
    public boolean func_230044_c_() {
        return false;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return new OverrideList();
    }

    @Override
    @SuppressWarnings("deprecation")
    public TextureAtlasSprite getParticleTexture() {
        return missingno.get().getParticleTexture();
    }

    @Override
    public TextureAtlasSprite getParticleTexture(IModelData data) {
        return getModel(data, Direction.NORTH).getParticleTexture(data);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, Direction side, Random rand, IModelData extraData) {
        if (state == null) {
            return missingno.get().getQuads(null, side, rand, extraData);
        }
        Direction direction = state.has(HorizontalBlock.HORIZONTAL_FACING) ? state.get(HorizontalBlock.HORIZONTAL_FACING) : Direction.NORTH;
        return getModel(extraData, direction).getQuads(state, side, rand, extraData);
    }

    @Override
    public IBakedModel handlePerspective(TransformType cameraTransformType, MatrixStack mat) {
        // TODO Auto-generated method stub
        return IDynamicBakedModel.super.handlePerspective(cameraTransformType, mat);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isAmbientOcclusion(BlockState state) {
        return state.has(MasterBlock.AO) && state.get(MasterBlock.AO) == Boolean.TRUE;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }
}
