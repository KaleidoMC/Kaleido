package snownee.kaleido.mixin.buildinggadgets;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.direwolf20.buildinggadgets.client.renderer.EffectBlockTER;
import com.direwolf20.buildinggadgets.common.tainted.building.BlockData;
import com.direwolf20.buildinggadgets.common.tileentities.EffectBlockTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraftforge.client.model.data.IModelData;
import snownee.kaleido.compat.buildinggadgets.KaleidoTileData;

@Mixin(value = EffectBlockTER.class, remap = false)
public class MixinEffectBlockTER {

	@Redirect(
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/client/renderer/BlockRendererDispatcher;renderBlock(Lnet/minecraft/block/BlockState;Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;IILnet/minecraftforge/client/model/data/IModelData;)V"
			), method = "render"
	)
	private void kaleido_renderBlock(BlockRendererDispatcher dispatcher, BlockState p_228791_1_, MatrixStack p_228791_2_, IRenderTypeBuffer p_228791_3_, int p_228791_4_, int p_228791_5_, IModelData modelData, EffectBlockTileEntity tile, float partialTicks, MatrixStack stack, IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn) {
		BlockData renderData = tile.getRenderedBlock();
		if (renderData.getTileData() instanceof KaleidoTileData)
			modelData = ((KaleidoTileData) renderData.getTileData()).getModelData();
		dispatcher.renderBlock(p_228791_1_, p_228791_2_, p_228791_3_, p_228791_4_, p_228791_5_, modelData);
	}
}
