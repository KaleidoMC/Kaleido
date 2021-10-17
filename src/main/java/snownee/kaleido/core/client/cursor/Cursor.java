package snownee.kaleido.core.client.cursor;

public interface Cursor {

	long create();

	default void use() {
		Cursors.set(this);
	}

}
