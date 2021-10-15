package snownee.kaleido.core.client.gui;

import net.minecraft.client.gui.widget.Widget;
import snownee.kaleido.util.SmoothChasingValue;

public class AnimatedWidget {
	public final SmoothChasingValue x = new SmoothChasingValue();
	public final SmoothChasingValue y = new SmoothChasingValue();

	public void tick(Widget widget, float pTicks) {
		x.tick(pTicks);
		y.tick(pTicks);
		widget.x = (int) x.value;
		widget.y = (int) y.value;
	}
}
