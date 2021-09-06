package snownee.kaleido.core.network;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import com.google.common.hash.HashCode;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import snownee.kaleido.util.ShapeCache;
import snownee.kaleido.util.ShapeSerializer;
import snownee.kiwi.network.Packet;

public class SSyncShapesPacket extends Packet {

	private PacketBuffer buf;

	public static class Handler extends PacketHandler<SSyncShapesPacket> {

		private final ShapeCache shapeCache;

		public Handler(ShapeCache shapeCache) {
			this.shapeCache = shapeCache;
		}

		@Override
		public SSyncShapesPacket decode(PacketBuffer buf) {
			SSyncShapesPacket pkt = new SSyncShapesPacket();
			pkt.buf = buf;
			return pkt;
		}

		@Override
		public void encode(SSyncShapesPacket pkt, PacketBuffer buf) {
			Map<HashCode, VoxelShape[]> map = shapeCache.getMap();
			buf.writeVarInt(map.size());
			for (Entry<HashCode, VoxelShape[]> e : map.entrySet()) {
				buf.writeByteArray(e.getKey().asBytes());
				VoxelShape shape = e.getValue()[0];
				ShapeSerializer.toNetwork(buf, shape);
			}
		}

		@Override
		public void handle(SSyncShapesPacket pkt, Supplier<Context> ctx) {
			ctx.get().enqueueWork(() -> {
				shapeCache.getMap().clear();
				int size = pkt.buf.readVarInt();
				for (int i = 0; i < size; i++) {
					shapeCache.put(HashCode.fromBytes(pkt.buf.readByteArray()), ShapeSerializer.fromNetwork(pkt.buf));
				}
			});
			ctx.get().setPacketHandled(true);
		}

	}
}
