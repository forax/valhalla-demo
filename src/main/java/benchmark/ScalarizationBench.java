package benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = { "--enable-preview" })
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class ScalarizationBench {

  static final int WIDTH  = 800;
  static final int HEIGHT = 800;
  static final int MAX_ITER = 256;

  static final double X_MIN = -2.5, X_MAX = 1.0;
  static final double Y_MIN = -1.5, Y_MAX = 1.5;

  final BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

  static int iteratePrimitive(double cx, double cy) {
    var zx = 0.0;
    var zy = 0.0;
    for (var i = 0; i < MAX_ITER; i++) {
      var zx2 = zx * zx;
      var zy2 = zy * zy;
      if (zx2 + zy2 > 4.0) return i;   // escaped
      zy = 2 * zx * zy + cy;
      zx = zx2 - zy2 + cx;
    }
    return MAX_ITER;
  }

  record IdentityComplex(double re, double im) {
    IdentityComplex add(IdentityComplex o)  {
      return new IdentityComplex(re + o.re, im + o.im);
    }
    IdentityComplex square() {
      return new IdentityComplex(re*re - im*im, 2*re*im);
    }
    double absSquared() {
      return re*re + im*im;
    }
  }
  static int iterateIdentity(IdentityComplex c) {
    var z = new IdentityComplex(0, 0);
    for (var i = 0; i < MAX_ITER; i++) {
      if (z.absSquared() > 4.0) return i;  // escaped
      z = z.square().add(c);
    }
    return MAX_ITER;
  }

  value record ValueComplex(double re, double im) {
    ValueComplex add(ValueComplex o)  {
      return new ValueComplex(re + o.re, im + o.im);
    }
    ValueComplex square() {
      return new ValueComplex(re*re - im*im, 2*re*im);
    }
    double absSquared() {
      return re*re + im*im;
    }
  }
  static int iterateValue(ValueComplex c) {
    var z = new ValueComplex(0, 0);
    for (var i = 0; i < MAX_ITER; i++) {
      if (z.absSquared() > 4.0) return i;  // escaped
      z = z.square().add(c);
    }
    return MAX_ITER;
  }

  private int colorFor(int iter) {
    if (iter == MAX_ITER) return Color.BLACK.getRGB();
    float hue = (float) iter / MAX_ITER;
    return Color.HSBtoRGB(hue, 0.8f, 1.0f);
  }

  @Benchmark
  public void renderPrimitive() {
    for (var px = 0; px < WIDTH; px++) {
      for (var py = 0; py < HEIGHT; py++) {
        var cx = X_MIN + (X_MAX - X_MIN) * px / (WIDTH  - 1);
        var cy = Y_MIN + (Y_MAX - Y_MIN) * py / (HEIGHT - 1);

        image.setRGB(px, py, colorFor(iteratePrimitive(cx, cy)));
      }
    }
  }

  @Benchmark
  public void renderIdentity() {
    for (var px = 0; px < WIDTH; px++) {
      for (var py = 0; py < HEIGHT; py++) {
        var cx = X_MIN + (X_MAX - X_MIN) * px / (WIDTH  - 1);
        var cy = Y_MIN + (Y_MAX - Y_MIN) * py / (HEIGHT - 1);

        image.setRGB(px, py, colorFor(iterateIdentity(new IdentityComplex(cx, cy))));
      }
    }
  }

  @Benchmark
  public void renderValue() {
    for (var px = 0; px < WIDTH; px++) {
      for (var py = 0; py < HEIGHT; py++) {
        var cx = X_MIN + (X_MAX - X_MIN) * px / (WIDTH  - 1);
        var cy = Y_MIN + (Y_MAX - Y_MIN) * py / (HEIGHT - 1);

        image.setRGB(px, py, colorFor(iterateValue(new ValueComplex(cx, cy))));
      }
    }
  }
}
