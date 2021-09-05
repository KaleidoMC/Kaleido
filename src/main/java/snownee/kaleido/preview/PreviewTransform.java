package snownee.kaleido.preview;

import net.minecraft.util.math.BlockPos;
import snownee.kaleido.util.SmoothChasingAngle;
import snownee.kaleido.util.SmoothChasingValue;

public class PreviewTransform {

	private final SmoothChasingAngle rotation;
	private final SmoothChasingValue x, y, z;

	public PreviewTransform() {
		x = new SmoothChasingValue();
		y = new SmoothChasingValue();
		z = new SmoothChasingValue();
		rotation = new SmoothChasingAngle();
	}

	public float getX() {
		return x.value;
	}

	public float getY() {
		return y.value;
	}

	public float getZ() {
		return z.value;
	}

	public boolean isMoving() {
		return x.isMoving() || y.isMoving() || z.isMoving() || rotation.isMoving();
	}

	public PreviewTransform pos(BlockPos pos) {
		x.set(pos.getX());
		y.set(pos.getY());
		z.set(pos.getZ());
		return this;
	}

	public PreviewTransform target(BlockPos pos) {
		x.target(pos.getX());
		y.target(pos.getY());
		z.target(pos.getZ());
		return this;
	}

	public void tick(float pTicks) {
		x.tick(pTicks);
		y.tick(pTicks);
		z.tick(pTicks);
		rotation.tick(pTicks);
	}
}
