import data.Complex;

void main() throws NoSuchFieldException, IllegalAccessException {
  var field = Complex.class.getDeclaredField("re");
  field.setAccessible(true);

  var complex = new Complex(1.0, 2.0);
  field.set(complex, 3.0);
  println(complex);
}
