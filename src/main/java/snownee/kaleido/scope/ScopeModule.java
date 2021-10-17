package snownee.kaleido.scope;

import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import snownee.kaleido.Hooks;
import snownee.kaleido.core.client.KaleidoClient;
import snownee.kaleido.scope.block.ScopeBlock;
import snownee.kaleido.scope.block.ScopeBlockEntity;
import snownee.kaleido.scope.client.ScopeClient;
import snownee.kaleido.scope.client.model.ScopeModel;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.KiwiModule.Subscriber.Bus;
import snownee.kiwi.Name;
import snownee.kiwi.item.ModBlockItem;

@KiwiModule("scope")
@KiwiModule.Optional
@KiwiModule.Subscriber(Bus.MOD)
public class ScopeModule extends AbstractModule {

	public static final ScopeBlock SCOPE = new ScopeBlock();

	@Name("scope")
	public static final TileEntityType<ScopeBlockEntity> TILE = TileEntityType.Builder.of(ScopeBlockEntity::new, SCOPE).build(null);

	@Override
	protected void init(FMLCommonSetupEvent event) {
		Hooks.scopeEnabled = true;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected void clientInit(FMLClientSetupEvent event) {
		ScopeClient.init();
		ModBlockItem.INSTANT_UPDATE_TILES.add(TILE);
		RenderTypeLookup.setRenderLayer(SCOPE, KaleidoClient.blockRenderTypes::contains);
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void registerModelLoader(ModelRegistryEvent event) {
		ModelLoaderRegistry.registerLoader(RL("scope"), ScopeModel.Loader.INSTANCE);
	}
}
