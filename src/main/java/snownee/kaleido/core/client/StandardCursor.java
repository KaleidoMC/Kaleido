package snownee.kaleido.core.client;

import org.lwjgl.glfw.GLFW;

public class StandardCursor implements Cursor {

	public static final StandardCursor ARROW = new StandardCursor(GLFW.GLFW_ARROW_CURSOR);
	public static final StandardCursor IBEAM = new StandardCursor(GLFW.GLFW_IBEAM_CURSOR);
	public static final StandardCursor CROSSHAIR = new StandardCursor(GLFW.GLFW_CROSSHAIR_CURSOR);
	public static final StandardCursor HAND = new StandardCursor(GLFW.GLFW_HAND_CURSOR);
	public static final StandardCursor H_RESIZE = new StandardCursor(GLFW.GLFW_HRESIZE_CURSOR);
	public static final StandardCursor V_RESIZE = new StandardCursor(GLFW.GLFW_VRESIZE_CURSOR);

	private final int shape;

	public StandardCursor(int shape) {
		this.shape = shape;
	}

	@Override
	public long create() {
		return GLFW.glfwCreateStandardCursor(shape);
	}

}
