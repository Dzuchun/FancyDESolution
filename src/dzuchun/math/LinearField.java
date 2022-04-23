package dzuchun.math;

/**
 * A primitive class, structuring a mathematically defined Linear Field field.
 *
 * @author dzu
 *
 * @param <T>
 */
public abstract class LinearField<T> {

	/**
	 *
	 * @param t1     1st instance.
	 * @param t2     2nd instance.
	 * @param write1 If a method should write result to first argument.
	 * @param write2 If a method should write result to second argument.
	 * @return Result of the operation.
	 */
	public abstract T add(T t1, T t2, boolean write1, boolean write2);

	/**
	 * Must return sum of two instances. This is a version with a predefined
	 * write1=write2=0, so it's literally just {@code add(t1, t2, false, false)}.
	 *
	 * @param t1 1st instance.
	 * @param t2 2nd instance.
	 * @return Result of the operation.
	 */
	public T add(T t1, T t2) {
		return add(t1, t2, false, false);
	}

	/**
	 * Must return the negative of an instance.
	 *
	 * @param t     Instance.
	 * @param write If a method should write result to an argument.
	 * @return Result of the operation.
	 */
	public abstract T neg(T t, boolean write);

	/**
	 * Must return scaled version of the element.
	 *
	 * @param t      Instance to scale.
	 * @param scalar A ratio to scale by.
	 * @param write  If method should write result to {@code t}.
	 * @return A scaled version of instance.
	 */
	public abstract T scale(T t, double scalar, boolean write);

	/**
	 * Must return difference of two instances. It's recommended to be overridden
	 *
	 * @param t1     1st instance.
	 * @param t2     2nd instance.
	 * @param write1 If a method should write result to first argument.
	 * @param write2 If a method should write result to second argument.
	 * @return Result of the operation.
	 */
	public T sub(T t1, T t2, boolean write1, boolean write2) {
		return add(t1, neg(t2, false), write1, write2);
	}

	/**
	 * Must return difference of two instances. This is a version with a predefined
	 * write1=write2=0, so it's literally just {@code sub(t1, t2, false, false)}.
	 *
	 * @param t1 1st instance.
	 * @param t2 2nd instance.
	 * @return Result of the operation.
	 */
	public T sub(T t1, T t2) {
		return sub(t1, t2, false, false);
	}

	/**
	 * Must return product of two instances.
	 *
	 * @param t1    1st instance
	 * @param t2    2nd instance
	 * @param write Address to additionally write result to (0 means no additional
	 *              writing).
	 * @return
	 */

	/**
	 * @return A zero element of the field. Zero has properties: add(zero, a) and
	 *         add(a, zero) return a.
	 */
	public abstract T zero();
}
