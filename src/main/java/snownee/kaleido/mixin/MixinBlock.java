package snownee.kaleido.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import snownee.kaleido.Hooks;
import snownee.kaleido.Kaleido;

@Mixin(Block.class)
public class MixinBlock {

	@Inject(at = @At("HEAD"), method = "shouldRenderFace", cancellable = true)
	private static void kaleido_shouldRenderFace(BlockState state, IBlockReader level, BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> ci) {
		if (Kaleido.isKaleidoBlock(state) && Hooks.skipRender(state, level, pos, direction)) {
			ci.setReturnValue(false);
		}
	}
}
