package snownee.kaleido.util;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import com.google.common.util.concurrent.Runnables;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class GhostRenderType extends RenderType {
	private static Map<RenderType, GhostRenderType> remappedTypes = new IdentityHashMap<>();

	public static RenderType remap(RenderType type, float alpha) {
		GhostRenderType remappedType = type instanceof GhostRenderType ? (GhostRenderType) type : remappedTypes.computeIfAbsent(type, GhostRenderType::new);
		remappedType.alpha = MathHelper.clamp(alpha, 0, 1);
		return remappedType;
	}

	private float alpha;
	private final RenderType original;

	private GhostRenderType(RenderType original) {
		super(original.toString() + "_kaleido_ghost", original.format(), original.mode(), original.bufferSize(), original.affectsCrumbling(), true, Runnables.doNothing(), Runnables.doNothing());
		this.original = original;
	}

	@Override
	public void setupRenderState() {
		original.setupRenderState();
		RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.CONSTANT_ALPHA, GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA);
		RenderSystem.blendColor(1F, 1F, 1F, alpha);
	}

	@Override
	public void clearRenderState() {
		RenderSystem.blendColor(1F, 1F, 1F, 1F);
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableBlend();
		RenderSystem.enableDepthTest();
		original.clearRenderState();
	}

	private static Buffer defaultBuffer;

	public static class Buffer extends IRenderTypeBuffer.Impl {

		private float alpha = 1;

		protected Buffer(BufferBuilder builder, Map<RenderType, BufferBuilder> fixedBuffers) {
			super(builder, fixedBuffers);
		}

		public void setAlpha(float alpha) {
			this.alpha = alpha;
		}

		@Override
		public IVertexBuilder getBuffer(RenderType type) {
			return super.getBuffer(GhostRenderType.remap(type, alpha));
		}

	}

	public static Buffer createBuffer(IRenderTypeBuffer.Impl original) {
		BufferBuilder fallback = ObfuscationReflectionHelper.getPrivateValue(IRenderTypeBuffer.Impl.class, original, "field_228457_a_"); //builder
		Map<RenderType, BufferBuilder> fixedBuffers = ObfuscationReflectionHelper.getPrivateValue(IRenderTypeBuffer.Impl.class, original, "field_228458_b_"); //fixedBuffers
		Map<RenderType, BufferBuilder> remapped = new HashMap<>();
		for (Map.Entry<RenderType, BufferBuilder> e : fixedBuffers.entrySet()) {
			remapped.put(GhostRenderType.remap(e.getKey(), 1), e.getValue());
		}
		return new Buffer(fallback, remapped);
	}

	public static Buffer defaultBuffer() {
		if (defaultBuffer == null) {
			defaultBuffer = createBuffer(Minecraft.getInstance().renderBuffers().bufferSource());
		}
		return defaultBuffer;
	}
}
