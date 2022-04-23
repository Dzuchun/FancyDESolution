package dzuchun.math.tensor;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Function;

import dzuchun.math.LinearField;
import dzuchun.math.Ring;
import dzuchun.util.ArrayUtil;
import dzuchun.util.Util;

@SuppressWarnings("unchecked")
public class TensorField<E, T extends Tensor<E>> extends LinearField<T> {

	private Ring<E> elementsField;
	private E[] exampleArray;
	private Function<E, E> copyFunction;

	@FunctionalInterface
	/**
	 * Must behave as {@code Tensor::new}.
	 *
	 * @author dzu
	 *
	 * @param <E> Type of elements in tensor.
	 * @param <T> Type of a tensor itself.
	 */
	public interface TensorCreator<E, T extends Tensor<E>> {
		T create(int order, int size, Function<E, E> copyFunction, E... components);
	}

	private TensorCreator<E, T> tensorFactory;

	public TensorField(Ring<E> elemetsFieldIn, E[] exampleArrayIn, Function<E, E> copyFunctionIn,
			TensorCreator<E, T> creatorIn) {
		elementsField = elemetsFieldIn;
		this.exampleArray = exampleArrayIn;
		Arrays.fill(exampleArray, elemetsFieldIn.zero());
		this.copyFunction = copyFunctionIn;
		this.tensorFactory = creatorIn;
	}

	@Override
	/**
	 * @throws IllegalArgumentException If tensors specified are differ in order or
	 *                                  size.
	 */
	public T add(T t1, T t2, boolean write1, boolean write2) throws IllegalArgumentException {
		if ((t1.order != t2.order) && (t1.size != t2.size)) {
			throw (new IllegalArgumentException("Tensors do not match in rank!"));
		}
		if (write1) {
			for (int i = 0; i < t1.components.length; i++) {
				elementsField.add(t1.components[i], t2.components[i], true, false);
			}
			if (write2) {
				t2.components = ArrayUtil.deepCopy(t1.components, copyFunction, t1.components.length);
			}
			return t1;
		}
		if (write2) {
			for (int i = 0; i < t2.components.length; i++) {
				elementsField.add(t1.components[i], t2.components[i], false, true);
			}
			return t2;
		}
		E[] resComponents = ArrayUtil.deepCopy(t1.components, copyFunction, t1.components.length);
		for (int i = 0; i < resComponents.length; i++) {
			resComponents[i] = elementsField.add(t1.components[i], t2.components[i]);
		}
		return tensorFactory.create(t1.order, t1.size, null, resComponents);
	}

	@Override
	public T neg(T t, boolean write) {
		if (write) {
			for (E component : t.components) {
				elementsField.neg(component, true);
			}
			return t;
		}
		E[] resComponents = ArrayUtil.deepCopy(t.components, copyFunction, t.components.length);
		for (E resComponent : resComponents) {
			elementsField.neg(resComponent, true);
		}
		return tensorFactory.create(t.order, t.size, null, resComponents);
	}

	/**
	 * Creates unlinked copy of a tensor.
	 *
	 * @param t Tensor to copy.
	 * @return A copied instance.
	 */
	public T copy(T t) {
		return tensorFactory.create(t.order, t.size, null,
				ArrayUtil.deepCopy(t.components, copyFunction, t.components.length));
	}

	@Override
	// Example of overridden method.
	/**
	 * @throws IllegalArgumentException If tensors specified are differ in order or
	 *                                  size.
	 */
	public T sub(T t1, T t2, boolean write1, boolean write2) throws IllegalArgumentException {
		if ((t1.order != t2.order) && (t1.size != t2.size)) {
			throw (new IllegalArgumentException("Tensors do not match in rank!"));
		}
		if (write1) {
			for (int i = 0; i < t1.components.length; i++) {
				elementsField.sub(t1.components[i], t2.components[i], true, false);
			}
			if (write2) {
				t2.components = ArrayUtil.deepCopy(t1.components, copyFunction, t1.components.length);
			}
			return t1;
		}
		if (write2) {
			for (int i = 0; i < t2.components.length; i++) {
				elementsField.sub(t1.components[i], t2.components[i], false, true);
			}
			return t2;
		}
		E[] resComponents = ArrayUtil.deepCopy(t1.components, copyFunction, t1.components.length);
		for (int i = 0; i < resComponents.length; i++) {
			resComponents[i] = elementsField.sub(t1.components[i], t2.components[i]);
		}
		return tensorFactory.create(t1.order, t1.size, null, resComponents);
	}

	private int cost1Order = 1, cost1Size = 1;

	public T zero(T t) {
		return zero(t.order, t.size);
	}

	public T zero(int order, int size) {
		cost1Order = order;
		cost1Size = size;
		return zero();
	}

	@Override
	public T zero() {
		return tensorFactory.create(cost1Order, cost1Size, null, ArrayUtil.deepCopy(exampleArray, copyFunction,
				(int) Math.pow(cost1Size, cost1Order), elementsField.zero()));
	}

	/**
	 * Return Fold of a specified tensors.
	 *
	 * @param t1      First tensor.
	 * @param t2      Second tensor.
	 * @param rank    Number of indexes used.
	 * @param indexes Array, containing used indexes for both arguments.
	 * @throws {@code IllegalArgumentException} If size of tensor don't match, or
	 * {@code indexes} contains repeating indexes for same array or a total of less
	 * than order*2 elements
	 * @return
	 */
	public T fold(T t1, T t2, int... indexes) {
		int rank = indexes.length / 2;
		if ((t1.size != t2.size) || (indexes.length < (rank * 2))) {
			throw new IllegalArgumentException("Tensors differ in size!");
		}
		int[] t1Indexes = Arrays.stream(Util.orderInt(0, t1.order))
				.filter(n -> (Arrays.binarySearch(indexes, 0, rank, n) < 0)
						|| (n != indexes[Arrays.binarySearch(indexes, 0, rank, n)]))
				.toArray();
		int[] t1Coords = new int[t1.order];
		int[] t2Indexes = Arrays.stream(Util.orderInt(0, t2.order))
				.filter(n -> (Arrays.binarySearch(indexes, rank, rank * 2, n) < 0)
						|| (n != indexes[Arrays.binarySearch(indexes, rank, rank * 2, n)]))
				.toArray();
		int[] t2Coords = new int[t2.order];
//		System.out.println(String.format("Tensor indexes: \nt1:%s,\nt2:%s\nFold indexes:%s", Arrays.toString(t1Indexes),
//				Arrays.toString(t2Indexes), Arrays.toString(indexes)));
		int size = t1.size;
		int order = (t1.order + t2.order) - (rank * 2);
		E[] resComp = ArrayUtil.deepCopy(exampleArray, copyFunction, (int) Math.pow(size, order), elementsField.zero());
		T res = (T) new Tensor<E>(order, size, null, resComp);
		IndexIterator resIter = new IndexIterator(order, size);
		IndexIterator sumIter = new IndexIterator(rank, size);
		int[] resPos, sumPos;
		E tmpRes;
		do {
			resPos = resIter.next();
			for (int i = 0; i < t1Indexes.length; i++) {
				t1Coords[t1Indexes[i]] = resPos[i];
			}
			for (int i = 0; i < t2Indexes.length; i++) {
				t2Coords[t2Indexes[i]] = resPos[t1Indexes.length + i];
			}
			tmpRes = elementsField.zero();
			do {
				sumPos = sumIter.next();
				for (int i = 0; i < rank; i++) {
					t1Coords[indexes[i]] = t2Coords[indexes[rank + i]] = sumPos[i];
				}
//				System.out.println(String.format(
//						"Current tensor coordinates:\nt1:%s,\nt2:%s\nRes coordinates:%s,\nSum coordinates:%s",
//						Arrays.toString(t1Coords), Arrays.toString(t2Coords), Arrays.toString(resPos),
//						Arrays.toString(sumPos)));
				tmpRes = elementsField.add(tmpRes,
						elementsField.mul(t1.getComponentAt(t1Coords), t2.getComponentAt(t2Coords)), true, false);
			} while (sumIter.hasNext());

			res.setComponentAt(tmpRes, resPos);
			sumIter.reset();
		} while (resIter.hasNext());
		return res;
	}

	public T selfSymFold(T t, int... indexes) {
		int rank = indexes.length;
		int[] newIndexes = new int[rank * 2];
		for (int i = 0; i < rank; i++) {
			newIndexes[i] = newIndexes[rank + i] = indexes[i];
		}
		return this.fold(t, t, newIndexes);
	}

	@Override
	public T scale(T t, double scalar, boolean write) {
		if (write) {
			for (E component : t.components) {
				elementsField.scale(component, scalar, true);
			}
			return t;
		}
		E[] resComponents = ArrayUtil.deepCopy(t.components, copyFunction, t.components.length);
		for (E resComponent : resComponents) {
			elementsField.scale(resComponent, scalar, true);
		}
		return tensorFactory.create(t.order, t.size, null, resComponents);
	}

	public static <E, T extends Tensor<E>> T componentTransform(T t, BiConsumer<int[], E> transformer) {
		IndexIterator iter = new IndexIterator(t.order, t.size);
		int[] pos;
		while (iter.hasNext()) {
			pos = iter.next();
			transformer.accept(pos, t.getComponentAt(pos));
		}
		return t;
	}
}
