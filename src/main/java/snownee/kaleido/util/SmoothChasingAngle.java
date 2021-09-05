package snownee.kaleido.util;

public class SmoothChasingAngle extends SmoothChasingValue {

	@Override
	protected float getCurrentDiff() {
		return AngleHelper.getShortestAngleDiff(value, getTarget());
	}

}
