package snownee.kaleido.core.network;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonNull;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import snownee.kaleido.core.KaleidoDataManager;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.behavior.Behavior;
import snownee.kiwi.network.Packet;

public class SSyncBehaviorsPacket extends Packet {

	public static class Handler extends PacketHandler<SSyncBehaviorsPacket> {

		@Override
		public SSyncBehaviorsPacket decode(PacketBuffer buf) {
			return new SSyncBehaviorsPacket(buf);
		}

		@Override
		public void encode(SSyncBehaviorsPacket pkt, PacketBuffer buf) {
			buf.writeVarInt(pkt.clientBehaviorModelsCount);
			for (ModelInfo info : pkt.infos) {
				if (info.behaviors.isEmpty())
					continue;
				List<Entry<String, Behavior>> entries = info.behaviors.entrySet().stream().filter($ -> $.getValue().syncClient()).collect(Collectors.toList());
				if (entries.isEmpty())
					continue;
				buf.writeVarInt(entries.size());
				for (Entry<String, Behavior> entry : entries) {
					if (entry.getValue().syncClient()) {
						buf.writeUtf(entry.getKey(), 32);
						entry.getValue().toNetwork(buf);
					}
				}
			}
		}

		@Override
		public void handle(SSyncBehaviorsPacket pkt, Supplier<Context> ctx) {
			ctx.get().enqueueWork(() -> {
				int size = pkt.buf.readVarInt();
				for (int i = 0; i < size; i++) {
					ModelInfo info = KaleidoDataManager.get(pkt.buf.readResourceLocation());
					if (info == null) {
						continue; //???
					}
					ImmutableMap.Builder<String, Behavior> builder = ImmutableMap.builder();
					int size2 = pkt.buf.readVarInt();
					for (int j = 0; j < size2; j++) {
						String k = pkt.buf.readUtf(32);
						Behavior v = Behavior.fromJson(k, JsonNull.INSTANCE);
						v.fromNetwork(pkt.buf);
						builder.put(k, v);
					}
					info.behaviors = builder.build();
				}
			});
			ctx.get().setPacketHandled(true);
		}

	}

	public int clientBehaviorModelsCount;
	public Collection<ModelInfo> infos;
	private ServerPlayerEntity player;
	private PacketBuffer buf;

	public SSyncBehaviorsPacket(PacketBuffer buf) {
		this.buf = buf;
	}

	public SSyncBehaviorsPacket(int clientBehaviorModelsCount, Collection<ModelInfo> infos) {
		this.clientBehaviorModelsCount = clientBehaviorModelsCount;
		this.infos = infos;
	}

	@Override
	public void send() {
		send(player);
	}

	public SSyncBehaviorsPacket setPlayer(ServerPlayerEntity player) {
		this.player = player;
		return this;
	}
}
