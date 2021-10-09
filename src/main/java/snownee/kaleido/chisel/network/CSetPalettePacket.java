package snownee.kaleido.chisel.network;

import java.util.function.Supplier;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import snownee.kaleido.chisel.ChiselPalette;
import snownee.kaleido.chisel.item.ChiselItem;
import snownee.kiwi.network.ClientPacket;

public class CSetPalettePacket extends ClientPacket {

	private final boolean mainhand;
	private final String palette;

	public CSetPalettePacket(Hand hand, ChiselPalette palette) {
		this(hand == Hand.MAIN_HAND, palette.name);
	}

	public CSetPalettePacket(boolean mainhand, String palette) {
		this.mainhand = mainhand;
		this.palette = palette;
	}

	public static class Handler extends PacketHandler<CSetPalettePacket> {

		@Override
		public CSetPalettePacket decode(PacketBuffer buf) {
			return new CSetPalettePacket(buf.readBoolean(), buf.readUtf(32));
		}

		@Override
		public void encode(CSetPalettePacket pkt, PacketBuffer buf) {
			buf.writeBoolean(pkt.mainhand);
			buf.writeUtf(pkt.palette, 32);
		}

		@Override
		public void handle(CSetPalettePacket pkt, Supplier<Context> ctx) {
			ctx.get().enqueueWork(() -> {
				ItemStack stack = ctx.get().getSender().getItemInHand(pkt.mainhand ? Hand.MAIN_HAND : Hand.OFF_HAND);
				if (stack.getItem() instanceof ChiselItem) {
					stack.getOrCreateTag().putString("Palette", pkt.palette);
				}
			});
			ctx.get().setPacketHandled(true);
		}

	}

}
