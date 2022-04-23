import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

import com.memorynotfound.image.GifSequenceWriter;

import dzuchun.math.Ring;
import dzuchun.math.solve.DifferentialEquation;
import dzuchun.math.tensor.Tensor;
import dzuchun.math.tensor.TensorField;
import dzuchun.util.PrimitiveWrapper;

public class Main {

	public static class DWrapper extends PrimitiveWrapper<Double> {

		public DWrapper(Double valueIn) {
			super(valueIn);
		}

		public DWrapper copy() {
			return new DWrapper(this.value);
		}

	}

	public static Ring<DWrapper> dRing = new Ring<DWrapper>() {

		@Override
		public Main.DWrapper mul(Main.DWrapper t1, Main.DWrapper t2, boolean write1, boolean write2) {
			double res = t1.value * t2.value;
			if (write1) {
				t1.value = res;
				if (write2) {
					t2.value = res;
				}
				return t1;
			} else if (write2) {
				t2.value = res;
				return t2;
			}
			return new DWrapper(res);
		}

		@Override
		public Main.DWrapper one() {
			return new DWrapper(1.0d);
		}

		@Override
		public Main.DWrapper add(Main.DWrapper t1, Main.DWrapper t2, boolean write1, boolean write2) {
			double res = t1.value + t2.value;
			if (write1) {
				t1.value = res;
				if (write2) {
					t2.value = res;
				}
				return t1;
			} else if (write2) {
				t2.value = res;
				return t2;
			}
			return new DWrapper(res);
		}

		@Override
		public Main.DWrapper neg(Main.DWrapper t, boolean write) {
			double res = -t.value;
			if (write) {
				t.value = res;
				return t;
			}
			return new DWrapper(res);
		}

		@Override
		public Main.DWrapper scale(Main.DWrapper t, double scalar, boolean write) {
			double res = t.value * scalar;
			if (write) {
				t.value = res;
				return t;
			}
			return new DWrapper(res);
		}

		@Override
		public Main.DWrapper zero() {
			return new DWrapper(0.0d);
		}
	};

	public static class State extends Tensor<DWrapper> {

		public State(double x, double vx) {
			super(1, 2, DWrapper::copy, new DWrapper(x), new DWrapper(vx));
		}

		public State(DWrapper... wrappers) {
			super(1, 2, DWrapper::copy, wrappers);
		}

		public DWrapper coord() {
			return this.getComponentAt(0);
		}

		public DWrapper speed() {
			return this.getComponentAt(1);
		}

	}

	public static TensorField<DWrapper, State> tF = new TensorField<DWrapper, State>(dRing, new DWrapper[1],
			DWrapper::copy, (o, s, cF, comp) -> new State(comp));

	public static void main(String[] args) {
		// Physics params
		final double w02 = 1;
		final double gamma = 0;

		final int states = 100;

		// Simulation params
		State state0;
		final double tb = 0;
		final double te = 20;
		final double dt = 0.001;
		final int frms = 60;
		final int df = (int) (te / dt / frms);

		// Visuals params
		final double xMin = -3 * Math.PI;
		final double xMax = 3 * Math.PI;
		final double xC = (xMax + xMin) / 2;
		final double vMin = -3;
		final double vMax = 3;
		final double vC = (vMax + vMin) / 2;

		final int frameWidth = 192 * 5;
		final int frameHeight = 108 * 5;

		@SuppressWarnings("unchecked")
		Map<Double, State>[] results = new Map[states];
		for (int n = 0; n < states; n++) {
			System.out.print(String.format("Running simulation %d/%d...", n, states));
			state0 = new State(0, 0.06*n-3);
			results[n] = DifferentialEquation.sOFOTDERK(tb, te, dt, dt / 1000, e -> e.value > 0.000001, state0,
					(t, state) -> {
						return new State(state.speed().value,
								-gamma * state.speed().value - w02 * Math.sin(state.coord().value));
					}, tF);
			System.out.println("done!");
		}
		BufferedImage frame = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_RGB);
		// Creating and transforming graphics
		Graphics2D g = frame.createGraphics();

		System.out.println("Simulaions finished, creating gif...");
		try {
			// Gif output setup
			final ImageOutputStream output = new FileImageOutputStream(
					new File("./tmp/test-" + System.currentTimeMillis() + ".gif"));
			final GifSequenceWriter writer = new GifSequenceWriter(output, BufferedImage.TYPE_4BYTE_ABGR, 1, true);

			// Starting frame generation
			@SuppressWarnings("unchecked")
			final Iterator<State>[] streams = new Iterator[states];
			for (int n = 0; n < states; n++) {
				streams[n] = results[n].entrySet().stream()
						.sorted((e2, e1) -> (int) ((e2.getKey() - e1.getKey()) / te * 1000)).map(e -> e.getValue())
						.iterator();
			}
			double[] prevX = new double[states];
			double[] prevY = new double[states];
			Color[] colors = new Color[states];
			State s;
			int frameCounter = df;
			// Initialising previous points and colors
			Color skyBlue = new Color(0, 87, 184);
			Color wheatYellow = new Color(255, 215, 0);
			for (int n = 0; n < states; n++) {
				s = streams[n].next();
				prevX[n] = (s.coord().value - xC) / (xMax - xMin) * frameWidth + frameWidth / 2;
				prevY[n] = (s.speed().value - vC) / (vMax - vMin) * frameHeight + frameHeight / 2;
//				colors[n] = new Color((int) (127.5 + 128 * (Math.cos(2 * Math.PI * n / states))),
//						(int) (127.5 + 128 * (Math.cos(2 * Math.PI * n / states + 2 * Math.PI / 3))),
//						(int) (127.5 + 128 * (Math.cos(2 * Math.PI * n / states - 2 * Math.PI / 3))));
				colors[n] = (n >= states / 2) ? bleach(skyBlue, ((double) n / states) - 0.5)
						: bleach(wheatYellow, (0.5 - ((double) n / states)));
			}
			double tmpX, tmpY;
			int fr = 1;
			while (streams[0].hasNext()) {
				// Draw corresponding lines
				for (int n = 0; n < states; n++) {
					s = streams[n].next();
					g.setColor(colors[n]);
					tmpX = (s.coord().value - xC) / (xMax - xMin) * frameWidth + frameWidth / 2;
					tmpY = (s.speed().value - vC) / (vMax - vMin) * frameHeight + frameHeight / 2;
					g.drawLine((int) tmpX, (int) tmpY, (int) prevX[n], (int) prevY[n]);
					prevX[n] = tmpX;
					prevY[n] = tmpY;
				}

				// Write frame if needed
				frameCounter--;
				if (frameCounter == 0) {
					System.out.println(String.format("Writing frame %d/%d", fr++, frms));
					writer.writeToSequence(frame);
					frameCounter = df;
				}
			}
			writer.close();
			output.close();
			System.out.println("Gif written!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static Color bleach(Color c, double bleachFactor) {
		return new Color((int) (c.getRed() + (255 - c.getRed()) * bleachFactor),
				(int) (c.getGreen() + (255 - c.getGreen()) * bleachFactor),
				(int) (c.getBlue() + (255 - c.getBlue()) * bleachFactor));
	}

}
