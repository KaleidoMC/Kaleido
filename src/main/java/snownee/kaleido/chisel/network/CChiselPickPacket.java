package snownee.kaleido.chisel.network;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import snownee.kaleido.chisel.item.ChiselItem;
import snownee.kaleido.util.KaleidoUtil;
import snownee.kiwi.network.ClientPacket;

public class CChiselPickPacket extends ClientPacket {

	private final Hand hand;

	public CChiselPickPacket(Hand hand) {
		this.hand = hand;
	}

	public static class Handler extends PacketHandler<CChiselPickPacket> {

		@Override
		public CChiselPickPacket decode(PacketBuffer buf) {
			return new CChiselPickPacket(buf.readEnum(Hand.class));
		}

		@Override
		public void encode(CChiselPickPacket pkt, PacketBuffer buf) {
			buf.writeEnum(pkt.hand);
		}

		@Override
		public void handle(CChiselPickPacket pkt, Supplier<Context> ctx) {
			ctx.get().enqueueWork(() -> {
				ServerPlayerEntity player = ctx.get().getSender();
				ItemStack stack = player.getItemInHand(pkt.hand);
				RayTraceResult hitResult = player.pick(KaleidoUtil.getPickRange(player), 0, false);
				if (stack.getItem() instanceof ChiselItem && hitResult.getType() == Type.BLOCK) {
					ChiselItem.pick(player, pkt.hand, stack, (BlockRayTraceResult) hitResult);
				}
			});
			ctx.get().setPacketHandled(true);
		}

	}

}
