package snownee.kaleido.scope;

import java.util.Random;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import snownee.kaleido.core.client.KaleidoClient;
import snownee.kaleido.core.definition.BlockDefinition;
import snownee.kaleido.core.definition.DynamicBlockDefinition;
import snownee.kaleido.util.SimulationBlockReader;

public class ScopeStack {

	@OnlyIn(Dist.CLIENT)
	private static BlockModelRenderer modelRenderer;
	public final BlockDefinition blockDefinition;
	public final Vector3f translation = new Vector3f();
	public final Vector3f rotation = new Vector3f();
	public final Vector3f scale = new Vector3f();
	private Quaternion quaternion;

	public ScopeStack(BlockDefinition blockDefinition) {
		this.blockDefinition = blockDefinition;
	}

	@OnlyIn(Dist.CLIENT)
	public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, SimulationBlockReader world, BlockPos pos, int pCombinedOverlay, boolean checkSides) {
		for (RenderType renderType : KaleidoClient.blockRenderTypes) {
			if (!blockDefinition.canRenderInLayer(renderType)) {
				continue;
			}
			ForgeHooksClient.setRenderLayer(renderType);
			IVertexBuilder vertexBuilder = buffer.getBuffer(renderType);
			render(matrixStack, vertexBuilder, world, pos, pCombinedOverlay, new Random(), blockDefinition.getBlockState().getSeed(pos), checkSides);
		}
		ForgeHooksClient.setRenderLayer(null);
	}

	public void render(MatrixStack matrixIn, IVertexBuilder buffer, SimulationBlockReader world, BlockPos posIn, int combinedOverlayIn, Random randomIn, long rand, boolean checkSides) {
		if (modelRenderer == null) {
			modelRenderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
		}

		boolean bTranslate = notZero(translation);
		boolean bRotate = notZero(rotation);
		boolean bScale = notZero(scale);
		boolean transformed = bTranslate || bRotate || bScale;

		// fix z-fighting
		int i = hashCode();
		double ox = (((i & 15L) / 15.0D) - 0.5) * 0.002;
		double oy = (((i >> 4 & 15L) / 15.0D) - 0.5) * 0.002;
		double oz = (((i >> 8 & 15L) / 15.0D) - 0.5) * 0.002;

		matrixIn.pushPose();
		if (bRotate || bScale) {
			ox += 0.5;
			oy += 0.5;
			oz += 0.5;
		}
		if (bTranslate) {
			ox += translation.x() * 0.0625;
			oy += translation.y() * 0.0625;
			oz += translation.z() * 0.0625;
		}
		matrixIn.translate(ox, oy, oz);
		if (bRotate) {
			matrixIn.mulPose(quaternion);
		}
		if (bScale) {
			matrixIn.scale(scale.x() + 1, scale.y() + 1, scale.z() + 1);
		}
		if (bRotate || bScale) {
			matrixIn.translate(-0.5, -0.5, -0.5);
		}

		world.useSelfLight(transformed);

		world.setPos(posIn);
		if (blockDefinition.getClass() == DynamicBlockDefinition.class) {
			world.setBlockEntity(((DynamicBlockDefinition) blockDefinition).blockEntity);
		} else {
			world.setBlockEntity(null);
		}
		BlockState blockState = blockDefinition.getBlockState();
		modelRenderer.renderModel(world, blockDefinition.model(), blockState, posIn, matrixIn, buffer, checkSides && !transformed, randomIn, rand, combinedOverlayIn, blockDefinition.modelData());

		matrixIn.popPose();
	}

	public boolean isTransformed() {
		return notZero(translation) || notZero(rotation) || notZero(scale);
	}

	public void save(CompoundNBT tag) {
		CompoundNBT def = new CompoundNBT();
		blockDefinition.save(def);
		def.putString("Type", blockDefinition.getFactory().getId());
		tag.put("Def", def);
		if (notZero(translation))
			tag.put("Translation", saveVec(translation));
		if (notZero(rotation))
			tag.put("Rotation", saveVec(rotation));
		if (notZero(scale))
			tag.put("Scale", saveVec(scale));
	}

	@Nullable
	public static ScopeStack load(CompoundNBT tag) {
		BlockDefinition blockDefinition = BlockDefinition.fromNBT(tag.getCompound("Def"));
		if (blockDefinition == null) {
			return null;
		}
		ScopeStack stack = new ScopeStack(blockDefinition);
		loadVec(stack.translation, tag.getCompound("Translation"));
		loadVec(stack.rotation, tag.getCompound("Rotation"));
		loadVec(stack.scale, tag.getCompound("Scale"));
		stack.updateRotation();
		return stack;
	}

	public void translate(Axis axis, float f) {
		addVec(translation, axis, f);
	}

	public void rotate(Axis axis, float f) {
		addVec(rotation, axis, f);
		updateRotation();
	}

	public void scale(Axis axis, float f) {
		addVec(scale, axis, f);
	}

	private void updateRotation() {
		quaternion = new Quaternion(rotation.x(), rotation.y(), rotation.z(), true);
	}

	private void addVec(Vector3f vec, Axis axis, float f) {
		switch (axis) {
		case X:
			vec.setX(vec.x() + f);
			break;
		case Y:
			vec.setY(vec.y() + f);
			break;
		case Z:
			vec.setZ(vec.z() + f);
			break;
		}
	}

	private static CompoundNBT saveVec(Vector3f vec) {
		CompoundNBT tag = new CompoundNBT();
		tag.putFloat("x", vec.x());
		tag.putFloat("y", vec.y());
		tag.putFloat("z", vec.z());
		return tag;
	}

	private static void loadVec(Vector3f vec, CompoundNBT tag) {
		vec.setX(tag.getFloat("x"));
		vec.setY(tag.getFloat("y"));
		vec.setZ(tag.getFloat("z"));
	}

	private static boolean notZero(Vector3f vec) {
		return vec.x() != 0 || vec.y() != 0 || vec.z() != 0;
	}
}
