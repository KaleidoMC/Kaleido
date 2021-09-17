package snownee.kaleido.preview;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.AbstractBannerBlock;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.properties.BedPart;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.BannerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.animation.Animation;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import snownee.kaleido.KaleidoClientConfig;
import snownee.kaleido.core.CoreModule;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.block.KaleidoBlocks;
import snownee.kaleido.core.block.entity.MasterBlockEntity;
import snownee.kaleido.core.client.KaleidoClient;
import snownee.kaleido.core.util.KaleidoTemplate;
import team.chisel.ctm.Configurations;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(Dist.CLIENT)
public final class PlacementPreview {

	private static class GhostRenderType extends RenderType {
		private static Map<RenderType, RenderType> remappedTypes = new IdentityHashMap<>();

		public static RenderType remap(RenderType type) {
			return type instanceof GhostRenderType ? type : remappedTypes.computeIfAbsent(type, GhostRenderType::new);
		}

		GhostRenderType(RenderType original) {
			super(original.toString() + "_place_preview", original.format(), original.mode(), original.bufferSize(), original.affectsCrumbling(), true, () -> {
				original.setupRenderState();
				RenderSystem.disableDepthTest();
				RenderSystem.enableBlend();
				RenderSystem.blendFunc(GlStateManager.SourceFactor.CONSTANT_ALPHA, GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA);
				float alpha = KaleidoClientConfig.previewAlpha + MathHelper.sin(Animation.getWorldTime(Minecraft.getInstance().level) * 4) * 0.05F;
				alpha = MathHelper.clamp(alpha, 0, 1);
				RenderSystem.blendColor(1F, 1F, 1F, alpha);
			}, () -> {
				RenderSystem.blendColor(1F, 1F, 1F, 1F);
				RenderSystem.defaultBlendFunc();
				RenderSystem.disableBlend();
				RenderSystem.enableDepthTest();
				original.clearRenderState();
			});
		}
	}

	private static final MethodHandle GET_STATE_FOR_PLACEMENT;
	private static ItemStack lastStack = ItemStack.EMPTY;
	private static IRenderTypeBuffer.Impl renderBuffer;

	private static PreviewTransform transform = new PreviewTransform();

	static {
		MethodHandle m = null;
		try {
			m = MethodHandles.lookup().unreflect(ObfuscationReflectionHelper.findMethod(BlockItem.class, "func_195945_b", BlockItemUseContext.class));
		} catch (Exception e) {
			throw new RuntimeException("Report this to author", e);
		}
		GET_STATE_FOR_PLACEMENT = m;
	}

	private static IRenderTypeBuffer.Impl initRenderBuffer(IRenderTypeBuffer.Impl original) {
		BufferBuilder fallback = ObfuscationReflectionHelper.getPrivateValue(IRenderTypeBuffer.Impl.class, original, "field_228457_a_");
		Map<RenderType, BufferBuilder> layerBuffers = ObfuscationReflectionHelper.getPrivateValue(IRenderTypeBuffer.Impl.class, original, "field_228458_b_");
		Map<RenderType, BufferBuilder> remapped = new HashMap<>();
		for (Map.Entry<RenderType, BufferBuilder> e : layerBuffers.entrySet()) {
			remapped.put(GhostRenderType.remap(e.getKey()), e.getValue());
		}
		return new IRenderTypeBuffer.Impl(fallback, remapped) {
			@Override
			public IVertexBuilder getBuffer(RenderType type) {
				return super.getBuffer(GhostRenderType.remap(type));
			}
		};
	}

	private static boolean successLast;

	@SubscribeEvent
	public static void render(RenderWorldLastEvent event) {
		successLast = renderInternal(event);
	}

	private static boolean renderInternal(RenderWorldLastEvent event) {
		if (!KaleidoClientConfig.previewEnabled) {
			return false;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.overlay != null || mc.player == null || mc.options.hideGui || mc.options.keyAttack.isDown() || !(mc.hitResult instanceof BlockRayTraceResult) || mc.hitResult.getType() == RayTraceResult.Type.MISS/* || mc.level.isDebug()*/) {
			return false;
		}

		ClientPlayerEntity player = mc.player;
		ItemStack held = player.getMainHandItem();
		if (!(held.getItem() instanceof BlockItem)) {
			held = player.getOffhandItem();
		}
		if (!KaleidoClientConfig.previewAllBlocks && held.getItem() != CoreModule.STUFF_ITEM) {
			return false;
		}
		if (held.getItem() instanceof BlockItem) {
			BlockItem theBlockItem = (BlockItem) held.getItem();
			ModelInfo info = KaleidoBlocks.getInfo(held);
			if (theBlockItem == CoreModule.STUFF_ITEM) {
				if (info == null || info.template == KaleidoTemplate.item) {
					return false;
				}
				if (info.template == KaleidoTemplate.none) {
					TileEntity tile = mc.level.getBlockEntity(((BlockRayTraceResult) mc.hitResult).getBlockPos());
					if (tile instanceof MasterBlockEntity) {
						MasterBlockEntity masterTile = (MasterBlockEntity) tile;
						if (masterTile.getModelInfo() != null && masterTile.getModelInfo().id.equals(info.id)) {
							return false;
						}
					}
				}
			}
			BlockItemUseContext context = theBlockItem.updatePlacementContext(new BlockItemUseContext(new ItemUseContext(player, Hand.MAIN_HAND, (BlockRayTraceResult) mc.hitResult)));
			if (context == null) {
				return false;
			}
			BlockState placeResult = null;
			try {
				placeResult = (BlockState) GET_STATE_FOR_PLACEMENT.invokeExact(theBlockItem, context);
			} catch (Throwable e) {
			}
			if (placeResult == null) {
				return false;
			}
			//			if (renderType == BlockRenderType.INVISIBLE) {
			//				return false;
			//			}

			BlockPos target = context.getClickedPos();
			World world = context.getLevel();
			if (!world.getBlockState(target).isAir()) {
				return false;
			}

			Direction direction = null;
			if (placeResult.hasProperty(HorizontalBlock.FACING)) {
				direction = placeResult.getValue(HorizontalBlock.FACING);
				placeResult = placeResult.setValue(HorizontalBlock.FACING, Direction.NORTH);
			} else if (placeResult.hasProperty(DirectionalBlock.FACING) && placeResult.getValue(DirectionalBlock.FACING).get2DDataValue() != -1) {
				direction = placeResult.getValue(DirectionalBlock.FACING);
				placeResult = placeResult.setValue(DirectionalBlock.FACING, Direction.NORTH);
			} else {
				transform.canRotate = false;
			}
			if (info != null && info.template == KaleidoTemplate.block) {
				direction = null;
			}
			if (direction != null) {
				transform.rotate(direction.toYRot() + 180);
			}

			if (lastStack != held) {
				lastStack = held;
				transform.pos(target);
			}
			if (renderBuffer == null) {
				renderBuffer = initRenderBuffer(mc.renderBuffers().bufferSource());
			}
			MatrixStack transforms = event.getMatrixStack();
			Vector3d projVec = mc.getEntityRenderDispatcher().camera.getPosition();
			transforms.translate(-projVec.x, -projVec.y, -projVec.z);
			transforms.pushPose();
			if (successLast) {
				transform.target(target);
			} else {
				transform.pos(target);
			}
			transform.tick(mc.getDeltaFrameTime());
			transforms.translate(transform.getX() + .5, transform.getY() + .5, transform.getZ() + .5);
			if (transform.canRotate)
				transforms.mulPose(transform.getRotation());
			transforms.translate(-.5, -.5, -.5);
			renderBlock(transforms, world, placeResult, target, held, info);
			if (placeResult.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) && placeResult.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) {
				transforms.translate(0, 1, 0);
				renderBlock(transforms, world, placeResult.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER), target.above(), held, info);
			} else if (placeResult.hasProperty(BlockStateProperties.BED_PART) && placeResult.hasProperty(HorizontalBlock.FACING)) {
				Direction facing = placeResult.getValue(HorizontalBlock.FACING);
				transforms.translate(facing.getStepX(), 0, facing.getStepZ());
				renderBlock(transforms, world, placeResult.setValue(BlockStateProperties.BED_PART, BedPart.HEAD), target.relative(facing), held, info);
			}
			transforms.popPose();
			renderBuffer.endBatch();
		}
		return true;
	}

	private static void renderBlock(MatrixStack transforms, World world, BlockState state, BlockPos pos, ItemStack stack, ModelInfo info) {
		BlockRenderType renderType = state.getRenderShape();
		if (renderType == BlockRenderType.MODEL) {
			IModelData data = EmptyModelData.INSTANCE;//ModelDataManager.getModelData(world, target);
			//				if (data == null) {
			//					data = EmptyModelData.INSTANCE;
			//				}
			BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
			IBakedModel bakedModel;
			if (stack.getItem() == CoreModule.STUFF_ITEM) {
				bakedModel = KaleidoClient.getModel(info, state);
			} else {
				bakedModel = dispatcher.getBlockModelShaper().getBlockModel(state);
			}
			long i = state.getSeed(pos);
			boolean preDisableCTM = false;
			if (KaleidoClient.ctm) {
				preDisableCTM = Configurations.disableCTM;
				Configurations.disableCTM = true;
			}
			transforms.pushPose();
			dispatcher.getModelRenderer().renderModel(world, bakedModel, state, pos, transforms, renderBuffer.getBuffer(RenderTypeLookup.getRenderType(state, false)), false, new Random(), i, OverlayTexture.NO_OVERLAY, data);
			transforms.popPose();
			if (KaleidoClient.ctm) {
				Configurations.disableCTM = preDisableCTM;
			}
		}
		/* Assume renderType is not null.
         *
         * Yes, we use a fake tile entity to workaround this. All exceptions are
         * discared. It is ugly, yes, but it partially solve the problem.
         */
		if (state.hasTileEntity()) {
			TileEntity tile = state.createTileEntity(world);
			@SuppressWarnings("rawtypes")
			TileEntityRenderer renderer = TileEntityRendererDispatcher.instance.getRenderer(tile);
			tile.setLevelAndPosition(world, pos);
			tile.blockState = state;
			if (tile instanceof BannerTileEntity && state.getBlock() instanceof AbstractBannerBlock) {
				((BannerTileEntity) tile).fromItem(stack, ((AbstractBannerBlock) state.getBlock()).getColor());
			}
			if (renderer != null) {
				try {
					// 0x00F0_00F0 means "full sky light and full block light".
					// Reference: LightTexture.packLight (func_228451_a_)
					renderer.render(tile, 0F, transforms, renderBuffer, 0x00F0_00F0, OverlayTexture.NO_OVERLAY);
				} catch (Exception ignored) {
				}
			}
		}

	}
}
