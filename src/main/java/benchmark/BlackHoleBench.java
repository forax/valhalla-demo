package benchmark;

import jdk.internal.value.ValueClass;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

// Benchmark                                                   Mode  Cnt          Score    Error   Units
//BlackHoleBench.renderWithIdentity                           avgt    5         52,593 ±  1,389   ms/op
//BlackHoleBench.renderWithIdentity:gc.alloc.rate             avgt    5       2022,373 ± 53,553  MB/sec
//BlackHoleBench.renderWithIdentity:gc.alloc.rate.norm        avgt    5  111536309,830 ±  9,108    B/op
//BlackHoleBench.renderWithIdentity:gc.count                  avgt    5        128,000           counts
//BlackHoleBench.renderWithIdentity:gc.time                   avgt    5         51,000               ms
//BlackHoleBench.renderWithValue                              avgt    5         48,566 ±  1,727   ms/op
//BlackHoleBench.renderWithValue:gc.alloc.rate                avgt    5          4,151 ±  0,147  MB/sec
//BlackHoleBench.renderWithValue:gc.alloc.rate.norm           avgt    5     211368,714 ±  7,780    B/op
//BlackHoleBench.renderWithValue:gc.count                     avgt    5            ≈ 0           counts
//BlackHoleBench.renderWithValueFlatArray                     avgt    5         48,312 ±  1,441   ms/op
//BlackHoleBench.renderWithValueFlatArray:gc.alloc.rate       avgt    5          4,172 ±  0,125  MB/sec
//BlackHoleBench.renderWithValueFlatArray:gc.alloc.rate.norm  avgt    5     211368,343 ±  9,512    B/op
//BlackHoleBench.renderWithValueFlatArray:gc.count            avgt    5            ≈ 0           counts

@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = { "--enable-preview", "--add-exports=java.base/jdk.internal.value=ALL-UNNAMED" })
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class BlackHoleBench {

  static final int WIDTH = 80;
  static final int HEIGHT = 60;

  static final double DT = 0.05;         // Ray step size
  static final double GM = 1.5;          // Gravity strength
  static final double RS = 1.0;          // Event horizon radius

  static final class Value {
    value record Vec3(double x, double y, double z) {
      Vec3 add(Vec3 o) { return new Vec3(x + o.x, y + o.y, z + o.z); }
      Vec3 sub(Vec3 o) { return new Vec3(x - o.x, y - o.y, z - o.z); }
      Vec3 mul(double s) { return new Vec3(x * s, y * s, z * s); }
      double mag() { return Math.sqrt(x * x + y * y + z * z); }
      Vec3 norm() {
        double m = mag();
        return m == 0 ? this : mul(1.0 / m);
      }
      public Vec3 cross(Vec3 o) {
        return new Vec3(y * o.z - z * o.y, z * o.x - x * o.z, x * o.y - y * o.x);
      }
    }

    value record Color(double r, double g, double b) {
      static final Color BLACK = new Color(0, 0, 0);

      Color add(Color o) { return new Color(r + o.r, g + o.g, b + o.b); }
      Color mul(double s) { return new Color(r * s, g * s, b * s); }
      Color clamp() { return new Color(Math.clamp(r, 0, 1), Math.clamp(g, 0, 1), Math.clamp(b, 0, 1)); }
    }

    static void generateBlackHole(Color[] pixels) {
      // Camera setup
      var camPos = new Vec3(0, 1.5, 7.0);
      var lookAt = new Vec3(0, 0, 0);
      var forward = lookAt.sub(camPos).norm();
      var up = new Vec3(0, 1, 0);
      var right = forward.cross(up).norm();
      var trueUp = right.cross(forward).norm();

      // Render loop
      for (var py = 0; py < HEIGHT; py++) {
        for (var px = 0; px < WIDTH; px++) {

          // Map pixel to -1.0 to 1.0 coordinates
          var u = (px - WIDTH / 2.0) / (HEIGHT / 2.0);
          var v = -(py - HEIGHT / 2.0) / (HEIGHT / 2.0);

          // Initial ray position and velocity (speed of light = 1.0)
          var vel = forward.add(right.mul(u)).add(trueUp.mul(v)).norm();

          var color = rayMarching(camPos, vel);

          pixels[py * WIDTH + px] = color.clamp();
        }
      }
    }

    static Color rayMarching(Vec3 pos, Vec3 vel) {
      var color = Color.BLACK;

      // Step the photon through the gravity field
      for (var step = 0; step < 400; step++) {
        var r = pos.mag();

        if (r < RS) {
          return Color.BLACK; // Photon fell into the event horizon
        }
        if (r > 20.0) {
          return Color.BLACK; // Photon escaped to deep space
        }

        // Calculate gravity acceleration: a = -GM / r^2 towards origin
        var acc = pos.norm().mul(-GM / (r * r));

        // Euler integration for position and velocity
        var nextVel = vel.add(acc.mul(DT)).norm(); // Normalize to maintain speed of light
        var nextPos = pos.add(nextVel.mul(DT));

        // Check if the ray crossed the accretion disk (the X-Z plane, where Y = 0)
        if (pos.y() * nextPos.y() < 0) {
          // Find exact distance from center where it hit the disk
          var hitR = Math.sqrt(pos.x() * pos.x() + pos.z() * pos.z());

          // Disk exists between radius 1.5 and 4.0
          if (hitR > 1.5 && hitR < 4.0) {
            // Calculate temperature/color (hotter closer to the black hole)
            var heat = (4.0 - hitR) / 2.5;
            var diskBase = new Color(1.0, 0.5 + 0.5 * heat, heat * heat);

            // Fake Doppler beaming (brighter on one side as it spins towards us)
            var doppler = 1.0 + (pos.x() / hitR) * 0.8;

            // Soften the inner and outer edges of the disk
            var alpha = Math.sin((hitR - 1.5) / 2.5 * Math.PI);

            // Additively blend the disk light to our ray
            color = color.add(diskBase.mul(doppler * alpha * 0.6));
          }
        }

        vel = nextVel;
        pos = nextPos;
      }

      return color;
    }
  }

  static final class Identity {
    record Vec3(double x, double y, double z) {
      Vec3 add(Vec3 o) { return new Vec3(x + o.x, y + o.y, z + o.z); }
      Vec3 sub(Vec3 o) { return new Vec3(x - o.x, y - o.y, z - o.z); }
      Vec3 mul(double s) { return new Vec3(x * s, y * s, z * s); }
      double mag() { return Math.sqrt(x * x + y * y + z * z); }
      Vec3 norm() {
        double m = mag();
        return m == 0 ? this : mul(1.0 / m);
      }
      public Vec3 cross(Vec3 o) {
        return new Vec3(y * o.z - z * o.y, z * o.x - x * o.z, x * o.y - y * o.x);
      }
    }

    record Color(double r, double g, double b) {
      static final Color BLACK = new Color(0, 0, 0);

      Color add(Color o) { return new Color(r + o.r, g + o.g, b + o.b); }
      Color mul(double s) { return new Color(r * s, g * s, b * s); }
      Color clamp() { return new Color(Math.clamp(r, 0, 1), Math.clamp(g, 0, 1), Math.clamp(b, 0, 1)); }
    }

    static void generateBlackHole(Color[] pixels) {
      // Camera setup
      var camPos = new Vec3(0, 1.5, 7.0);
      var lookAt = new Vec3(0, 0, 0);
      var forward = lookAt.sub(camPos).norm();
      var up = new Vec3(0, 1, 0);
      var right = forward.cross(up).norm();
      var trueUp = right.cross(forward).norm();

      // Render loop
      for (var py = 0; py < HEIGHT; py++) {
        for (var px = 0; px < WIDTH; px++) {

          // Map pixel to -1.0 to 1.0 coordinates
          var u = (px - WIDTH / 2.0) / (HEIGHT / 2.0);
          var v = -(py - HEIGHT / 2.0) / (HEIGHT / 2.0);

          // Initial ray position and velocity (speed of light = 1.0)
          var vel = forward.add(right.mul(u)).add(trueUp.mul(v)).norm();

          var color = rayMarching(camPos, vel);

          pixels[py * WIDTH + px] = color.clamp();
        }
      }
    }

    static Color rayMarching(Vec3 pos, Vec3 vel) {
      var color = Color.BLACK;

      // Step the photon through the gravity field
      for (var step = 0; step < 400; step++) {
        var r = pos.mag();

        if (r < RS) {
          return Color.BLACK; // Photon fell into the event horizon (black)
        }
        if (r > 20.0) {
          return Color.BLACK; // Photon escaped to deep space
        }

        // Calculate gravity acceleration: a = -GM / r^2 towards origin
        var acc = pos.norm().mul(-GM / (r * r));

        // Euler integration for position and velocity
        var nextVel = vel.add(acc.mul(DT)).norm(); // Normalize to maintain speed of light
        var nextPos = pos.add(nextVel.mul(DT));

        // Check if the ray crossed the accretion disk (the X-Z plane, where Y = 0)
        if (pos.y() * nextPos.y() < 0) {
          // Find exact distance from center where it hit the disk
          var hitR = Math.sqrt(pos.x() * pos.x() + pos.z() * pos.z());

          // Disk exists between radius 1.5 and 4.0
          if (hitR > 1.5 && hitR < 4.0) {
            // Calculate temperature/color (hotter closer to the black hole)
            var heat = (4.0 - hitR) / 2.5;
            var diskBase = new Color(1.0, 0.5 + 0.5 * heat, heat * heat);

            // Fake Doppler beaming (brighter on one side as it spins towards us)
            var doppler = 1.0 + (pos.x() / hitR) * 0.8;

            // Soften the inner and outer edges of the disk
            var alpha = Math.sin((hitR - 1.5) / 2.5 * Math.PI);

            // Additively blend the disk light to our ray
            color = color.add(diskBase.mul(doppler * alpha * 0.6));
          }
        }

        vel = nextVel;
        pos = nextPos;
      }

      return color;
    }
  }

  @Benchmark
  public Value.Color[] renderWithValue() {
    var pixels = new Value.Color[WIDTH * HEIGHT];
    Value.generateBlackHole(pixels);
    return pixels;
  }

  @Benchmark
  public Value.Color[] renderWithValueFlatArray() {
    var pixels = (Value.Color[]) ValueClass.newNullRestrictedNonAtomicArray(Value.Color.class,
        WIDTH * HEIGHT, Value.Color.BLACK);
    Value.generateBlackHole(pixels);
    return pixels;
  }

  @Benchmark
  public Identity.Color[] renderWithIdentity() {
    var pixels = new Identity.Color[WIDTH * HEIGHT];
    Identity.generateBlackHole(pixels);
    return pixels;
  }
}
