package snownee.kaleido.util;

import net.minecraft.util.math.vector.Vector3f;

public class SmoothChasingVector {

	public final SmoothChasingValue x;
	public final SmoothChasingValue y;
	public final SmoothChasingValue z;

	public SmoothChasingVector() {
		this(false);
	}

	public SmoothChasingVector(boolean angle) {
		x = angle ? new SmoothChasingAngle() : new SmoothChasingValue();
		y = angle ? new SmoothChasingAngle() : new SmoothChasingValue();
		z = angle ? new SmoothChasingAngle() : new SmoothChasingValue();
	}

	public Vector3f getTarget() {
		return new Vector3f(x.target, y.target, z.target);
	}

	public boolean isMoving() {
		return x.isMoving() || y.isMoving() || z.isMoving();
	}

	public SmoothChasingVector set(Vector3f value) {
		x.value = value.x();
		y.value = value.y();
		z.value = value.z();
		return this;
	}

	public SmoothChasingVector start(Vector3f value) {
		set(value);
		target(value);
		return this;
	}

	public SmoothChasingVector target(Vector3f target) {
		x.target(target.x());
		y.target(target.y());
		z.target(target.z());
		return this;
	}

	public void tick(float pTicks) {
		x.tick(pTicks);
		y.tick(pTicks);
		z.tick(pTicks);
	}

	public SmoothChasingVector withSpeed(float speed) {
		x.withSpeed(speed);
		y.withSpeed(speed);
		z.withSpeed(speed);
		return this;
	}

	public void copyTo(Vector3f value) {
		value.setX(x.value);
		value.setY(y.value);
		value.setZ(z.value);
	}

	public SmoothChasingValue get(int i) {
		switch (i) {
		case 0:
			return x;
		case 1:
			return y;
		case 2:
			return z;
		default:
			throw new IndexOutOfBoundsException();
		}
	}
}
