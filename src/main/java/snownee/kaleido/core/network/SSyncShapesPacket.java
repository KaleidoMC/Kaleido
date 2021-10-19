package snownee.kaleido.core.network;

import java.util.Collection;
import java.util.function.Supplier;

import com.google.common.hash.HashCode;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import snownee.kaleido.util.data.ShapeCache;
import snownee.kaleido.util.data.ShapeSerializer;
import snownee.kaleido.util.data.ShapeCache.Instance;
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
			Collection<Instance> shapes = shapeCache.getMap().values();
			buf.writeVarInt(shapes.size());
			for (Instance shape : shapes) {
				buf.writeByteArray(shape.hashCode.asBytes());
				ShapeSerializer.toNetwork(buf, shape.shapes[0]);
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
