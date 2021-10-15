package snownee.kaleido.scope.network;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import snownee.kaleido.scope.ScopeStack;
import snownee.kaleido.scope.block.ScopeBlockEntity;
import snownee.kaleido.util.KaleidoUtil;
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
				datalist.add(data);
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
				if (!pkt.pos.closerThan(player.blockPosition(), 16))
					return;
				TileEntity blockEntity = player.level.getBlockEntity(pkt.pos);
				if (blockEntity instanceof ScopeBlockEntity) {
					ScopeBlockEntity scope = (ScopeBlockEntity) blockEntity;
					if (!pkt.data.isEmpty()) {
						List<ScopeStack> toRemove = Lists.newArrayList();
						int size = Math.min(pkt.data.size(), scope.stacks.size());
						for (int i = 0; i < size; i++) {
							Data data = pkt.data.get(i);
							ScopeStack stack = scope.stacks.get(i);
							if (data.removed) {
								toRemove.add(stack);
							} else {
								KaleidoUtil.copyVector(data.position, stack.translation);
								KaleidoUtil.copyVector(data.size, stack.scale);
								KaleidoUtil.copyVector(data.rotation, stack.rotation);
							}
						}
						scope.stacks.removeAll(toRemove);
					}
					scope.refresh();
				}
			});
			ctx.get().setPacketHandled(true);
		}

	}

}
