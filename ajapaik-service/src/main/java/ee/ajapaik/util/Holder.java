package ee.ajapaik.util;

public class Holder<T> {

	private T value;

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value != null ? value.toString() : null;
	}
}
