value record Complex(double re, double im) { }

void main() throws NoSuchFieldException, IllegalAccessException {
  var field = Complex.class.getDeclaredField("re");
  field.setAccessible(true);

  var complex = new Complex(1.0, 2.0);
  field.set(complex, 3.0);
  IO.println(complex);
}
