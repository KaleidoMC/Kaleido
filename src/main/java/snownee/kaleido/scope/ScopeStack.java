package snownee.kaleido.scope;

import net.minecraft.util.math.vector.Vector3d;
import snownee.kaleido.core.supplier.BlockDefinition;

public class ScopeStack {

	private BlockDefinition modelSupplier;
	private Vector3d translation = Vector3d.ZERO;

	public ScopeStack(BlockDefinition modelSupplier) {
		this.modelSupplier = modelSupplier;
	}
}
