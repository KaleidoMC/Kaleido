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
import snownee.kaleido.core.ModelPack;
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
			size = buf.readVarInt();
			ImmutableList.Builder<ModelPack> builder1 = ImmutableList.builder();
			for (int i = 0; i < size; i++) {
				ModelPack pack = new ModelPack(buf.readUtf());
				pack.fromNetwork(buf);
				builder1.add(pack);
			}
			return new SSyncModelsPacket(builder.build(), builder1.build());
		}

		@Override
		public void encode(SSyncModelsPacket pkt, PacketBuffer buf) {
			buf.writeVarInt(pkt.infos.size());
			BitBufferHelper bitHelper = new BitBufferHelper(buf, true);
			for (ModelInfo info : pkt.infos) {
				info.toNetwork(buf, pkt.player, bitHelper);
			}
			buf.writeVarInt(pkt.packs.size());
			for (ModelPack pack : pkt.packs) {
				pack.toNetwork(buf);
			}
		}

		@Override
		public void handle(SSyncModelsPacket pkt, Supplier<Context> ctx) {
			ctx.get().enqueueWork(() -> {
				KaleidoDataManager.INSTANCE.read(pkt.infos, pkt.packs);
			});
			ctx.get().setPacketHandled(true);
		}

		@Override
		public NetworkDirection direction() {
			return NetworkDirection.PLAY_TO_CLIENT;
		}

	}

	public final Collection<ModelInfo> infos;
	public final Collection<ModelPack> packs;

	private ServerPlayerEntity player;

	public SSyncModelsPacket(Collection<ModelInfo> infos, Collection<ModelPack> packs) {
		this.infos = infos;
		this.packs = packs;
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
