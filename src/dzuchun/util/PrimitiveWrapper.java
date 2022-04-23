package dzuchun.util;

public class PrimitiveWrapper<T> {
	public T value;

	public PrimitiveWrapper(T valueIn) {
		this.value = valueIn;
	}

	@Override
	public String toString() {
		return value.toString();
	}

	public PrimitiveWrapper<T> copy() {
		return new PrimitiveWrapper<T>(value);
	}
}
