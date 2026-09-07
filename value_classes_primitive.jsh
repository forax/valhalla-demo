// To start, execute java -jar jvisualbook-*.jar on the command line
// jvisualbook is a notebook program that runs in the browser

// # Beyond Java 28 — Design exploration
// Remi Forax

// ParisJUG, September 2026


// ## Warning, I'm using my own build

// This is experimental

import module java.base;
IO.println(Runtime.version());


// ## Value classes in Java 28 (Preview)

// Mantra: Code like a class, Work like an int

// **Scalarization** is done by the JIT

// **Flattening** if payload size <= 56 bits

// **No** need to **recompile** the user code (not fully true)

// **Retrofit** `Integer`, `Optional`, `LocalDate`, etc to be value classes


// # ... and beyond?
// ` `

// ... We are still in exploratory mode


// ## Challenges

// - How to improve the heap flattening?
// - How to create user defined primitives?


// ## Field flattening kind
// Currently Hotspot supports 4 kinds of field/array flat layout

// ```text
// |            | null_marker            | null_free        |
// -------------|------------------------|-------------------
// | atomic     | 56 bits                | 64 bits*         |
// | non_atomic | must be strict final*  | no restriction*  |
// ```

// (*) Not yet fully implemented


// ## How to improve the heap flattening?

// Add information about:
// - _nullability_?
// - _atomicity_?

// Note: read/write _non-atomic_ implies _null-restricted_


// ## Exploration: Null-restricted types
// Let's help flattening by adding nullability markers

// '!' or '?' sigils at the end of a type

// '!' for strictness/certainty, and '?' for uncertainty

// `Boolean!`, `Optional!`, `Complex?`, etc.


// ## Bang '!' as a contract
// Extends Java to add '!' at the end of a type of a field

value record Euro(long amount) {}
class Car {
  Euro! price;
}


// ## Fields with '!' have to be initialized before super()
// A null-restricted field can **not be set** to 'null'

value record Euro(long amount) {}
class Car {
  Euro! price;
  Car(Euro price) {
    this.price = price;   // the VM can throw a NPE
    super();
  }
}

//new Car(null);


// ## Flattening on Heap (with '!')

// ![Heap representation with bang](images/value-null-restricted-in-memory.png)


// ## Why not using '?' instead of '!'?
// Like in Kotlin?

String s = null;   // Invalid in Kotlin, valid in Java

// Adding '?' requires to change the semantics of Java

// This is **not a backward compatible** change


// ## Using '!' on parameters and with identity objects

class Car {
  // String! driver;
  Car(String! driver) {
    // this.driver = driver;
    super();
  }
}

new Car(null);

// Equivalent to `Objects.requireNonNull()` on the parameter


// ## Creating an array with '!'
// The array elements **can not be initialized** to `null`

// Without initial elements
var array = new Euro![4];

// Special syntax?
// ```java
// new Complex![] (index -> new Complex(index, index))
// ```


// ## Using a **Prototype API**

static final int NULL_RESTRICTED = 0x0200;

var proto = new Euro[4];
Arrays.setAll(proto, _ -> new Euro(200));
var array = (Euro[]) Array.newInstance(Euro.class, NULL_RESTRICTED, 4, proto, 0);

//array[1] = null;

Object[] objectArray = array;
//objectArray[1] = null;


// ## Using collections with '!'
// Sadly, it does not work 😢

// Generics are erased at compile time,
// the type arguments are not available for the VM at runtime

var list = new ArrayList<Euro!>();
//list.add(null);


// We need a parametrized VM. We are working on it!


// ## Method selection and '!'
// We want to be backward compatible, so '!' can not be used in method selection

class A { void m(Object o) {} }
class B extends A { void m(Euro! c) {} }

B b = new B();
b.m(null);


// # Is declaring '!' at type level a good idea?

// null-restricted or non-atomic looks more like storage keywords
// than markers on types

// Those keywords are **implementation decisions**, not something the user should control


// ## Exploration: Primitive classes
// `primitive` implies `value`

/*primitive*/ record Complex(double re, double im) {}

class Holder {
  Complex complex;  // strict non-null
  Holder() { }      // complex is initialized with Complex(0, 0)
}

// - Non-null on heap like primitives
// - Non-atomic on heap like primitives (full flattening)
// - Have a default value like primitives (all fields at zero)


// ## `null` primitive instances are allowed on stack
// Backward compatible so `Map.get()` can return `null`

class Holder {
  Complex c;   // strict non-null
  Holder(Complex c) {
    Objects.requireNonNull(c);  // good practice
    this.c = c;
  }
  void f(Complex c) {  // nullable
    // Complex c2 = Complex.default;  // can ask the default value
  }
}

// Not symmetric on stack and on heap:
// like fields are initialized to the default value but not locals


// ## And add a keyword `non-null` for value classes
// to avoid a cliff between primitive classes and value classes

value record Point(int x, int y) {}
class Holder {
  /*non-null*/ Point p;  // null-check at runtime
  Holder(Point p) {
    Objects.requireNonNull(p);   // good practice
    this.p = p;  // must be strictly initialized
    super();
  }
}

// Enable 64 bits flattening (more if coupled with strict final)

// No keyword `non-atomic` given it breaks encapsulation


// ## May allow `non-null` on record components?
// Will be transferred to fields

value record Point(int x, int y) {}
record Holder(/*non-null*/ Point p) {
  public Holder {
    Objects.requireNonNull(p);   // good practice
  }
}


// ## In summary
// ` `

// ```text
// | Feature                    | Status                |
// |----------------------------|-----------------------|
// | `value` class              | Java 28 Preview       |
// | `!` null-restricted syntax | Failed experiment     |
// | `primitive` class          | Future proposal       |
// | `non-null`                 | Future proposal       |
// | parametric JVM             | Future direction      |
// ```


// ## Roadmap to Valhalla
// Subject to change

// 🚚 JEP 513: Flexible Constructor Bodies

// 🏗️ JEP 401: Value Objects (Java 28 Preview)

// 🏗️ JEP 539: Strict Field Initialization in the JVM (Java 28 Preview)

// 🚧 JEP Draft: Primitive Class?

// ☁️ JEP 402: Enhanced Primitive Boxing (int ≈ Integer!)

// 🚧 Type Classes (operator overloading for primitive class)

// ☁️ Parametric JVM (List<ValueType>)
