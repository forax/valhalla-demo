// To start, execute java -jar jvisualbook-*.jar on the command line
// jvisualbook is a notebook program that runs in the browser

// # Value Classes in Java 28 Preview
// Remi Forax

// ParisJUG, September 2026


// ## Warning, I'm using a Java 28 preview build

// This JDK has not been released yet!

import module java.base;
IO.println(Runtime.version());

// Try it yourself at [jdk.java.net/28](https://jdk.java.net/28/)


// ## OpenJDK Projects
// Modernizing Java

// - Language Improvements (Amber, Babylon)
// - Platform Improvements (Panama, Loom, Lilliput, **Valhalla**, Leyden)


// #
// ![Java mascot in a norse god clothing](images/duke-valhalla-small.png)


// ## Why Valhalla ?
// Started in July 2014 by Brian Goetz and John Rose

// - Zero-cost abstraction?
// - Flat memory representation? (CPU friendly)
// - Primitives are a nuisance!

// ## Zero-cost abstraction

// We should not choose between abstraction and performance!

class DayInMonth {
  private final int day;
  public DayInMonth(int day) {
    if (day < 1 || day > 31) {
      throw new IllegalArgumentException("invalid day " + day);
    }
    this.day = day;
  }
}

// same for `BankId`, `Complex` or even immutable builders


// ## Flat memory representation?
// An array of Points in memory is **not** a flat array

// ![Heap representation of an array](images/data-in-memory.png)


// ## JEP 401 Pull Request to github.com/openjdk/jdk

// ![JEP 401 Pull Request](images/jep401-pull-request.png)


// ## JEP 401: What is a value class?
// Instances/references/objects are values not pointers

/*value*/ record Point(int x, int y) {}

Point p1 = new Point(1, 2);
Point p2 = new Point(1, 2);
IO.println(p1 == p2);

// A value class has **no identity**, no address in memory

// The operator == compares all the field values (using ==)


// ## A value instance is passed "by value"

static Point translate(Point p, int dx, int dy) {
  return new Point(p.x() + dx, p.y() + dy);
}

// At runtime, in pseudo Java
// ```java
// static (int, int) translate((int x, int y), int dx, int dy) {
//   return (x + dx, y + dy);
// }
// ```

// May eliminate allocation, value components are usually in registers (scalarization)


// ## Value objects are:
// - **identity-free** (no address, no header),
// - **unmodifiable** (all fields are final, class is final),
// - stored and passed **by value** rather than by pointer

// Goals:
// **no pointer indirection** for small data structures,
// **No overhead** for heap allocation


// ## Synchronized vs value class?
// An identity class has a header, a value class has **no header**

value record MyFloat(float f) {}
MyFloat myFloat = new MyFloat(3.14f);
synchronized (myFloat) { }

Object o = myFloat;
synchronized (o) { }


// ## And hashCode()?
// An identity class has a header, a value class has **no header**

/*value*/ class Pet {
  String kind;
  Pet(String kind) { this.kind = kind; }
}

var garfield = new Pet("cat");
var charly = new Pet("cat");
IO.println(Integer.toHexString(garfield.hashCode()));
IO.println(Integer.toHexString(charly.hashCode()));

// A value class uses the values of the fields to compute the 'default' hashCode()


// ## Weak references do not work either!

// A weak reference is a reference not followed by the garbage collector

value record Cat(String name) { }
var cat = new Cat("charly");
var weakCat = new WeakReference<>(cat);


// ## Value classes require Strict Initialization!
// A not fully initialized instance should not be observable

 value class MyInteger {
   int value;
   MyInteger(int value) {
     super();  // Oops
     IO.println(this.value);
     this.value = value;
   }
 }

// All fields of a value class must be initialized **before** the call to `super()`


// ## Strict initialization and Java 25

// Java 25 already supports strict initialization,
// to prepare the introduction of value classes

// Useful even for identity class, to avoid leaking an uninitialized `this`

class Person {
  String name;            // final or not
  Person(String name) {
    Objects.requireNonNull(name);
    this.name = name;
    super();
  }
  public String toString() { return name; }
}
new Person("John")


// ## Are value instances objects?
// Yes !

// It can also implement interfaces

value record Person(String name) implements Comparable<Person> {
  @Override
  public int compareTo(Person o) {
    return name.compareTo(o.name);
  }
}

Person person = new Person("Bob the welder");
Object object = person;   // the VM may box the value


// ## Or extend an abstract class
// "value" on an abstract class means "value enable"

abstract /*value*/ class Point {
  int x, y;
  Point(int x, int y) { this.x = x; this.y = y; super(); }
}
abstract value class ColoredPoint extends Point {
  String color;
  ColoredPoint(int x, int y, String color) { this.color = color; super(x, y); }
}


// ## Storing value instances in fields/arrays?
// Works but may not get the best performance

value record Person(int age/*, String name*/) {}
class Car {
  Person driver;
  int numberOfSeats;
}

// Reading/Storing the value instance `driver` in RAM may require **several read/writes**

// The VM spec mandates reference read/write to be "atomic"

// So only payload with size <= 56 bits (size <= 64 bits nullable) are flattened


// ## Flattening on Heap (payload size <= 56 bits)

// ![Heap representation of a value class](images/value-in-memory.png)


// ## Field flattening kind
// Currently Hotspot supports 4 kinds of field/array flat layout

// ```text
// |            | null_marker            | null_free        |
// -------------|------------------------|-------------------
// | atomic     | 56 bits                | 64 bits*         |
// | non_atomic | must be strict final*  | no restriction*  |
// ```

// (*) Not yet fully implemented

// [https://github.com/openjdk/jdk/blob/master/src/hotspot/share/oops/layoutKind.hpp#L32]


// ## Strict final field
// A value instance inside a value class is flattened

value record ProductId(long id) {}
value record Product(ProductId id, String name) {}

// At runtime

new Product(new ProductId(5), "banana")   // pointer to a 96/128 bits payload

// Not yet fully implemented!


// ## How to NOT get scalarization/flattening ?

// No Scalarization

sealed interface Vehicle permits Bus, Truck {}
value record Bus(int passengers) {}
value record Truck(int packets) {}
void m(Vehicle vehicle /* boxed */) { }

// No Flattening

class Car {
  Object driver;  // boxed
  Car(Person driver) {
    this.driver = driver;
  }
}


// ## Example: Mandelbrot set
// A two-dimensional set defined in the complex plane

// ![Mandelbrot image](images/mandelbrot.png)


// ## Mandelbrot set
// The set of complex numbers c for which the sequence defined by the iteration:

// z₀ = 0 or zₙ₊₁ = zₙ² + c

// remains bounded (never escapes to infinity)

value record Complex(double re, double im) {
  // add(), square() ...
}
static int iterate(Complex c) {
  var z = new Complex(0, 0);
  for (var i = 0; i < MAX_ITER; i++) {
    if (z.absSquared() > 4.0) return i;  // escaped
    z = z.square().add(c);
  }
  return MAX_ITER;
}


// ## Version using primitives
// This version is less readable

static int iterate(double cx, double cy) {
  var zx = 0.0;
  var zy = 0.0;
  for (var i = 0; i < MAX_ITER; i++) {
    var zx2 = zx * zx;
    var zy2 = zy * zy;
    if (zx2 + zy2 > 4.0) return i;   // escaped
    zy = 2 * zx * zy + cy;
    zx = zx2 - zy2 + cx;
  }
  return MAX_ITER;
}


// ## Benchmarks
// 1024 x 1024 — iterations max : 256

// ```text
// Benchmark    | Mode | Cnt | Score     Error  Units
// -------------|------|-----|----------------------
// primitive    | avgt | 10  |  86.127 ± 2,460  ms/op
// record       | avgt | 10  | 207.828 ± 1.938  ms/op
// value record | avgt | 10  |  86.528 ± 1,689  ms/op
// ```


// ## GC usages
// Using JFR to measure allocations

// [JFR allocation using value class](images/jfr-alloc-value.png)

// [JFR allocation using identity class](images/jfr-alloc-identity.png)


// # Value class is a VM optimization

// Same bytecode when **using** an identity class or a value class?

// The JIT removes allocation/indirection when
// the bytecode is **transformed to machine code**


// ## Java compiler and VM (JEP 539)

// When compiling a value class.
// - The compiler removes the `ACC_IDENTITY` modifier bit of the class file
// - The compiler adds `ACC_STRICT_INIT` on all fields

// When compiling a class that uses a value class
// the compiler inserts an attribute **LoadableDescriptors**
// that list the classes that should be pre-loaded

// The VM loads these classes early to check if they are value classes


// ## Existing JDK classes retrofitted as value classes
// Most existing classes annotated with `@ValueBased` are now value classes

// Classes like `java.util.Optional` and most classes of `java.time`

// All wrappers `java.lang.Boolean`, `java.lang.Integer`, etc

var a = (Integer) 1024;
var b = (Integer) 1024;
IO.println(a == b);
IO.println(Integer.class.isValue());

// This may break some existing code, `@ValueBased` contract was not enforced before


// ## JEP available in Java 28
// ` `

// 🚚 JEP 513: Flexible Constructor Bodies (Java 25)

// 🏗️ JEP 401: Value Objects (Java 28 Preview)

// 🏗️ JEP 539: Strict Field Initialization in the JVM (Java 28 Preview)


// ## Value classes in Java 28 (Preview)

// Mantra: Code like a class, Work like an int

// **Scalarization** done by the JIT

// **Flattening** if payload size <= 56 bits

// **No** need to **recompile** the user code (not fully true)

// **Retrofit** `Integer`, `Optional`, `LocalDate`, etc to be value classes
