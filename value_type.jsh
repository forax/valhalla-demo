// To start, execute java -jar jvisualbook-*.jar on the command line
// jvisualbook is a notebook program that runs in the browser

// # Value Class + Null Restricted Types
// Remi Forax

// JCrete, July 2026

// ## Warning, I'm using a un-released JDK!

// This is experimental!

import module java.base;
IO.println(Runtime.version());


// ## OpenJDK Projects
// Modernizing Java

// - Language Improvements (Amber, Babylon)
// - Platform Improvements (Panama, Loom, Liliput, **Valhalla**, Leyden)


// #
// ![Java mascot in a north god clothing](images/duke-valhalla-small.png)


// ## Why Valhalla ?
// Started in August 2014 by Brian Goetz and John Rose

// - No cost abstraction?
// - Flat memory representation? (CPU friendly)
// - Primitives are a nuisance


// ## Flat memory representation?
// An array of Points in memory is **not** a flat array

// ![Heap representation of an array](images/data-in-memory.png)

// ## Roadmap to Valhalla
// Subject to change

// 🚚 JEP 513: Flexible Constructor Bodies (Java 25)

// 🏗️ JEP 401: Value Classes and Objects (Java 28 Preview)

// 🚧 JEP Draft: Null-Restricted Value Class Types (Java 3X Preview)

// ...


// ## What is a value class?
// Instances/references/objects are values not pointers

/*value*/ record Point(int x, int y) {}

Point p1 = new Point(1, 2);
Point p2 = new Point(1, 2);
IO.println(p1 == p2);

// A value class has **no identity**, no address in memory

// The operator == compares all the field values


// ## Value type objects are:
// - **identity-free**,
// - **unmodifiable** (all fields are final),
// - stored and passed **by value** rather than by pointer

// Goals:
// **No overhead** of heap allocation,
// **no pointer indirection** for small data structures


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
  Point(int x, int y) { this.x = x; this.y = y; }
}
abstract value class ColoredPoint extends Point {
  String color;
  ColoredPoint(int x, int y, String color) { this.color = color; super(x, y); }
}

// More on the `super()` call later


// ## And hashCode()?
// An **identity** class has a header, a **value** class has no header

/*value*/ class Pet {
  /*final*/ String kind;
  Pet(String kind) { this.kind = kind; }
}

var garfield = new Pet("cat");
var charly = new Pet("cat");
IO.println(Integer.toHexString(garfield.hashCode()));
IO.println(Integer.toHexString(charly.hashCode()));

// Uses the values of the fields to compute the 'default' hashCode()


// ## And Synchronized?
// An **identity** class has a header, a **value** class has no header

// A value class has no header

value record MyFloat(float f) {}
MyFloat myFloat = new MyFloat(3.14f);
synchronized (myFloat) { }

Object o = myFloat;
synchronized (o) { }


// ## Weak references do not work too!

// A weak reference is a reference that does not seen by the garbage collector

value record Cat(String name) { }
var cat = new Cat("charly");
var weakCat = new WeakReference<>(cat);


// ## Primitives vs Objects

// Two different effects
// - operations on stack (extra allocation, extra instructions)
// - memory layout on heap (extra indirection + header overhead)

// Can be solved separately !
// - VM optimization/deoptimization (scalarization on stack)
// - Object layout (flattening on heap)


// ## Valhalla problems checklist
// Problems to solve

// - [ ] Constructors modify the fields of `this`

// - [ ] Do user code has to be recompiled?

// - [ ] Java classes are loaded lazily (after fields and parameters are discovered)


// ## Strict initialization!
// All fields of a value class must be initialized **before** the call to `super()`

value class MyInteger {
  int value;
  MyInteger(int value) {
    super();
    IO.println(this.value);  // Oops
    this.value = value;
  }
}


// ## Strict initialization and Java 25

// Java 25 already supports strict initialization,
// To prepare the introduction of value types

// Useful even for identity class, avoid **leaky** `this`

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


// ## Mandelbrot set
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
// primitive    | avgt | 10  |  86,127 ± 2,460  ms/op
// record       | avgt | 10  | 207.828 ± 1.938  ms/op
// value record | avgt | 10  |  86,528 ± 1,689  ms/op
// ```


// ## GC usages
// Using JFR to measure allocations

// [JFR allocation using value type](images/jfr-alloc-value.png)

// [JFR allocation using identity type](images/jfr-alloc-identity.png)


// # Value class is a VM optimization

// Same bytecode when **using** an identity class or a value class?

// The JIT removes allocation/indirection when
// the bytecode is **transformed to machine code**


// ## Java compiler

// When compiling a value class.
// The compiler removes the ACC_IDENTITY modifier bit of the class file

// When compiling a class that uses a value class
// the compiler inserts an attribute **LoadableDescriptors**
// that list the classes that should be loaded

// The VM loads these classes early to check if they are value classes


// ## Valhalla solution checklist
// Problems solved!

// - [X] Constructors modify the fields of `this` **only before super()**

// - [X] **Same bytecode** for identity class and value class?

// - [X] Java classes are still loaded lazily, **an attribute ask for early loading**


// ## Existing JDK classes retrofitted as value classes
// All existing classes annotated with `@ValueBased` are now value classes

// All wrappers `java.lang.Boolean`, `java.lang.Integer`, etc

// `java.util.Optional`

// Most classes of `java.time`

IO.println(Boolean.class.isValue());


// ## Storing value instances in fields/arrays?
// Works but may not get the best performance

value record Person(int age/*, String name*/) {}
class Car {
  Person driver;
  int numberOfSeats;
}

// Reading/Storing the value instance `driver` in RAM may require **several read/writes**

// The VM spec mandates reference read/write to be "atomic"

// So only 64 bits value instances (`null` included) are flattened?


// ## Flattening on Heap

// ![Heap representation of a value class](images/value-in-memory.png)


// ## JEP 401: Value Classes and Objects (Preview)

// Mantra: Code like a class, Work like an int

// **Scalarization** is done by the JIT

// **Flattening** if size <= 64 bits (`null` included)

// **No** need to **recompile** the user code (not fully true)

// **Retrofit** `Integer`, `Optional`, `LocalDate`, etc to be value classes


// ## JEP 401 Pull Request to github.com/openjdk/jdk

// ![JEP 401 Pull Request](images/jep401-pull-request.png)


// # How to improve the heap flattening?


// ## How to improve the heap flattening?

// Add informations about:
// - _nullability_?
// - _atomicity_?

// Note: _non-atomic_ implies _null-restricted_


// ## Idea: Null-restricted types
// Let's help flattening by adding nullability markers

// '!' or '?' sigils at the end of a type

// '!' for strictness/certainty, and '?' for uncertainty

// `Boolean!`, `Optional!`, `Complex?`, etc.


// ## Bang '!' as a contract
// Extends Java to add '!' at the end of a type of a field

value record Person(String name, int age) {}
class Car {
  Person! driver;
}


// ## Fields with '!' has to be initialized before super()
// A null-restricted field can **not be set** to 'null'

value record Person(int age, String name) {}
class Car {
  Person! driver;
  Car(Person driver) {
    this.driver = driver;   // the VM can throw a NPE
    super();
  }
}

//new Car(null);


// ## Flattening on Heap (with '!')

// ![Heap representation with bang](images/value-null-restricted-in-memory.png)


// ## Final and '!'
// Are fully flattened!

value record Complex(double re, double im) {}
class Maybe {
  final Boolean! flag;
  final Complex! value;
  Maybe(Boolean flag, Complex value) {
    this.flag = flag;
    this.value = value;
    super();
  }
}

// No concurrency issues (write once first, read many)

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


// ## '!' also works on identity classes
// This exactly the same semantics as for value classes

class User { String! name; User(String name) { this.name = name; super(); }}
var user = new User(null);

void m(String! s) {}
m(null);

// ## Creating an array with '!'
// The array elements **can not be initialized** to `null`

// Without initial elements
var array = new Person![4];

// **Prototype API**, not final syntax
var proto = new Person[4];
Arrays.setAll(proto, _ -> new Person("Bob", 42));
var array = (Person[]) Array.newInstance(Person.class, 0x0200, 4, proto, 0);

// `0x0200` means null-restricted


// ## Using an array with '!'
// As with fields, the VM checks at runtime

var proto = new Person[4];
Arrays.setAll(proto, _ -> new Person("Bob", 42));
var array = (Person[]) Array.newInstance(Person.class, 0x0200, 4, proto, 0);

//array[1] = null;

Object[] objectArray = array;
//objectArray[1] = null;


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


// ## Method selection and '!'
// We want to be backward compatible, so '!' can not be used in method selection

class A { void m(Object o) {} }
class B extends A { void m(Complex! c) {} }

B b = new B();
b.m(null);


// # Is declaring '!' at type level a good idea?

// null-restricted or non-atomic looks more like storage keywords
// than markers on types

// Those keywords are implementation decisions, not something the user should control

// So no `ArrayList<Person!>` and more a `FlattenList<Person>`

// # TLDR;  Code like a class, Work like an int
// OpenJDK project Valhalla:

// value type instances are **scalarized** on stack (by the JIT)

// Value type fields/array elements are **maybe flattened** on heap

// To enhance flattening: recompile user code + add keywords or markers on types?


// ## Roadmap to Valhalla
// Subject to change

// 🚚 JEP 513: Flexible Constructor Bodies

// 🏗️ JEP 401: Value Classes and Objects (Preview)

// 🚧 JEP Draft: Null-Restricted Value Class Types (Preview)

// ☁️ JEP 402: Enhanced Primitive Boxing (int ≈ Integer!)

// 🚧 Type Classes (operator overloading)

// ☁️ Parametric JVM (List<ValueType>)
