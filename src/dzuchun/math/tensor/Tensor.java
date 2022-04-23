package dzuchun.math.tensor;

import java.util.Arrays;
import java.util.function.Function;

import dzuchun.util.ArrayUtil;
import dzuchun.util.Util;

public class Tensor<E> {
	public final int order;
	public final int size;
	protected E[] components;
//	protected Collection<Referrenced<E>> children;

	/**
	 * Creates a new tensor object.
	 *
	 * @param orderIn      Order of a tensor.
	 * @param sizeIn       Size of a tensor.
	 * @param leaveLink    If created tensor should be changed as specified array is
	 *                     changed.
	 * @param copyFunction Function that copies elements of a tesor. Leave null to
	 *                     not perform a deep copy.
	 * @param componentsIn Array of coordinates.
	 */
	@SafeVarargs
	public Tensor(int orderIn, int sizeIn, Function<E, E> copyFunction, E... componentsIn) {
		this.order = orderIn;
		this.size = sizeIn;
		int reqLength = (int) Math.pow(size, order);
		if (reqLength != componentsIn.length) {
			throw new IllegalArgumentException("Number of components specified for a tensor is incorrect");
		}
		if (copyFunction == null) {
			components = componentsIn;
		} else {
			components = ArrayUtil.deepCopy(componentsIn, copyFunction, reqLength, null);
		}
	}

	@SuppressWarnings("unchecked")
	public Tensor(int orderIn, int sizeIn, Function<int[], E> elementSupplier) {
		this(orderIn, sizeIn, null,
				(E[]) Util.collect(new IndexIterator(orderIn, sizeIn)).stream().map(elementSupplier).toArray());
	}

	public Tensor(Tensor<E> t, Function<E, E> copyFunction) {
		this(t.order, t.size, copyFunction, t.components);
	}

	protected int getInternalIndex(int... indexes) {
		int res = 0;
		int prefix = 1;
		for (int i = indexes.length - 1; i >= 0; i--) {
			res += prefix * indexes[i];
			prefix *= this.size;
		}
		return res;
	}

	public E getComponentAt(int... indexes) {
		if (indexes.length != this.order) {
			throw new IllegalArgumentException();
		}
		return this.getComponent(this.getInternalIndex(indexes));
	}

	public void setComponentAt(E value, int... indexes) {
		if (indexes.length != this.order) {
			throw new IllegalArgumentException();
		}
		this.setComponent(value, this.getInternalIndex(indexes));
	}

	public E firstComponent() {
		return this.components[0];
	}

	protected E getComponent(int d) {
		return this.components[d];
	}

	protected void setComponent(E value, int d) {
		this.components[d] = value;
	}

	public String simpleToString() {
		return Arrays.toString(components);
	}

//	// TODO define
//	@Deprecated
//	public static class Referrenced<E> extends Tensor<E> {
//		private final Tensor<E> parent;
//		@SuppressWarnings("unused")
//		private final int[] indexes;
//		@SuppressWarnings("unused")
//		private final int[] cells;
//		protected boolean wasChanged;
//
//		public Referrenced(Tensor<E> parentIn, int orderIn, int size, int... scriptIn) {
//			super(orderIn, size, false);
//			this.parent = parentIn;
//			parent.children.add(this);
//			int scriptIndexes = parent.order - this.order;
//			indexes = Arrays.copyOfRange(scriptIn, 0, scriptIndexes);
//			int scriptCells = parent.size - this.size;
//			cells = Arrays.copyOfRange(scriptIn, scriptIndexes, scriptIndexes + scriptCells);
//			wasChanged = true; // Setting to true, so components will be updated
//		}
//
//		public void updateComponents() {
//
//			wasChanged = false;
//		}
//
//		protected void checkDelete() {
//			if (this.children.isEmpty()) {
//				parent.children.remove(this);
//			}
//		}
//	}
}