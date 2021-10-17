package snownee.kaleido.brush.network;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import snownee.kaleido.brush.item.BrushItem;
import snownee.kiwi.network.ClientPacket;

public class CConfigureBrushPacket extends ClientPacket {

	private final Hand hand;
	private final String key;

	public CConfigureBrushPacket(Hand hand, String key) {
		this.hand = hand;
		this.key = key;
	}

	public static class Handler extends PacketHandler<CConfigureBrushPacket> {

		@Override
		public CConfigureBrushPacket decode(PacketBuffer buf) {
			return new CConfigureBrushPacket(buf.readEnum(Hand.class), buf.readUtf(64));
		}

		@Override
		public void encode(CConfigureBrushPacket pkt, PacketBuffer buf) {
			buf.writeEnum(pkt.hand);
			buf.writeUtf(pkt.key, 64);
		}

		@Override
		public void handle(CConfigureBrushPacket pkt, Supplier<Context> ctx) {
			ctx.get().enqueueWork(() -> {
				ServerPlayerEntity player = ctx.get().getSender();
				ItemStack stack = player.getItemInHand(pkt.hand);
				if (stack.getItem() instanceof BrushItem) {
					stack.getOrCreateTag().putString("Tint", pkt.key);
				}
			});
			ctx.get().setPacketHandled(true);
		}

	}

}
