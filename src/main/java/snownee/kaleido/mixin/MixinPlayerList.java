package snownee.kaleido.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import snownee.kaleido.core.KaleidoDataManager;
import snownee.kaleido.core.network.SSyncModelsPacket;
import snownee.kiwi.Kiwi;

@Mixin(PlayerList.class)
public class MixinPlayerList {

	@Inject(
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;updatePermissionLevel(Lnet/minecraft/entity/player/ServerPlayerEntity;)V"
			), method = "initializeConnectionToPlayer"
	)
	private void kaleido_initializeConnectionToPlayerHook(NetworkManager netManager, ServerPlayerEntity player, CallbackInfo info) {
		MinecraftServer server = Kiwi.getServer();
		if (!player.level.isClientSide && server != null && !KaleidoDataManager.INSTANCE.allInfos.isEmpty()) {
			if (server.isSingleplayerOwner(player.getGameProfile())) {
				KaleidoDataManager.INSTANCE.syncAllLockInfo(player);
			} else {
				new SSyncModelsPacket(KaleidoDataManager.INSTANCE.allInfos.values()).setPlayer(player).send();
			}
		}
	}

}
