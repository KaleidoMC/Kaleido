package snownee.kaleido.hub.network;

import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import snownee.kiwi.network.Packet;

public class SHubSettingsPacket extends Packet {

	public static class Handler extends PacketHandler<SHubSettingsPacket> {

		@Override
		public void encode(SHubSettingsPacket msg, PacketBuffer buffer) {
			// TODO Auto-generated method stub

		}

		@Override
		public SHubSettingsPacket decode(PacketBuffer buffer) {
			// TODO Auto-generated method stub
			return new SHubSettingsPacket();
		}

		@Override
		public void handle(SHubSettingsPacket msg, Supplier<Context> ctx) {
			// TODO Auto-generated method stub

			ctx.get().setPacketHandled(true);
		}

	}
}
