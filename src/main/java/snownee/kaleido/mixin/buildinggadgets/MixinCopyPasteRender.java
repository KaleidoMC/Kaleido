package snownee.kaleido.mixin.buildinggadgets;

import java.util.List;
import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.direwolf20.buildinggadgets.client.renderer.MyRenderMethods;
import com.direwolf20.buildinggadgets.client.renderer.OurRenderTypes;
import com.direwolf20.buildinggadgets.client.renders.CopyPasteRender;
import com.direwolf20.buildinggadgets.client.renders.CopyPasteRender.MultiVBORenderer;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.tainted.building.PlacementTarget;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import snownee.kaleido.compat.buildinggadgets.KaleidoTileData;

@Mixin(value = CopyPasteRender.class, remap = false)
public class MixinCopyPasteRender {
	@Shadow
	private MultiVBORenderer renderBuffer;
	@Shadow
	private int tickTrack = 0;

	@Inject(at = @At("HEAD"), method = "renderTargets", cancellable = true)
	private void kaleido_renderTargets(MatrixStack matrix, Vector3d projectedView, BuildContext context, List<PlacementTarget> targets, BlockPos startPos, CallbackInfo ci) {
		ci.cancel();
		tickTrack++;
		if (renderBuffer != null && tickTrack < 300) {
			if (tickTrack % 30 == 0) {
				try {
					Vector3d projectedView2 = projectedView;
					Vector3d startPosView = new Vector3d(startPos.getX(), startPos.getY(), startPos.getZ());
					projectedView2 = projectedView2.subtract(startPosView);
					renderBuffer.sort((float) projectedView2.x(), (float) projectedView2.y(), (float) projectedView2.z());
				} catch (Exception ignored) {
				}
			}

			matrix.translate(startPos.getX(), startPos.getY(), startPos.getZ());
			renderBuffer.render(matrix.last().pose()); //Actually draw whats in the buffer
			return;
		}

		//        List<BlockPos> blockPosList = sorter.getSortedTargets().stream().map(PlacementTarget::getPos).collect(Collectors.toList());

		tickTrack = 0;
		if (renderBuffer != null) //Reset Render Buffer before rebuilding
			renderBuffer.close();

		renderBuffer = MultiVBORenderer.of((buffer) -> {
			IVertexBuilder builder = buffer.getBuffer(OurRenderTypes.RenderBlock);
			IVertexBuilder noDepthbuilder = buffer.getBuffer(OurRenderTypes.CopyPasteRenderBlock);

			BlockRendererDispatcher dispatcher = getMc().getBlockRenderer();

			MatrixStack stack = new MatrixStack(); //Create a new matrix stack for use in the buffer building process
			stack.pushPose(); //Save position

			for (PlacementTarget target : targets) {
				BlockPos targetPos = target.getPos();
				BlockState state = context.getWorld().getBlockState(target.getPos());

				/// Kaleido patch START
				IModelData modelData = EmptyModelData.INSTANCE;
				if (target.getData().getTileData() instanceof KaleidoTileData)
					modelData = ((KaleidoTileData) target.getData().getTileData()).getModelData();
				/// Kaleido patch END

				stack.pushPose(); //Save position again
				//matrix.translate(-startPos.getX(), -startPos.getY(), -startPos.getZ());
				stack.translate(targetPos.getX(), targetPos.getY(), targetPos.getZ());

				IBakedModel ibakedmodel = dispatcher.getBlockModel(state);
				BlockColors blockColors = Minecraft.getInstance().getBlockColors();
				int color = blockColors.getColor(state, context.getWorld(), targetPos, 0);

				float f = (float) (color >> 16 & 255) / 255.0F;
				float f1 = (float) (color >> 8 & 255) / 255.0F;
				float f2 = (float) (color & 255) / 255.0F;
				try {
					if (state.getRenderShape() == BlockRenderType.MODEL) {
						for (Direction direction : Direction.values()) {
							if (Block.shouldRenderFace(state, context.getWorld(), targetPos, direction) && !(context.getWorld().getBlockState(targetPos.relative(direction)).getBlock().equals(state.getBlock()))) {
								if (state.getMaterial().isSolidBlocking()) {
									MyRenderMethods.renderModelBrightnessColorQuads(stack.last(), builder, f, f1, f2, 0.7f, ibakedmodel.getQuads(state, direction, new Random(MathHelper.getSeed(targetPos)), modelData), 15728640, 655360);
								} else {
									MyRenderMethods.renderModelBrightnessColorQuads(stack.last(), noDepthbuilder, f, f1, f2, 0.7f, ibakedmodel.getQuads(state, direction, new Random(MathHelper.getSeed(targetPos)), modelData), 15728640, 655360);
								}
							}
						}
						if (state.getMaterial().isSolidBlocking())
							MyRenderMethods.renderModelBrightnessColorQuads(stack.last(), builder, f, f1, f2, 0.7f, ibakedmodel.getQuads(state, null, new Random(MathHelper.getSeed(targetPos)), modelData), 15728640, 655360);
						else
							MyRenderMethods.renderModelBrightnessColorQuads(stack.last(), noDepthbuilder, f, f1, f2, 0.7f, ibakedmodel.getQuads(state, null, new Random(MathHelper.getSeed(targetPos)), modelData), 15728640, 655360);
					}
				} catch (Exception e) {
					BuildingGadgets.LOG.trace("Caught exception whilst rendering {}.", state, e);
				}

				stack.popPose(); // Load the position we saved earlier
			}
			stack.popPose(); //Load after loop
		});
		//        try {
		Vector3d projectedView2 = getMc().gameRenderer.getMainCamera().getPosition();
		Vector3d startPosView = new Vector3d(startPos.getX(), startPos.getY(), startPos.getZ());
		projectedView2 = projectedView2.subtract(startPosView);
		renderBuffer.sort((float) projectedView2.x(), (float) projectedView2.y(), (float) projectedView2.z());
		//        } catch (Exception ignored) {
		//        }
		matrix.translate(startPos.getX(), startPos.getY(), startPos.getZ());
		renderBuffer.render(matrix.last().pose()); //Actually draw whats in the buffer
	}

	private static Minecraft getMc() {
		return Minecraft.getInstance();
	}
}
