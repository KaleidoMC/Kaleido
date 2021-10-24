package snownee.kaleido.core.util;

import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.ModelRotation;
import net.minecraft.util.math.vector.TransformationMatrix;

public enum UVLockedRotation implements IModelTransform {

	X0_Y0(ModelRotation.X0_Y0),
	X0_Y90(ModelRotation.X0_Y90),
	X0_Y180(ModelRotation.X0_Y180),
	X0_Y270(ModelRotation.X0_Y270),
	X90_Y0(ModelRotation.X90_Y0),
	X90_Y90(ModelRotation.X90_Y90),
	X90_Y180(ModelRotation.X90_Y180),
	X90_Y270(ModelRotation.X90_Y270),
	X180_Y0(ModelRotation.X180_Y0),
	X180_Y90(ModelRotation.X180_Y90),
	X180_Y180(ModelRotation.X180_Y180),
	X180_Y270(ModelRotation.X180_Y270),
	X270_Y0(ModelRotation.X270_Y0),
	X270_Y90(ModelRotation.X270_Y90),
	X270_Y180(ModelRotation.X270_Y180),
	X270_Y270(ModelRotation.X270_Y270);

	private static final UVLockedRotation[] VALUES = values();
	public final ModelRotation rotation;

	UVLockedRotation(ModelRotation rotation) {
		this.rotation = rotation;
	}

	public static IModelTransform of(ModelRotation transform, boolean uvlock) {
		return uvlock ? VALUES[transform.ordinal()] : transform;
	}

	@Override
	public TransformationMatrix getRotation() {
		return rotation.getRotation();
	}

	@Override
	public boolean isUvLocked() {
		return true;
	}

}
