package snownee.kaleido.scope.network;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import snownee.kiwi.network.ClientPacket;

public class CConfigureScopePacket extends ClientPacket {

	private final BlockPos pos;
	private final List<Data> data; // empty list = cancel (refresh)

	public CConfigureScopePacket(BlockPos pos, List<Data> data) {
		this.pos = pos;
		this.data = data;
	}

	public static class Data {
		public boolean removed;
		public Vector3f position;
		public Vector3f size;
		public Vector3f rotation;
	}

	public static class Handler extends PacketHandler<CConfigureScopePacket> {

		private static Vector3f readVec(PacketBuffer buf) {
			return new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat());
		}

		private static void writeVec(PacketBuffer buf, Vector3f vec) {
			buf.writeFloat(vec.x());
			buf.writeFloat(vec.y());
			buf.writeFloat(vec.z());
		}

		@Override
		public CConfigureScopePacket decode(PacketBuffer buf) {
			BlockPos pos = buf.readBlockPos();
			int size = buf.readVarInt();
			List<Data> datalist = Lists.newArrayListWithCapacity(size);
			for (int i = 0; i < size; i++) {
				Data data = new Data();
				data.removed = buf.readBoolean();
				if (!data.removed) {
					data.position = readVec(buf);
					data.size = readVec(buf);
					data.rotation = readVec(buf);
				}
			}
			return new CConfigureScopePacket(pos, datalist);
		}

		@Override
		public void encode(CConfigureScopePacket pkt, PacketBuffer buf) {
			buf.writeBlockPos(pkt.pos);
			buf.writeVarInt(pkt.data.size());
			for (Data data : pkt.data) {
				buf.writeBoolean(data.removed);
				if (!data.removed) {
					writeVec(buf, data.position);
					writeVec(buf, data.size);
					writeVec(buf, data.rotation);
				}
			}
		}

		@Override
		public void handle(CConfigureScopePacket pkt, Supplier<Context> ctx) {
			ctx.get().enqueueWork(() -> {
				PlayerEntity player = ctx.get().getSender();
			});
			ctx.get().setPacketHandled(true);
		}

	}

}
