package dzuchun.math.solve;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import dzuchun.math.tensor.Tensor;
import dzuchun.math.tensor.TensorField;
import dzuchun.util.Util;

public class DifferentialEquation {
	/**
	 * Solves ordinary first-order tensor differential equation using 4th-order
	 * adaptive Runge-Kutta method. Quite "dirty method", so {@code System.gc()}
	 * after it might be nesessary.
	 *
	 * @param <E>          Type of elements in tensors.
	 * @param tb           Initial time.
	 * @param te           Ending time.
	 * @param dt           Default time step.
	 * @param qt           Minimal time step.
	 * @param badCondition Predicate to determine a case when calculations must be
	 *                     more precise.
	 * @param y0           Initial tensor state
	 * @param derivative   Function, that should return a {@code y.order} tensor
	 *                     that represents a derivative of a tensor at time
	 *                     {@code t} point {@code y}.
	 * @param tF           A field containing operations on a used tensors.
	 * @return A map representing change of a tensor over time.
	 */
	public static <E, T extends Tensor<E>> Map<Double, T> sOFOTDERK(double tb, double te, double dt, double qt,
			Predicate<E> badCondition, T y0, BiFunction<Double, T, T> derivative, TensorField<E, T> tF) {
		// TODO maybe I should use a faster map
		Map<Double, T> res = new LinkedHashMap<Double, T>(0);
		res.put(tb, y0);
		T y = tF.copy(y0);
		T predict;
		T correct;
		double t = tb;
		double step, tmpT; // An interval used currently for approximation
		while (t <= te) {
			step = dt;
			// Prediction -- 2^power shifts
			tmpT = t;
			correct = y;
			while (tmpT < (t + dt)) {
				correct = DifferentialEquation.makeStepKutta(tmpT, step, correct, derivative, tF);
				tmpT += step;
//				System.out.println(String.format("Predict at %.6f is %s", tmpT, correct.simpleToString()));
			}
			do {
				predict = correct;
				// Correction -- 2*2^power shifts
				step /= 2;
				tmpT = t;
				correct = y;
				while (tmpT < (t + dt)) {
					correct = DifferentialEquation.makeStepKutta(tmpT, step, correct, derivative, tF);
					tmpT += step;
//					System.out.println(String.format("Predict at %.6f is %s", tmpT, correct.simpleToString()));
				}
//				System.out.println(String.format("Norm differ at %.6f: %s.... %s", t,
//						DifferentialEquation.getNormDiffer(predict, correct, tF),
//						(badCondition.test(DifferentialEquation.getNormDiffer(predict, correct, tF)) ? "failed"
//								: "passed!")));
				if (step < qt) {
//					printf("WARNING! REACHED STEP QUANT\n");
					break;
				}
			} while (badCondition.test(DifferentialEquation.getNormDiffer(predict, correct, tF)));
			// saving current state and advance
			t += dt;
			res.put(t, correct);
			y = correct;
		}
		return res;
	}

	private static <E, T extends Tensor<E>> T makeStepKutta(double t, double step, T currentState,
			BiFunction<Double, T, T> derivative, TensorField<E, T> tF) {
		T tmpR, k1, k2, k3, k4;
		// k1
		k1 = tF.scale(derivative.apply(t, currentState), step, true);
		tmpR = tF.add(currentState, tF.scale(k1, 1.0d / 2, false), false, true);
		// k2
		k2 = tF.scale(derivative.apply(t + (step / 2), tmpR), step, true);
		tmpR = tF.add(currentState, tF.scale(k2, 1.0d / 2, false), false, true);
		// k3
		k3 = tF.scale(derivative.apply(t + (step / 2), tmpR), step, true);
		tmpR = tF.add(currentState, k3, false, false);
		// k4
		k4 = tF.scale(derivative.apply(t + step, tmpR), step, true);
		// Calculating result: res = current + (k1+k4)/6 + (k2+k3)/3
//		System.out.println(String.format("At %.6f:\nk1=%s\nk2=%s\nk3=%s\nk4=%s", t, k1.simpleToString(),
//				k2.simpleToString(), k3.simpleToString(), k4.simpleToString()));
		return tF.add(currentState, tF.add(tF.scale(tF.add(k1, k4, true, false), 1.0d / 6, true),
				tF.scale(tF.add(k2, k3, true, false), 1.0d / 3, true)), false, true);
	}

	private static <E, T extends Tensor<E>> E getNormDiffer(T t1, T t2, TensorField<E, T> tF) {
		return tF.selfSymFold(tF.sub(t1, t2), Util.orderInt(0, t1.order)).firstComponent();
	}
}
