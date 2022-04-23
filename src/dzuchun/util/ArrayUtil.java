package dzuchun.util;

import java.util.Arrays;
import java.util.function.Function;

public class ArrayUtil {
	public static <T> T[] deepCopy(T[] source, Function<T, T> copyFunction, int length, T filler) {
		T[] res = Arrays.copyOf(source, length);
		if (length > source.length) {
			Arrays.fill(res, source.length, length, filler);
		}
		for (int i = 0; i < res.length; i++) {
			res[i] = copyFunction.apply(res[i]);
		}
		return res;
	}

	public static <T> T[] deepCopy(T[] source, Function<T, T> copyFunction, int length) {
		return ArrayUtil.deepCopy(source, copyFunction, length, null);
	}

	public static <T> T[] deepCopy(T[] source, Function<T, T> copyFunction) {
		return ArrayUtil.deepCopy(source, copyFunction, source.length, null);
	}
}
