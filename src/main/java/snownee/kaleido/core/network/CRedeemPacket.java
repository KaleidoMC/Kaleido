package snownee.kaleido.core.network;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import snownee.kaleido.core.KaleidoDataManager;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.util.KaleidoUtil;
import snownee.kiwi.network.ClientPacket;

public class CRedeemPacket extends ClientPacket {

	public static class Handler extends PacketHandler<CRedeemPacket> {

		@Override
		public CRedeemPacket decode(PacketBuffer buf) {
			return new CRedeemPacket(KaleidoDataManager.INSTANCE.get(buf.readResourceLocation()), buf.readVarInt());
		}

		@Override
		public void encode(CRedeemPacket pkt, PacketBuffer buf) {
			if (pkt.info == null) {
				buf.writeResourceLocation(new ResourceLocation(""));
			} else {
				buf.writeResourceLocation(pkt.info.id);
			}
			buf.writeVarInt(pkt.amount);
		}

		@Override
		public void handle(CRedeemPacket pkt, Supplier<Context> ctx) {
			ctx.get().enqueueWork(() -> {
				if (pkt.info == null || pkt.amount < 1) {
					return;
				}
				ServerPlayerEntity player = ctx.get().getSender();
				int coins = KaleidoUtil.getCoins(player);
				int price = pkt.info.price * pkt.amount;
				if (coins < price) {
					return;
				}
				KaleidoUtil.takeCoins(player, price);
				KaleidoUtil.giveItems(player, pkt.amount, pkt.info.makeItem());
			});
			ctx.get().setPacketHandled(true);
		}

	}

	private final int amount;

	private final ModelInfo info;

	public CRedeemPacket(ModelInfo info, int amount) {
		this.info = info;
		this.amount = amount;
	}
}
