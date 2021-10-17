package snownee.kaleido.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.AbstractBlock.AbstractBlockState;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import snownee.kaleido.Hooks;
import snownee.kaleido.chisel.ChiselModule;

@Mixin(AbstractBlockState.class)
public class MixinBlockState {

	@Inject(at = @At("HEAD"), method = "getMapColor", cancellable = true)
	private void kaleido_getMapColor(IBlockReader pLevel, BlockPos pPos, CallbackInfoReturnable<MaterialColor> ci) {
		AbstractBlockState state = (AbstractBlockState) (Object) this;
		if (Hooks.chiselEnabled && ChiselModule.CHISELED_BLOCKS.contains(state.getBlock())) {
			ci.setReturnValue(Hooks.getMapColor(state, pLevel, pPos));
		}
	}

}
