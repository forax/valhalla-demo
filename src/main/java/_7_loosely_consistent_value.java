import static java.util.stream.IntStream.rangeClosed;

@jdk.internal.vm.annotation.LooselyConsistentValue
value record Complex(double re, double im) { }

static final class ComplexHolder {
  @jdk.internal.vm.annotation.NullRestricted
  Complex complex;

  ComplexHolder() {
    this.complex = new Complex(0, 0);
    super();
  }
}

void main() {
  var holder = new ComplexHolder();
  var threads = rangeClosed(1, 2)
      .mapToObj(i -> Thread.ofPlatform().start(() -> {
        while (!Thread.interrupted()) {
          holder.complex = new Complex(i, i);
        }
      }))
      .toList();

  for(;;) {
    var complex = holder.complex;
    if (complex.re() != complex.im()) {
      threads.forEach(Thread::interrupt);
      throw new AssertionError("boom ! " + complex);
    }
  }
}
