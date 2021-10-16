package snownee.kaleido.core.client;

public interface Cursor {

	long create();

	default void use() {
		Cursors.set(this);
	}

}
