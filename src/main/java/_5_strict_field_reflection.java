value class Complex {
  double re;
  double im;

  Complex(double re, double im) {
    this.re = re;
    this.im = im;
  }

  @Override
  public String toString() {
    return "Complex[re=" + re + ", " + "im=" + im + ']';
  }
}

void main() throws NoSuchFieldException, IllegalAccessException {
  var field = Complex.class.getDeclaredField("re");
  field.setAccessible(true);

  var complex = new Complex(1.0, 2.0);
  field.set(complex, 3.0);
  IO.println(complex);
}
