import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;

// ---------- PerlinNoise: classic 2D Perlin noise ----------
static final class PerlinNoise {
  private final int[] perm = new int[512];

  PerlinNoise(long seed) {
    var p = new int[256];
    for (var i = 0; i < 256; i++) {
      p[i] = i;
    }
    var random = new Random(seed);
    for (var i = 255; i > 0; i--) {
      var j = random.nextInt(i + 1);
      var tmp = p[i];
      p[i] = p[j];
      p[j] = tmp;
    }
    for (var i = 0; i < 512; i++) {
      perm[i] = p[i & 255];
    }
  }

  private float fade(float t) {
    return t * t * t * (t * (t * 6 - 15) + 10);
  }

  private float lerp(float t, float a, float b) {
    return a + t * (b - a);
  }

  private float grad(int hash, float x, float y) {
    var direction = hash & 7;
    var primaryAxis = direction < 4 ? x : y;
    var secondaryAxis = direction < 4 ? y : x;
    return ((direction & 1) == 0 ? primaryAxis : -primaryAxis)
        + ((direction & 2) == 0 ? secondaryAxis : -secondaryAxis);
  }

  float noise(float x, float y) {
    var cellX = (int) Math.floor(x) & 255;
    var cellY = (int) Math.floor(y) & 255;
    var fracX = x - (float) Math.floor(x);
    var fracY = y - (float) Math.floor(y);
    var u = fade(fracX);
    var v = fade(fracY);
    var hashBottomLeft = perm[perm[cellX] + cellY];
    var hashTopLeft = perm[perm[cellX] + cellY + 1];
    var hashBottomRight = perm[perm[cellX + 1] + cellY];
    var hashTopRight = perm[perm[cellX + 1] + cellY + 1];
    return lerp(v,
        lerp(u, grad(hashBottomLeft, fracX, fracY), grad(hashBottomRight, fracX - 1, fracY)),
        lerp(u, grad(hashTopLeft, fracX, fracY - 1), grad(hashTopRight, fracX - 1, fracY - 1)));
  }
}

// ---------- Vector2: shared type for position, velocity, and force ----------
value record Vector2(float x, float y) {
  Vector2 add(Vector2 o) { return new Vector2(x + o.x, y + o.y); }
  Vector2 scale(float s) { return new Vector2(x * s, y * s); }
  float length() { return (float) Math.sqrt(x * x + y * y); }
  Vector2 normalize() {
    var len = length();
    return len == 0 ? new Vector2(0, 0) : new Vector2(x / len, y / len);
  }
  Vector2 wraparound(float width, float height) {
    var x = this.x;
    var y = this.y;
    if (x < 0) x += width;
    if (x > width) x -= width;
    if (y < 0) y += height;
    if (y > height) y -= height;
    return new Vector2(x, y);
  }
}

// ---------- FlowField: converts coordinates into a force vector ----------
value record FlowField(float scale, float strength) {
  private static final PerlinNoise NOISE = new PerlinNoise(42L);

  Vector2 forceAt(float x, float y) {
    var angle = NOISE.noise(x * scale, y * scale) * Math.PI * 4;
    return new Vector2((float) Math.cos(angle), (float) Math.sin(angle)).scale(strength);
  }
}

// ---------- Particle: position, velocity, force accumulator ----------
private static final float MASS = 1.0f;
private static final float MAX_SPEED = 4.0f;

value record Particle(Vector2/*!*/ position, Vector2/*!*/ velocity, Vector2/*!*/ force) {
  Particle update(Vector2 f, float width, float height) {
    var force = this.force.add(f);
    var acceleration = force.scale(1.0f / MASS);
    var velocity = this.velocity.add(acceleration);
    if (velocity.length() > MAX_SPEED) {
      velocity = velocity.normalize().scale(MAX_SPEED);
    }
    var position = this.position.add(velocity).wraparound(width, height);
    return new Particle(position, velocity, new Vector2(0, 0));
  }
}

void main() throws IOException {
  var width = 1600;
  var height = 1200;
  var numParticles = 2500;
  var steps = 400;
  var scale = 0.0025f;
  var strength = 0.6f;

  var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
  var g = image.createGraphics();
  g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
  g.setColor(new Color(8, 8, 16));
  g.fillRect(0, 0, width, height);
  g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));  // Set trail opacity

  var field = new FlowField(scale, strength);
  var random = new Random(1);
  var colors = new Color[numParticles];
  var particles = new Particle[numParticles];   // Array of non-null elements ?
  for (var i = 0; i < numParticles; i++) {
    var hue = (float) (random.nextDouble() * 0.15 + 0.5);
    colors[i] = Color.getHSBColor(hue, 0.7f, 1.0f);

    var pos = new Vector2((float) random.nextDouble() * width, (float) random.nextDouble() * height);
    particles[i] = new Particle(pos, new Vector2(0, 0), new Vector2(0, 0));
  }

  for (var step = 0; step < steps; step++) {
    for (var i = 0; i < particles.length; i++) {
      var particle = particles[i];
      var force = field.forceAt(particle.position.x, particle.position.y);
      var newParticle = particle.update(force, width, height);
      particles[i] = newParticle;

      var movedX = Math.abs(newParticle.position.x - particle.position.x);
      var movedY = Math.abs(newParticle.position.y - particle.position.y);
      if (movedX < width / 2.0 && movedY < height / 2.0) {  // did wraparound?
        g.setColor(colors[i]);
        g.drawLine(
            (int) particle.position.x, (int) particle.position.y,
            (int) newParticle.position.x, (int) newParticle.position.y);
      }
    }
  }

  g.dispose();
  ImageIO.write(image, "png", Path.of("flowfield.png").toFile());
  System.out.println("Saved flowfield.png (" + width + "x" + height + ")");
}
