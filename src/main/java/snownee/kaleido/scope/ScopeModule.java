package snownee.kaleido.scope;

import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import snownee.kaleido.core.client.KaleidoClient;
import snownee.kaleido.scope.block.ScopeBlock;
import snownee.kaleido.scope.block.ScopeBlockEntity;
import snownee.kaleido.scope.client.ScopeClient;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.Name;
import snownee.kiwi.item.ModBlockItem;

@KiwiModule("scope")
public class ScopeModule extends AbstractModule {

	public static final ScopeBlock SCOPE = new ScopeBlock();

	@Name("scope")
	public static final TileEntityType<ScopeBlockEntity> TILE = TileEntityType.Builder.of(ScopeBlockEntity::new, SCOPE).build(null);

	@Override
	protected void clientInit(FMLClientSetupEvent event) {
		ScopeClient.init();
		ModBlockItem.INSTANT_UPDATE_TILES.add(TILE);
		RenderTypeLookup.setRenderLayer(SCOPE, KaleidoClient.blockRenderTypes::contains);
	}
}
