package snownee.kaleido.preview;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import snownee.kaleido.KaleidoClientConfig;
import snownee.kaleido.core.CoreModule;
import snownee.kaleido.core.ModelInfo;
import snownee.kaleido.core.block.MasterBlock;
import snownee.kaleido.core.tile.MasterTile;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(Dist.CLIENT)
public final class PlacementPreview {

    private static class GhostRenderType extends RenderType {
        private static Map<RenderType, RenderType> remappedTypes = new IdentityHashMap<>();

        public static RenderType remap(RenderType type) {
            return type instanceof GhostRenderType ? type : remappedTypes.computeIfAbsent(type, GhostRenderType::new);
        }

        GhostRenderType(RenderType original) {
            super(original.toString() + "_place_preview", original.getVertexFormat(), original.getDrawMode(), original.getBufferSize(), original.isUseDelegate(), true, () -> {
                original.setupRenderState();
                RenderSystem.disableDepthTest();
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.CONSTANT_ALPHA, GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA);
                RenderSystem.blendColor(1F, 1F, 1F, KaleidoClientConfig.previewAlpha);
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

    @SubscribeEvent
    public static void render(RenderWorldLastEvent event) {
        if (!KaleidoClientConfig.previewEnabled) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameSettings.hideGUI || mc.gameSettings.keyBindAttack.isKeyDown() || !(mc.objectMouseOver instanceof BlockRayTraceResult) || mc.objectMouseOver.getType() == RayTraceResult.Type.MISS) {
            return;
        }

        ClientPlayerEntity player = mc.player;
        ItemStack held = player.getHeldItemMainhand();
        if (!(held.getItem() instanceof BlockItem)) {
            held = player.getHeldItemOffhand();
        }
        if (!KaleidoClientConfig.previewAllBlocks && held.getItem() != CoreModule.STUFF_ITEM) {
            return;
        }
        if (held.getItem() instanceof BlockItem) {
            BlockItem theBlockItem = (BlockItem) held.getItem();
            ModelInfo info = MasterBlock.getInfo(held);
            if (theBlockItem == CoreModule.STUFF_ITEM) {
                info = MasterBlock.getInfo(held);
                TileEntity tile = mc.world.getTileEntity(((BlockRayTraceResult) mc.objectMouseOver).getPos());
                if (tile instanceof MasterTile) {
                    if (((MasterTile) tile).getModelInfo() == info) {
                        return;
                    }
                }
            }
            BlockItemUseContext context = theBlockItem.getBlockItemUseContext(new BlockItemUseContext(new ItemUseContext(player, Hand.MAIN_HAND, (BlockRayTraceResult) mc.objectMouseOver)));
            if (context == null) {
                return;
            }
            BlockState placeResult;
            try {
                placeResult = (BlockState) GET_STATE_FOR_PLACEMENT.invokeExact(theBlockItem, context);
            } catch (Throwable e) {
                placeResult = null;
            }
            if (placeResult == null) {
                return;
            }
            BlockRenderType renderType = placeResult.getRenderType();
            if (renderType == BlockRenderType.INVISIBLE) {
                return;
            }
            BlockPos target = context.getPos();
            if (lastStack != held) {
                lastStack = held;
                transform.pos(target);
            }
            if (renderBuffer == null) {
                renderBuffer = initRenderBuffer(mc.getRenderTypeBuffers().getBufferSource());
            }
            MatrixStack transforms = event.getMatrixStack();
            Vec3d projVec = mc.getRenderManager().info.getProjectedView();
            transforms.translate(-projVec.x, -projVec.y, -projVec.z);
            transforms.push();
            transform.target(target).tick(mc.getTickLength());
            transforms.translate(transform.getX(), transform.getY(), transform.getZ());
            World world = context.getWorld();
            if (renderType == BlockRenderType.MODEL) {
                IModelData data = ModelDataManager.getModelData(world, target);
                if (data == null) {
                    data = EmptyModelData.INSTANCE;
                }
                BlockRendererDispatcher dispatcher = mc.getBlockRendererDispatcher();
                IBakedModel bakedModel;
                if (theBlockItem == CoreModule.STUFF_ITEM) {
                    if (info == null) {
                        bakedModel = mc.getModelManager().getMissingModel();
                    } else {
                        Direction direction = placeResult.get(HorizontalBlock.HORIZONTAL_FACING);
                        bakedModel = info.getBakedModel(direction);
                    }
                } else {
                    bakedModel = dispatcher.getBlockModelShapes().getModel(placeResult);
                }
                long i = placeResult.getPositionRandom(target);
                dispatcher.getBlockModelRenderer().renderModel(world, bakedModel, placeResult, target, transforms, renderBuffer.getBuffer(RenderTypeLookup.getRenderType(placeResult)), false, dispatcher.random, i, OverlayTexture.NO_OVERLAY, data);
            }
            /* Assume renderType is not null.
                             *
                             * Yes, we use a fake tile entity to workaround this. All exceptions are
                             * discared. It is ugly, yes, but it partially solve the problem.
                             */
            if (placeResult.hasTileEntity()) {
                TileEntity tile = placeResult.createTileEntity(world);
                tile.setWorldAndPos(world, target);
                TileEntityRenderer<? super TileEntity> renderer = TileEntityRendererDispatcher.instance.getRenderer(tile);
                if (renderer != null) {
                    try {
                        // 0x00F0_00F0 means "full sky light and full block light".
                        // Reference: LightTexture.packLight (func_228451_a_)
                        renderer.render(tile, 0F, transforms, renderBuffer, 0x00F0_00F0, OverlayTexture.NO_OVERLAY);
                    } catch (Exception ignored) {}
                }
            }
            transforms.pop();
            renderBuffer.finish();
        }
    }
}
