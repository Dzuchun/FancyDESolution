package dzuchun.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

public class Util {
	public static int[] orderInt(int from, int to) {
		int[] res = new int[to - from];
		for (int i = 0; i < res.length; i++) {
			res[i] = from + i;
		}
		return res;
	}

	public static <E> Collection<E> collect(Iterator<E> iter) {
		final Vector<E> res = new Vector<E>(0);
		while (iter.hasNext()) {
			res.add(iter.next());
		}
		res.trimToSize();
		return res;
	}

	public static double smoothClamp(double f, double min, double max) {
		double c = (max + min) / 2;
		return c + (((max - min) / Math.PI) * Math.atan(10 * (f - c)));
	}
}
