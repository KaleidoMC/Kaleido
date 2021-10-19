package snownee.kaleido.core.block.entity;

import java.util.function.IntSupplier;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;

public class BELightManager {
	public IBlockDisplayReader level;
	public BlockPos pos;
	public IntSupplier lightSupplier;
	public int light;

	public BELightManager(IBlockDisplayReader level, BlockPos pos, IntSupplier lightSupplier) {
		this.level = level;
		this.pos = pos;
		this.lightSupplier = lightSupplier;
		this.light = -1;
	}

	public void add(int l) {
		if (l > light)
			set(l);
	}

	public void remove(int l) {
		if (l >= light)
			update();
	}

	public void update() {
		if (lightSupplier != null)
			set(lightSupplier.getAsInt());
	}

	public void set(int l) {
		if (l == light)
			return;
		light = l;
		check();
	}

	public void check() {
		if (level != null && pos != null && light >= 0)
			level.getLightEngine().checkBlock(pos);
	}
}
