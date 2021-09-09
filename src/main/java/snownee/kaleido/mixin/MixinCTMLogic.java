package snownee.kaleido.mixin;

import java.util.Optional;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import snownee.kaleido.Kaleido;
import snownee.kaleido.compat.ctm.Hooks;
import team.chisel.ctm.Configurations;
import team.chisel.ctm.client.util.CTMLogic;

@Mixin(value = CTMLogic.class, remap = false)
public abstract class MixinCTMLogic {

	@Shadow
	public Optional<Boolean> disableObscuredFaceCheck;

	@Inject(
			at = @At(
				"HEAD"
			), method = "isConnected(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/Direction;Lnet/minecraft/block/BlockState;)Z", cancellable = true
	)
	public void kaleido_isConnected(IBlockReader world, BlockPos current, BlockPos connection, Direction dir, BlockState state, CallbackInfoReturnable<Boolean> ci) {

		BlockPos obscuringPos = connection.relative(dir);

		boolean disableObscured = disableObscuredFaceCheck.orElse(Configurations.connectInsideCTM);

		BlockState con = getConnectionState(world, connection, dir, current);
		BlockState obscuring = disableObscured ? null : getConnectionState(world, obscuringPos, dir, current);

		// bad API user
		if (con == null) {
			throw new IllegalStateException("Error, received null blockstate as facade from block " + world.getBlockState(connection));
		}

		boolean ret = stateComparator(world, current, connection, state, con, dir);

		// no block obscuring this face
		if (obscuring == null) {
			ci.setReturnValue(ret);
			return;
		}

		// check that we aren't already connected outwards from this side
		ret &= !stateComparator(world, current, obscuringPos, state, obscuring, dir);

		ci.setReturnValue(ret);
	}

	private boolean stateComparator(IBlockReader world, BlockPos fromPos, BlockPos toPos, BlockState from, BlockState to, Direction dir) {
		if (!Kaleido.isKaleidoBlock(from) && !Kaleido.isKaleidoBlock(to)) {
			return stateComparator(from, to, dir);
		}
		return Hooks.stateComparator(fromPos, toPos, from, to, dir);
	}

	@Shadow
	abstract boolean stateComparator(BlockState from, BlockState to, Direction dir);

	@Shadow
	abstract BlockState getConnectionState(IBlockReader world, BlockPos pos, @Nullable Direction side, BlockPos connection);

}
