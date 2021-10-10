package snownee.kaleido.scope.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import snownee.kaleido.Kaleido;
import snownee.kaleido.scope.ScopeModule;
import snownee.kaleido.scope.network.CCreateScopePacket;

@EventBusSubscriber(modid = Kaleido.MODID, value = Dist.CLIENT)
public class ScopeClient {

	private static final KeyBinding scope = new KeyBinding("key.kaleido.scope", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputMappings.Type.KEYSYM.getOrCreate(70), "Kaleido");

	public static void init() {
		ClientRegistry.registerKeyBinding(scope);
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
			BlockState state = mc.level.getBlockState(((BlockRayTraceResult) mc.hitResult).getBlockPos());
			if (state.is(ScopeModule.SCOPE)) {

			} else {
				new CCreateScopePacket().send();
			}
		}
	}

}
