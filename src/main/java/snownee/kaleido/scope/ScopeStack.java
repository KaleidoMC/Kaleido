package snownee.kaleido.scope;

import net.minecraft.util.math.vector.Vector3d;
import snownee.kaleido.core.supplier.ModelSupplier;

public class ScopeStack {

	private ModelSupplier modelSupplier;
	private Vector3d translation = Vector3d.ZERO;

	public ScopeStack(ModelSupplier modelSupplier) {
		this.modelSupplier = modelSupplier;
	}
}
