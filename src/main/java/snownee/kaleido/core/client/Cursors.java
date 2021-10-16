package snownee.kaleido.core.client;

import org.lwjgl.glfw.GLFW;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.client.Minecraft;

public final class Cursors {
	private static final long WINDOW = Minecraft.getInstance().getWindow().getWindow();
	private static final Object2LongMap<Cursor> HANDLES = new Object2LongOpenHashMap<>();

	private Cursors() {
	}

	public static void set(Cursor cursor) {
		GLFW.glfwSetCursor(WINDOW, HANDLES.computeIfAbsent(cursor, Cursor::create));
	}
}
