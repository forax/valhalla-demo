import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

value record Complex(double re, double im) {
  Complex add(Complex o) {
    return new Complex(re + o.re, im + o.im);
  }

  Complex square() {
    return new Complex(re * re - im * im, 2 * re * im);
  }

  double absSquared() {
    return re * re + im * im;
  }
}

static final int WIDTH = 800;
static final int HEIGHT = 800;
static final int MAX_ITER = 256;

static final double X_MIN = -2.5, X_MAX = 1.0;
static final double Y_MIN = -1.5, Y_MAX = 1.5;

static int iterate(Complex c) {
  Complex z = new Complex(0, 0);
  for (int i = 0; i < MAX_ITER; i++) {
    if (z.absSquared() > 4.0) return i;  // escaped
    z = z.square().add(c);
  }
  return MAX_ITER; // inside the set
}

static int colorFor(int iter) {
  if (iter == MAX_ITER) return Color.BLACK.getRGB();
  // Smooth gradient: map [0, MAX_ITER) to a hue
  float hue = (float) iter / MAX_ITER;
  return Color.HSBtoRGB(hue, 0.8f, 1.0f);
}

static void render(BufferedImage image) {
  for (int px = 0; px < WIDTH; px++) {
    for (int py = 0; py < HEIGHT; py++) {
      // Map pixel → complex plane
      double cx = X_MIN + (X_MAX - X_MIN) * px / (WIDTH - 1);
      double cy = Y_MIN + (Y_MAX - Y_MIN) * py / (HEIGHT - 1);

      image.setRGB(px, py, colorFor(iterate(new Complex(cx, cy))));
    }
  }
}

void main() {
  var frame = new JFrame("Mandelbrot Set");
  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

  var image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
  render(image);
  var icon = new ImageIcon(image);

  var panel = new JPanel();
  var label = new JLabel(icon);
  panel.add(label);
  panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));

  frame.add(panel);
  frame.pack();
  frame.setLocationRelativeTo(null);
  frame.setVisible(true);
}
