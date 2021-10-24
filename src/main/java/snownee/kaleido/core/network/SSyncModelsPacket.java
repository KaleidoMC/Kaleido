package snownee.kaleido.core.network;

import java.util.Collection;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import snownee.kaleido.core.KaleidoDataManager;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.util.BitBufferHelper;
import snownee.kiwi.network.Packet;

public class SSyncModelsPacket extends Packet {

	public static class Handler extends PacketHandler<SSyncModelsPacket> {

		@Override
		public SSyncModelsPacket decode(PacketBuffer buf) {
			int size = buf.readVarInt();
			ImmutableList.Builder<ModelInfo> builder = ImmutableList.builder();
			BitBufferHelper bitHelper = new BitBufferHelper(buf, false);
			for (int i = 0; i < size; i++) {
				builder.add(ModelInfo.fromNetwork(buf, bitHelper));
			}
			return new SSyncModelsPacket(builder.build());
		}

		@Override
		public void encode(SSyncModelsPacket pkt, PacketBuffer buf) {
			buf.writeVarInt(pkt.infos.size());
			BitBufferHelper bitHelper = new BitBufferHelper(buf, true);
			for (ModelInfo info : pkt.infos) {
				info.toNetwork(buf, pkt.player, bitHelper);
			}
		}

		@Override
		public void handle(SSyncModelsPacket pkt, Supplier<Context> ctx) {
			ctx.get().enqueueWork(() -> {
				KaleidoDataManager.INSTANCE.read(pkt.infos);
			});
			ctx.get().setPacketHandled(true);
		}

		@Override
		public NetworkDirection direction() {
			return NetworkDirection.PLAY_TO_CLIENT;
		}

	}

	public final Collection<ModelInfo> infos;

	private ServerPlayerEntity player;

	public SSyncModelsPacket(Collection<ModelInfo> infos) {
		this.infos = infos;
	}

	@Override
	public void send() {
		send(player);
	}

	public SSyncModelsPacket setPlayer(ServerPlayerEntity player) {
		this.player = player;
		return this;
	}
}
