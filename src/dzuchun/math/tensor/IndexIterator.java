package dzuchun.math.tensor;

import java.util.Arrays;
import java.util.Iterator;

public class IndexIterator implements Iterator<int[]> {

	private final int indexes, size;
	private int[] current;

	public IndexIterator(int indexesIn, int sizeIn) {
		indexes = indexesIn;
		size = sizeIn;
		current = new int[indexes];
		if (indexes > 0) {
			current[indexes - 1] = -1;
		}
	}

	@Override
	public boolean hasNext() {
		for (int i : current) {
			if (i != (size - 1)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int[] next() {
		for (int i = indexes - 1; i > 0; i--) {
			current[i]++;
			if (current[i] != size) {
				return Arrays.copyOf(current, indexes);
			}
			current[i] = 0;
		}
		if (indexes > 0) {
			current[0]++;
		}
		return Arrays.copyOf(current, indexes);
	}

	public void reset() {
		Arrays.fill(current, 0);
		if (indexes > 0) {
			current[indexes - 1] = -1;
		}
	}

}