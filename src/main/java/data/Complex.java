package data;

public value class Complex {
  private final double re;
  private final double im;

  public Complex(double re, double im) {
    this.re = re;
    this.im = im;
    super();
  }

  @Override
  public String toString() {
    return "Complex(" + re + ", " + im + ")";
  }
}