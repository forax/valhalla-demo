import data.Complex;

void main() {
  var c1 = new Complex(12, 24);
  var c2 = new Complex(12, 24);
  System.out.println(c1);

  System.out.println("isValue " + c1.getClass().isValue());
  System.out.println("isIdentity " + c1.getClass().isIdentity());

  System.out.println(c1 == c2);

  System.out.println(System.identityHashCode(c1));
  System.out.println(System.identityHashCode(c2));

  synchronized ((Object) c1) {}

  new java.lang.ref.WeakReference<>(c1);
}
