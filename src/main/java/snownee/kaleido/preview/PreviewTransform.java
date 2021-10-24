package snownee.kaleido.preview;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import snownee.kaleido.util.SmoothChasingAngle;
import snownee.kaleido.util.SmoothChasingVector;

public class PreviewTransform {

	public boolean canRotate;
	private final SmoothChasingAngle rotation;
	private final SmoothChasingVector position;

	public PreviewTransform() {
		position = new SmoothChasingVector();
		rotation = new SmoothChasingAngle();
	}

	public float getX() {
		return position.x.value;
	}

	public float getY() {
		return position.y.value;
	}

	public float getZ() {
		return position.z.value;
	}

	public boolean isMoving() {
		return position.isMoving() || rotation.isMoving();
	}

	public PreviewTransform pos(BlockPos pos) {
		position.x.start(pos.getX());
		position.y.start(pos.getY());
		position.z.start(pos.getZ());
		return this;
	}

	public PreviewTransform target(BlockPos pos) {
		position.x.target(pos.getX());
		position.y.target(pos.getY());
		position.z.target(pos.getZ());
		return this;
	}

	public void tick(float pTicks) {
		position.tick(pTicks);
		rotation.tick(pTicks);
	}

	public Quaternion getRotation() {
		return Vector3f.YN.rotationDegrees(rotation.value);
	}

	public void rotate(float yRot) {
		if (canRotate)
			rotation.target(yRot);
		else
			rotation.set(yRot);
		canRotate = true;
	}
}
