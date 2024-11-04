import data3.Complex;

import static java.util.stream.IntStream.rangeClosed;

//@jdk.internal.vm.annotation.NullRestricted
Complex complex = new Complex(0, 0);

void main() {
  var threads = rangeClosed(1, 2)
      .mapToObj(i -> Thread.ofPlatform().start(() -> {
        while (!Thread.interrupted()) {
          this.complex = new Complex(i, i);
        }
      }))
      .toList();

  for(;;) {
    var complex = this.complex;
    if (complex.re() != complex.im()) {
      threads.forEach(Thread::interrupt);
      throw new AssertionError("boom ! " + complex);
    }
  }
}
