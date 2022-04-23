package dzuchun.math;

public abstract class Ring<T> extends LinearField<T> {

	/**
	 * Must return product of two instances.
	 *
	 * @param t1     1st instance.
	 * @param t2     2nd instance.
	 * @param write1 If a method should write result to first argument.
	 * @param write2 If a method should write result to second argument.
	 * @return Result of the operation.
	 */
	public abstract T mul(T t1, T t2, boolean write1, boolean write2);

	/**
	 * Must return product of two instances. This is a version with a predefined
	 * write1=write2=0, so it's literally just {@code mul(t1, t2, false, false)}.
	 *
	 * @param t1 1st instance.
	 * @param t2 2nd instance.
	 * @return Result of the operation.
	 */
	public T mul(T t1, T t2) {
		return mul(t1, t2, false, false);
	}

	/**
	 * @return A one element of the field. One has properties: mul(one, a) and
	 *         mul(a, one) return a.
	 */
	public abstract T one();
}
