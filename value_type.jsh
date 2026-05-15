// To start, execute java -jar jvisualbook-*.jar on the command line
// jvisualbook is a notebook program that runs in the browser

// # Value Class Emotions
// Remi Forax

// Université Gustave Eiffel, May 2026

// ## Warning, I'm not using a released JDK

// This is experimental!

import module java.base;
IO.println(Runtime.version());

// ## What is a value class?
// Instances/references/objects are values not pointers

value record Point(int x, int y) {}

Point p1 = new Point(1, 2);
Point p2 = new Point(1, 2);
IO.println(p1 == p2);

// A value class has **no identity**, no address in memory

// The operator == compares all the field values

// #
// ![Java mascot in a north god clothing](images/duke-valhalla-white.png)

// ## Why Valhalla ?
// Started in August 2014 by Brian Goetz and John Rose

// - No cost abstraction ?
// - Flat representation (CPU friendly)
// - Primitives are a nuisance

// ## Value type objects are:
// - **identity-free**,
// - **unmodifiable**,
// - stored and passed **by value** rather than by pointer

// **No overhead** of heap allocation and pointer indirection
// for small data structures

// ## Is it like a struct in C?
// No, a value class is unmodifiable!

// A value class is **implicitly `final`**

// All fields are **implicitly `final`**

value class MyInteger {
  int x;
}

// ## Are value instances objects?
// Yes !

// It can also implement interfaces and extend an abstract class

value record Person(String name) implements Comparable<Person> {
  @Override
  public int compareTo(Person o) {
    return name.compareTo(o.name);
  }
}

var person = new Person("Bob the welder");
Object object = person;   // the VM may box the value

// ## Synchronized and other methods that need a header?
// An **identity** class has a header

// A value class has no header

value record MyFloat(float f) {}
var myFloat = new MyFloat(3.14f);
synchronized (myFloat) { }

Object o = myFloat;
synchronized (o) { }

// ## Several problems to solve
// This is not that hard...

// 1) `this` in a constructor is modifiable

// 2) Same bytecode for identity class and value class

// 3) Java classes are loaded lazily (very late)
//    - after the types of fields are discovered
//    - after the types of parameters are discovered

// ## Strict initialization is required!
// All fields of a value class must be initialized **before** the call to `super()`

value class MyInteger {
  int value;
  MyInteger(int value) {
    super();
    this.value = value;
  }
  int value() { return value; }
}

// ## Strict initialization available in Java 25!
// To prepare the introduction of value types

// Java 25 supports strict initialization,
// avoiding **leaky** `this`

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

// # Mandelbrot set
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
// 1024 x 1024 — iterations max : 255

// ```text
// Benchmark    | Mode | Cnt | Score     Error  Units
// -------------|------|-----|----------------------
// primitive    | avgt | 10  |  86,127 ± 2,460  ms/op
// record       | avgt | 10  | 207.828 ± 1.938  ms/op
// value record | avgt | 10  |  86,528 ± 1,689  ms/op
// ```

// ## GC usages
// Using JFR to measure allocations

// [JFR allocation using value type](images/jfr-alloc-value.png)

// [JFR allocation using identity type](images/jfr-alloc-identity.png)


// # Value class is a VM optimization

// The Java compiler only removes the ACC_IDENTITY bit in the class file

// The JIT removes allocation/indirection when
// the bytecode is **transformed to machine code**

// ## Existing JDK classes retrofitted as value classes
// All existing classes annotated with `@ValueBased` are now value classes

// All wrappers `java.lang.Boolean`, `java.lang.Integer`, etc

// `java.util.Optional`

// Most classes of `java.time`

IO.println(Boolean.class.isValue());

// ## Storing value instances in fields/arrays?
// Works but may not get the best performance

value record Person(String name, int age) {}
class Car {
  Person driver;
  int numberOfSeats;
}

// Reading/Storing the value instance `driver` in RAM may require **several read/writes**

// The VM spec mandates reference read/write to be "atomic"

// So only 64 bits value instances (`null` included) are flattened?

// ## JEP 401: Value Classes and Objects (Preview)

// Mantra: Code like a class, Work like an int

// **Scalarization** is done by the JIT

// **Flattening** if size <= 64 bits (`null` included)

// **No** need to **recompile** the user code (not fully true)

// **Retrofit** `Integer`, `Optional`, `LocalDate`, etc to be value classes

// # Value Class Emotions
// Let's try to improve the heap flattening

// ## Idea of the Value Class Emotions

// Let's help flattening by adding type emotions

// '!' or '?' sigils at the end of a type to indicate **nullability**

// `Boolean!`, `Optional!`, `Complex?`, etc.

// ## The emotion bang '!'
// Extends Java to add '!' at the end of a type of a field

value record Person(String name, int age) {}
class Car {
  Person! driver;
}

// ## Fields with '!' has to be initialized before super()
// A null-restricted field can **not be set** to 'null'

value record Person(String name, int age) {}
class Car {
  Person! driver;
  Car(Person driver) {
    this.driver = driver;   // the VM can throw a NPE
    super();
  }
}

//new Car(null);

// ## Why not using '?' instead of '!'?
// Like in Kotlin?

String s = null;   // Invalid in Kotlin, valid in Java

// Adding '?' requires to change the semantics of Java

// This is **not a backward compatible** change

// ## Method parameters with '!'
// Equivalent to `Objects.requireNonNull()` on the parameter

value record Person(String name, int age) {}
class Car {
  // Person! driver;
  Car(Person! driver) {
    // this.driver = driver;
    super();
  }
}

new Car(null);

// ## Creating an array with '!'
// The array elements **can not be initialized** to `null`

// Without initial elements
var array = new Person![4];

// Use an existing array as prototype
var proto = new Person[4];
Arrays.setAll(proto, _ -> new Person("Bob", 42));
var array = (Person[]) Array.newInstance(Person.class, 0x0200, 4, proto, 0);

// ## Using an array with '!'
// As with fields, the VM checks at runtime

var proto = new Person[4];
Arrays.setAll(proto, _ -> new Person("Bob", 42));
var array = (Person[]) Array.newInstance(Person.class, 0x0200, 4, proto, 0);

array[1] = null;

Object[] objectArray = array;
objectArray[1] = null;

// ## Using collections with '!'
// Sadly, it does not work 😢

// Generics are erased at compile time,
// the type arguments are not available for the VM at runtime

var list = new ArrayList<Complex!>();
//list.add(null);

// We need a parametrized VM. We are working on it!

// ## Inside a method
// You can declare a local variable with '!' or use it in a cast

// In both cases, the compiler insert a check at compile time

Person f() { return null; }
void m() {
  Person! p = f();
  //var p2 = (Person!) f();
}

m();

// Allowing '!' for local variables is still in discussion

// ## '!' also works on identity classes
// This exactly the same semantics as for value classes

class User { String! name; User(String name) { this.name = name; super(); }}
var user = new User(null);

void m(String! s) {}
m(null);

void f(String s) { String! s2 = s; }
f(null);

// ## Method selection and '!'
// We want to be backward compatible, so '!' can not be used in method selection

class A { void m(Object o) {} }
class B extends A { void m(Complex! c) {} }

B b = new B();
b.m(null);

// ## Flattening with bangs

value record Complex(double re, double im) {}
class Maybe {
  Boolean! flag;
  final Complex! value;
  Maybe(Boolean! flag, Complex! value) {
    this.flag = flag;
    this.value = value;
    super();
  }
}

// `flag` is flatten (sizeof <= 64 bits),
// `value` is flatten (`final` bang)

// # TLDR;

// ## Code like a class, Work like an int

// value type instances are **scalarized** on stack

// Value type fields/array elements are **flattened** on heap
// - if sizeof <= 56 bits (need 1 byte for `null`)
// - if field is `!` and sizeof <= 64 bits
// - if field is `!` and `final`

// Maybe **`non-null`** instead of `!`
// ```java
// record Line(non-null Complex c1, non-null Complex c2) { }
// ```

// ## Roadmap to Valhalla
// Subject to change

// 🚚 JEP 513: Flexible Constructor Bodies

// 🏗️ JEP 401: Value Classes and Objects (Preview)

// 🚧 JEP Draft: Null-Restricted Value Class Types (Preview)

// ☁️ JEP 402: Enhanced Primitive Boxing (int ≈ Integer!)

// 🚧 Type Classes (operator overloading)

// ☁️ Parametric JVM (List<ValueType>)

// # Supplementary slides

// ## toString() and hashCode() works!

value class Pet {
  String kind;
  public Pet(String kind) { this.kind = kind; }
}

var garfield = new Pet("cat");
var charly = new Pet("cat");
IO.println(garfield);
IO.println(Integer.toHexString(charly.hashCode()));

// Uses the values of the fields to compute the 'default' hashCode()

// ## Weak references do not work!

// A weak reference is a reference that does not prevent the object from being garbage collected,
// i.e. a reference not seen by the GC

value record Cat(String name) { }
var cat = new Cat("charly");
var weakCat = new WeakReference<>(cat);
