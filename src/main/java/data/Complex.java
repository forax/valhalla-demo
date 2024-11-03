package data;

public /*value*/ class Complex {
  private final double re;
  private final double im;

  public Complex(double re, double im) {
    super();
    this.re = re;
    this.im = im;
  }

  @Override
  public String toString() {
    return "Complex(" + re + ", " + im + ")";
  }
}