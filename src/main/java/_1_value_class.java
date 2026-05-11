static final value class Complex {
  private final double re;
  private final double im;

  Complex(double re, double im) {
    this.re = re;
    this.im = im;
  }

  @Override
  public String toString() {
    return "Complex[" +
        "re=" + re + ", " +
        "im=" + im + ']';
  }
}

void main() {
  var c1 = new Complex(12, 24);
  var c2 = new Complex(12, 24);
  IO.println(c1);

  IO.println("isValue " + c1.getClass().isValue());
  IO.println("isIdentity " + c1.getClass().isIdentity());

  IO.println(c1 == c2);

  IO.println(System.identityHashCode(c1));
  IO.println(System.identityHashCode(c2));

  //synchronized ((Object) c1) {}

  //new java.lang.ref.WeakReference<>(c1);
}
