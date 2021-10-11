package snownee.kaleido.chisel.client.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Predicates;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverride;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.BlockModelConfiguration;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import snownee.kaleido.chisel.block.RetextureBlockEntity;
import snownee.kaleido.core.supplier.BlockDefinition;
import snownee.kiwi.util.NBTHelper;

@EventBusSubscriber(bus = Bus.MOD, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class RetextureModel implements IDynamicBakedModel {
	public static ModelProperty<Map<String, BlockDefinition>> TEXTURES = new ModelProperty<>();
	public static Map<IBakedModel, RetextureModel> CACHES = Maps.newHashMap();

	public static class ModelConfiguration implements IModelConfiguration {

		private final IModelConfiguration baseConfiguration;
		private final Map<String, BlockDefinition> overrides;

		public ModelConfiguration(IModelConfiguration baseConfiguration, Map<String, BlockDefinition> overrides) {
			this.baseConfiguration = baseConfiguration;
			this.overrides = overrides;
		}

		@Override
		public IUnbakedModel getOwnerModel() {
			return baseConfiguration.getOwnerModel();
		}

		@Override
		public String getModelName() {
			return baseConfiguration.getModelName();
		}

		@Override
		public boolean isTexturePresent(String name) {
			return baseConfiguration.isTexturePresent(name);
		}

		@Override
		public RenderMaterial resolveTexture(String name) {
			if (name.charAt(0) == '#') {
				String ref = name.substring(1);
				int i = ref.lastIndexOf('_');
				if (i != -1) {
					String ref0 = ref.substring(0, i);
					BlockDefinition supplier = overrides.get(ref0);
					if (supplier != null) {
						Direction direction = Direction.byName(ref.substring(i + 1));
						return supplier.renderMaterial(direction);
					}
				}
				BlockDefinition supplier = overrides.get(ref);
				if (supplier != null) {
					return supplier.renderMaterial(null);
				}
			}
			return baseConfiguration.resolveTexture(name);
		}

		@Override
		public boolean isShadedInGui() {
			return baseConfiguration.isShadedInGui();
		}

		@Override
		public boolean isSideLit() {
			return baseConfiguration.isSideLit();
		}

		@Override
		public boolean useSmoothLighting() {
			return baseConfiguration.useSmoothLighting();
		}

		@Override
		public ItemCameraTransforms getCameraTransforms() {
			return baseConfiguration.getCameraTransforms();
		}

		@Override
		public IModelTransform getCombinedTransform() {
			return baseConfiguration.getCombinedTransform();
		}

	}

	public static class Geometry implements IModelGeometry<Geometry> {

		private final ResourceLocation loaderId;
		private final Lazy<BlockModel> blockModel;
		private final String particle;
		private final boolean inventory;

		public Geometry(Lazy<BlockModel> blockModel, ResourceLocation loaderId, String particle, boolean inventory) {
			this.blockModel = blockModel;
			this.loaderId = loaderId;
			this.particle = particle;
			this.inventory = inventory;
		}

		@Override
		public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
			return new RetextureModel(bakery, modelTransform, loaderId, blockModel.get().customData, particle, inventory);
		}

		@Override
		public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<com.mojang.datafixers.util.Pair<String, String>> missingTextureErrors) {
			return blockModel.get().getMaterials(modelGetter, missingTextureErrors);
		}

	}

	public static class Loader implements IModelLoader<Geometry> {

		public static final Loader INSTANCE = new Loader();

		private Loader() {
		}

		@Override
		public void onResourceManagerReload(IResourceManager resourceManager) {
		}

		@Override
		public Geometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
			ResourceLocation loaderId = new ResourceLocation(JSONUtils.getAsString(modelContents, "base_loader", "elements"));
			Lazy<BlockModel> blockModel = Lazy.of(() -> (BlockModel) ModelLoader.instance().getModel(new ResourceLocation(JSONUtils.getAsString(modelContents, "base"))));
			return new Geometry(blockModel, loaderId, JSONUtils.getAsString(modelContents, "particle", "0"), JSONUtils.getAsBoolean(modelContents, "inventory", true));
		}

	}

	private final ModelBakery modelLoader;
	private final IModelTransform variant;
	private final ResourceLocation loaderId;
	private ItemOverrideList overrideList;
	private final Cache<String, IBakedModel> baked = CacheBuilder.newBuilder().expireAfterAccess(500L, TimeUnit.SECONDS).build();
	private final BlockModelConfiguration baseConfiguration;
	private final String particleKey;

	public RetextureModel(ModelBakery modelLoader, IModelTransform variant, ResourceLocation loaderId, BlockModelConfiguration baseConfiguration, String particleKey, boolean inventory) {
		this.modelLoader = modelLoader;
		this.variant = variant;
		this.loaderId = loaderId;
		this.baseConfiguration = baseConfiguration;
		overrideList = inventory ? new OverrideList(this) : ItemOverrideList.EMPTY;
		this.particleKey = particleKey;
	}

	@Override
	public boolean useAmbientOcclusion() {
		return baseConfiguration.useSmoothLighting();
	}

	@Override
	public boolean isGui3d() {
		return baseConfiguration.isShadedInGui();
	}

	@Override
	public boolean isCustomRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture(IModelData data) {
		if (data.getData(TEXTURES) != null) {
			BlockDefinition supplier = data.getData(TEXTURES).get(particleKey);
			if (supplier != null) {
				RenderMaterial material = supplier.renderMaterial(null);
				TextureAtlasSprite particle = ModelLoader.defaultTextureGetter().apply(material);
				if (particle.getClass() != MissingTextureSprite.class) {
					return particle;
				}
			}
		}
		return getParticleIcon();
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return modelLoader.getSpriteMap().getSprite(baseConfiguration.resolveTexture("particle"));
	}

	@Override
	public ItemOverrideList getOverrides() {
		return overrideList;
	}

	@Override
	public ItemCameraTransforms getTransforms() {
		return baseConfiguration.getCameraTransforms();
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData extraData) {
		Map<String, BlockDefinition> overrides = extraData.getData(TEXTURES);
		if (overrides == null)
			overrides = Collections.EMPTY_MAP;
		RenderType layer = MinecraftForgeClient.getRenderLayer();
		boolean noSupplier = true;
		if (layer != null) {
			for (BlockDefinition supplier : overrides.values()) {
				if (supplier != null) {
					noSupplier = false;
					if (supplier.canRenderInLayer(layer)) {
						IBakedModel model = getModel(overrides);
						return model.getQuads(state, side, rand, extraData);
					}
				}
			}
		}
		if (layer == null || (noSupplier && layer == RenderType.solid())) {
			IBakedModel model = getModel(overrides);
			return model.getQuads(state, side, rand, extraData);
		}
		return Collections.EMPTY_LIST;
	}

	public IBakedModel getModel(Map<String, BlockDefinition> overrides) {
		String key = generateKey(overrides);
		try {
			return baked.get(key, () -> {
				ModelConfiguration configuration = new ModelConfiguration(baseConfiguration, overrides);
				return baseConfiguration.getCustomGeometry().bake(configuration, modelLoader, ModelLoader.defaultTextureGetter(), variant, overrideList, loaderId);
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Minecraft.getInstance().getModelManager().getMissingModel();
	}

	private static String generateKey(Map<String, BlockDefinition> overrides) {
		if (overrides == null) {
			return "";
		} else {
			return StringUtils.join(overrides.entrySet(), ',');
		}
	}

	public static class OverrideList extends ItemOverrideList {
		private final RetextureModel baked;
		private final Cache<ItemStack, IBakedModel> cache = CacheBuilder.newBuilder().maximumSize(100L).expireAfterWrite(300L, TimeUnit.SECONDS).weakKeys().build();

		public OverrideList(RetextureModel model) {
			baked = model;
		}

		@Override
		public IBakedModel resolve(IBakedModel model, ItemStack stack, ClientWorld worldIn, LivingEntity entityIn) {
			if (model instanceof RetextureModel) {
				try {
					model = cache.get(stack, () -> {
						return baked.getModel(overridesFromItem(stack));
					});
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
			return model;
		}

		public static Map<String, BlockDefinition> overridesFromItem(ItemStack stack) {
			CompoundNBT data = NBTHelper.of(stack).getTag("BlockEntityTag.Overrides");
			if (data == null)
				data = new CompoundNBT();
			Set<String> keySet = data.getAllKeys();
			Map<String, BlockDefinition> overrides = Maps.newHashMapWithExpectedSize(keySet.size());
			keySet.forEach(k -> overrides.put(k, null));
			RetextureBlockEntity.readTextures(overrides, data, Predicates.alwaysTrue());
			return overrides;
		}

		@Override
		public ImmutableList<ItemOverride> getOverrides() {
			return ImmutableList.of();
		}
	}

	@Override
	public boolean usesBlockLight() {
		return baseConfiguration.isSideLit();
	}

	public static int getColor(Map<String, BlockDefinition> textures, BlockState state, IBlockDisplayReader level, BlockPos pos, int index) {
		BlockDefinition supplier = textures.get(Integer.toString(index));
		if (supplier != null)
			return supplier.getColor(state, level, pos, index);
		return -1;
	}
}