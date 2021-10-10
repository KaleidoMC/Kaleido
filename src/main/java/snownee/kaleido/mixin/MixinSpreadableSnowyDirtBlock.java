package snownee.kaleido.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.SpreadableSnowyDirtBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import snownee.kaleido.Hooks;
import snownee.kaleido.chisel.ChiselModule;

@Mixin(SpreadableSnowyDirtBlock.class)
public class MixinSpreadableSnowyDirtBlock {

	@Inject(at = @At("HEAD"), method = "canBeGrass", cancellable = true)
	private static void kaleido_canBeGrass(BlockState p_220257_0_, IWorldReader p_220257_1_, BlockPos p_220257_2_, CallbackInfoReturnable<Boolean> ci) {
		if (Hooks.chiselEnabled) {
			BlockPos blockpos = p_220257_2_.above();
			BlockState blockstate = p_220257_1_.getBlockState(blockpos);
			if (ChiselModule.CHISELED_BLOCKS.contains(blockstate.getBlock())) {
				ci.setReturnValue(true);
			}
		}
	}
}
