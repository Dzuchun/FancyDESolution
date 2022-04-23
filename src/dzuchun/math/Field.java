package dzuchun.math;

/**
 * A primitive class, structuring a mathematically defined field. For primitive
 * cases, please extend {@code PrimitiveWrapper} class
 *
 * @author dzu
 *
 * @param <T> Type of the instances.
 */
public abstract class Field<T> extends Ring<T> {

	/**
	 * Must return the inverse of an instance.
	 *
	 * @param t     Instance.
	 * @param write If a method should write result to an argument.
	 * @return Result of the operation.
	 */
	public abstract T inv(T t, boolean write);

	/**
	 * Must return division of two instances. It's recommended to be overridden
	 *
	 * @param t1     1st instance.
	 * @param t2     2nd instance.
	 * @param write1 If a method should write result to first argument.
	 * @param write2 If a method should write result to second argument.
	 * @return Result of the operation.
	 */
	public T div(T t1, T t2, boolean write1, boolean write2) {
		return mul(t1, inv(t2, false), write1, write2);
	}

	/**
	 * Must return division of two instances. This is a version with a predefined
	 * write1=write2=0, so it's literally just {@code div(t1, t2, false, false)}.
	 *
	 * @param t1 1st instance.
	 * @param t2 2nd instance.
	 * @return Result of the operation.
	 */
	public T div(T t1, T t2) {
		return div(t1, t2, false, false);
	}
}
