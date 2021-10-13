package snownee.kaleido.scope.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import snownee.kaleido.Kaleido;
import snownee.kaleido.scope.ScopeModule;
import snownee.kaleido.scope.block.ScopeBlockEntity;
import snownee.kaleido.scope.client.gui.ScopeScreen;
import snownee.kaleido.scope.network.CCreateScopePacket;

@EventBusSubscriber(modid = Kaleido.MODID, value = Dist.CLIENT)
public class ScopeClient {

	private static final KeyBinding scope = new KeyBinding("key.kaleido.scope", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputMappings.Type.KEYSYM.getOrCreate(70), "Kaleido");

	public static void init() {
		ClientRegistry.registerKeyBinding(scope);
		MinecraftForge.EVENT_BUS.addListener(ScopeClient::renderOverlay);
	}

	@SubscribeEvent
	public static void onKeyPressed(InputEvent.KeyInputEvent event) {
		if (event.getAction() != 1)
			return;

		if (scope.isDown()) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.screen != null && mc.gameMode.getPlayerMode().isBlockPlacingRestricted() || mc.hitResult == null || mc.hitResult.getType() != Type.BLOCK) {
				return;
			}
			BlockPos pos = ((BlockRayTraceResult) mc.hitResult).getBlockPos();
			BlockState state = mc.level.getBlockState(pos);
			if (state.is(ScopeModule.SCOPE)) {
				TileEntity blockEntity = mc.level.getBlockEntity(pos);
				if (blockEntity instanceof ScopeBlockEntity) {
					mc.setScreen(new ScopeScreen((ScopeBlockEntity) blockEntity));
				}
			} else {
				new CCreateScopePacket().send();
			}
		}
	}

	private static void renderOverlay(RenderGameOverlayEvent event) {
		Minecraft mc = Minecraft.getInstance();
		if (event.getType() == ElementType.CROSSHAIRS && mc.screen != null && mc.screen.getClass() == ScopeScreen.class) {
			event.setCanceled(true);
		}
	}

}
