package snownee.kaleido.core.network;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import snownee.kaleido.carpentry.client.gui.NewModelToast;
import snownee.kaleido.core.KaleidoDataManager;
import snownee.kaleido.core.ModelInfo;
import snownee.kiwi.network.Packet;

public class SUnlockModelsPacket extends Packet {

    private final Collection<ResourceLocation> ids;
    private final boolean toast;

    public SUnlockModelsPacket(Collection<ResourceLocation> ids, boolean toast) {
        this.ids = ids;
        this.toast = toast;
    }

    public static class Handler extends PacketHandler<SUnlockModelsPacket> {

        @Override
        public SUnlockModelsPacket decode(PacketBuffer buf) {
            int size = buf.readVarInt();
            ImmutableList.Builder<ResourceLocation> builder = ImmutableList.builder();
            for (int i = 0; i < size; i++) {
                builder.add(buf.readResourceLocation());
            }
            return new SUnlockModelsPacket(builder.build(), buf.readBoolean());
        }

        @Override
        public void encode(SUnlockModelsPacket pkt, PacketBuffer buf) {
            buf.writeVarInt(pkt.ids.size());
            for (ResourceLocation id : pkt.ids) {
                buf.writeResourceLocation(id);
            }
            buf.writeBoolean(pkt.toast);
        }

        @Override
        public void handle(SUnlockModelsPacket pkt, Supplier<Context> ctx) {
            ctx.get().enqueueWork(() -> {
                List<ItemStack> icons = Lists.newArrayList();
                for (ResourceLocation id : pkt.ids) {
                    ModelInfo info = KaleidoDataManager.INSTANCE.get(id);
                    if (info != null) {
                        info.setLocked(false);
                        if (pkt.toast) {
                            icons.add(info.makeItem());
                        }
                    }
                }
                if (!icons.isEmpty()) {
                    NewModelToast.addOrUpdate(Minecraft.getInstance().getToasts(), icons);
                }
            });
            ctx.get().setPacketHandled(true);
        }

    }

}
