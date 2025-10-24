package data3;

@jdk.internal.vm.annotation.LooselyConsistentValue
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
    return "data3.Complex(" + re + ", " + im + ")";
  }

  public double re() {
    return re;
  }

  public double im() {
    return im;
  }
}