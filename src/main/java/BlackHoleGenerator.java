import module java.desktop;

import jdk.internal.value.ValueClass;

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
  Color add(Color o) { return new Color(r + o.r, g + o.g, b + o.b); }
  Color mul(double s) { return new Color(r * s, g * s, b * s); }
  Color clamp() {
    return new Color(Math.clamp(r, 0, 1), Math.clamp(g, 0, 1), Math.clamp(b, 0, 1));
  }
}

static final int WIDTH = 800;
static final int HEIGHT = 600;

static final double DT = 0.05;         // Ray step size
static final double GM = 1.5;          // Gravity strength
static final double RS = 1.0;          // Event horizon radius

static Color[] generateBlackHole() {
  var pixels = new Color[WIDTH * HEIGHT];
  //var pixels = (Color[]) ValueClass.newNullRestrictedNonAtomicArray(Color.class,
  //    WIDTH * HEIGHT, new Color(0, 0, 0));

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

      pixels[py * WIDTH + px] = color;
    }
  }
  return pixels;
}

static Color rayMarching(Vec3 pos, Vec3 vel) {
  var color = new Color(0, 0, 0);

  // Step the photon through the gravity field
  for (var step = 0; step < 400; step++) {
    var r = pos.mag();

    if (r < RS) {
      return new Color(0, 0, 0); // Photon fell into the event horizon (black)
    }
    if (r > 20.0) {
      return new Color(0, 0, 0); // Photon escaped to deep space
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

static void saveImage(Color[] pixels, Path path) throws IOException {
  var image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

  for (var y = 0; y < HEIGHT; y++) {
    for (var x = 0; x < WIDTH; x++) {
      // Ensure no RGB values exceed 1.0 before converting to 0-255
      var color = pixels[y * WIDTH + x].clamp();

      var r = (int) (color.r() * 255);
      var g = (int) (color.g() * 255);
      var b = (int) (color.b() * 255);

      var rgb = (r << 16) | (g << 8) | b;
      image.setRGB(x, y, rgb);
    }
  }

  ImageIO.write(image, "png", path.toFile());
}

static void main() throws IOException {
  IO.println("Calculating light paths...");
  var start = System.currentTimeMillis();
  var pixels = generateBlackHole();
  var end = System.currentTimeMillis();
  IO.println("generation time: " + (end - start) + " ms");

  saveImage(pixels, Path.of("blackhole.png"));
  IO.println("Saved as blackhole.png");
}