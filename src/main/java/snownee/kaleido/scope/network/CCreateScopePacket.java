package snownee.kaleido.scope.network;

import java.util.function.Supplier;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import snownee.kaleido.scope.ScopeModule;
import snownee.kaleido.scope.block.ScopeBlockEntity;
import snownee.kiwi.network.ClientPacket;

public class CCreateScopePacket extends ClientPacket {

	public static class Handler extends PacketHandler<CCreateScopePacket> {

		@Override
		public CCreateScopePacket decode(PacketBuffer buf) {
			return new CCreateScopePacket();
		}

		@Override
		public void encode(CCreateScopePacket pkt, PacketBuffer buf) {
		}

		@Override
		public void handle(CCreateScopePacket pkt, Supplier<Context> ctx) {
			ctx.get().enqueueWork(() -> {
				PlayerEntity player = ctx.get().getSender();
				double reach = player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue();
				RayTraceResult hitResult = player.pick(reach, 0, false);
				if (hitResult instanceof BlockRayTraceResult) {
					BlockPos pos = ((BlockRayTraceResult) hitResult).getBlockPos();
					BlockState state = player.level.getBlockState(pos);
					if (state.is(ScopeModule.SCOPE)) {
						return;
					}
					player.level.setBlockAndUpdate(pos, ScopeModule.SCOPE.defaultBlockState());
					TileEntity blockEntity = player.level.getBlockEntity(pos);
					if (blockEntity instanceof ScopeBlockEntity) {
						((ScopeBlockEntity) blockEntity).addStack(state);
					}
				}
			});
			ctx.get().setPacketHandled(true);
		}

	}

}
