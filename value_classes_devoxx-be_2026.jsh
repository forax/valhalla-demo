// To start, execute java -jar jvisualbook-*.jar on the command line
// jvisualbook is a notebook program that runs in the browser

// # Will Valhalla Fix Tony’s Billion $ Mistake?
// Remi Forax & José Paumard

// Devoxx October 2026


// ## I'm using Java 28 preview

// This is a preview

import module java.base;
IO.println(Runtime.version());


// ## Tony Hoare’s 1B$ mistake

// Null reference violations have
// cost in term of programmer
// time, user discomfort and
// general damage in the order of
// a billion $.

// Tony Hoare,
// QCon London, 2009


// ## OpenJDK.org

// Java is open source all developments are hosted by [https://openjdk.org](openjdk.org)

// Source avaialble on github
// [https://github.com/openjdk/jdk](github.com/openjdk/jdk)

// Early access builds available at
// [https://jdk.java.net/jdk28](jdk.java.net/jdk28)


// ## JEP 401 Pull Request to github.com/openjdk/jdk

// ![JEP 401 Pull Request](images/jep401-pull-request.png)


// ## The Valhalla project

// OpenJDK project lead by Brian Goetz

// John Rose, Dan Smith, Dan Hedinga, etc

// Started on July, 2014!


// ## Primitive vs. Object in Java

// Strong separation between primitives and Objects

// Java code uses primitive directly (CPU and GPU friendly)
// and uses Object through references

// Primitive are not part of the OOP world
// (no encapsulation, no abstraction)


// ## Valhalla

// Bring the benefits of primitives to objects and classes
// - Enable flatter and denser memory layouts
// - Enable adding new primitive-like classes
// - Compatible migration of existing value-based classes

// Also (long term)
// - Healing the reference-primitive divide
// - Enable runtime specialization of generics


// ## Value Objects
// From the Openjdk project page:

// Value objects are immutable objects that lack **identity**

// Developers can save memory and improve performance by using value objects for immutable data


// ## Valhalla

// What does it mean to lack identity?

// - What is object identity?
//   - How does it relate to mutability?
// - How can you save memory with value objects?
// - How can you improve performance?


// # What is Object Identity and How Does it Relate to Mutability?


// ## What is Object Identity?
// The identity of an object is its address in memory

record Song(String title, int duration) {}
var beatIt = new Song("Beat it", 4 * 60 + 58);
var thriller = new Song("Thriller", 3 * 60 + 22);
var billieJean = new Song("Billie Jean", 4 * 60 + 56);


// ## What is Object Identity?
// There can only be one copy of any object in memory

class PlayList extends AbstractList<Song> {
  String title;
  Song[] songs;
  PlayList(String title, Song... songs) { this.title = title; this.songs = songs; }
  public int size() { return songs.length; }
  public Song get(int i) { return songs[i]; }
}
var playList = new PlayList("80s", beatIt, thriller, billieJean);
playList.title = "Jackson songs";

// This allows for mutable objects


// ## What is an Object without Identity?

// - No identity means no address, thus no mutability
// - An object without identity behaves like a primitive value
//   - 17 is 17 wherever it lives in memory


// ## Value Objects

// A value class is a class which instances do not have identity

// These instances are called value objects

// We want the best of both worlds!
// - Abstraction offered by classes
// - Performance offered by primitives


// ## Value Objects

// Since value objects give up their identity, the JVM can use the value objects directly (no pointer)

// Two consequences:
// 1) No object header for value objects
// 2) What about methods that use addresses?


// ## Demo: Creation of a value record

value record Song(String title, int duration) {}


// ## Demo: == on a value object vs an identity object

value record Song(String title, int duration) {}
var s1 = new Song("Beat it", 4 * 60 + 58);
var s2 = new Song("Beat it", 4 * 60 + 58);
IO.println(s1 == s2);   // true


// ## Demo: default hashCode()
value class Album {
  String title;
  Album(String title) { this.title = title; }
}
var a1 = new Album("Dangerous");
var a2 = new Album("Dangerous");
IO.println(a1.hashCode() + " " + a2.hashCode());  // same value


// ## Demo: synchronization

value record Month(int month) {
  public Month {
    if (month < 1 || month > 12) { throw new IllegalArgumentException(); }
  }
}

var january = new Month(1);
synchronized(january) { }

Object hidden = january;
synchronized(hidden) { }


// ## Demo: Weak reference

value record Safe<T>(T value) {
  public Safe { Objects.requireNonNull(value); }
}
var safe = new Safe<>("hello");
var weak = new WeakReference<>(safe);

// A value object may not be represented by a pointer,
// so it can not be weak


// ## Object mutation and constructor

// You can observe final fields mutation during a constructor call

value class Album {
  String title;
  Album(String title) {
    IO.println(this);
    this.title = title;
  }
  public String toString() { return title; }
}
var album = new Album("Bag");


// ## Field strict initialization

// fields must be initialized before super() call

// "this" is available after super() call

value class Album {
  String title;
  Album(String title) {
    this.title = title;
    super();
    IO.println(this);  // okay, after super()
  }
  public String toString() { return title; }
}
var album = new Album("Bag");


// ## strict init and inheritance

// A value class can only inherits a value class (or Object)

abstract /*value*/ class TitledItem {
  String title;
  TitledItem(String title) {
    this.title = title;
  }
}
value class Album extends TitledItem {
  Album(String title) {
    super(title);
  }
}


// # Improving Performance: Scalarization on Stack


// ## Creating a mix tape

// Create a Song from all the songs in the `PlayList`

static Song createMixTape(PlayList playList) {
  var duration = 0;
  for (var i = 0; i < playList.size(); i++) {
    duration += playList.get(i).duration();
  }
  return new Song(playList.title, duration);
}


// ## Creating a mix tape: a little more functional

static Song createMixTape(PlayList playList) {
  var mixtape = new Song(playList.title, 0);
  for (var song : playList) {
    mixtape = new Song(mixtape.title(),
        mixtape.duration() + song.duration());
  }
  return mixtape;
}


// ## Creating a mix tape: full functional

static Song createMixTape(PlayList playList) {
  return playList.stream()
    .reduce(new Song(playList.title, 0),
       (s1, s2) -> new Song(s1.title(), s1.duration() + s2.duration()));
}


// ## Let's benchmark that with Song an identity class
// ` `

// ```
// Xeon(R) CPU E5-2683 v4 @ 2.10GHz
// Benchmark                           Mode  Cnt     Score     Error   Units
// identityMixTapePrimitive            avgt    5    72.097 ±   3.423   ns/op
// identityMixTapeObject               avgt    5   431.049 ±  73.446   ns/op
// identityMixTapeReduce               avgt    5   647.751 ±  99.281   ns/op
// ```

// Sadly, going toward a more functional writing style has a **cost** :(


// ## Let's benchmark that with Song a value class
// ` `

// ```
// Xeon(R) CPU E5-2683 v4 @ 2.10GHz
// Benchmark                           Mode  Cnt     Score     Error   Units
// valueNoFlatteningMixTapePrimitive   avgt    5    64.056 ±   0.346   ns/op
// valueNoFlatteningMixTapeObject      avgt    5    63.795 ±   0.284   ns/op
// valueNoFlatteningMixTapeReduce      avgt    5   658.346 ± 101.971   ns/op
// ```

// We can use value object: **abstraction** and **performance**


// ## Why?: Benchmark with additional GC results
// ` `

// ```
// Xeon(R) CPU E5-2683 v4 @ 2.10GHz
// Benchmark                           Mode  Cnt     Score     Error   Units
// identityMixTapePrimitive            avgt    5    72.097 ±   3.423   ns/op
//           gc.alloc.rate             avgt    5   211.625 ±   9.935  MB/sec
//           gc.count                  avgt    5     9.000            counts
//           gc.time                   avgt    5    18.000                ms
// identityMixTapeObject               avgt    5   431.049 ±  73.446   ns/op
//           gc.alloc.rate             avgt    5  3579.812 ± 577.552  MB/sec
//           gc.count                  avgt    5    51.000            counts
//           gc.time                   avgt    5    72.000                ms
// valueNoFlatteningMixTapePrimitive   avgt    5    64.056 ±   0.346   ns/op
//           gc.alloc.rate             avgt    5     0.003 ?    0.001  MB/sec
//           gc.count                  avgt    5       ≈ 0             counts
// valueNoFlatteningMixTapeObject      avgt    5    63.795 ±   0.284   ns/op
//           gc.alloc.rate             avgt    5     0.003 ±   0.001  MB/sec
//           gc.count                  avgt    5       ≈ 0            counts
// ```

// In the identity + primitive case, we are still allocating the return value


// ## Scalarization on Stack

// Two effects:
// - parameters/return value fields are in register
// - local variable fields are in registers

// In pseudo-code, the VM sees
// ```java
// static (String,int) createMixTape(PlayList playList) {
//   var mixtape = (playList.title, 0);
//   for (var song : playList) {
//     mixtape = (mixtape.title,
//         mixtape.duration + song.duration);
//   }
//   return mixtape;
// }
// ```


// ## Why it does not work with a stream ?

// Because of generics erasure :(

// To be able to optimize the VM need to see the value class
// but Stream<Song> is erased as a stream of Object

// We need specialized generics for that :(

// We are not there yet!


// # Saving Memory: Flattening on Heap

// ## Flattening on value object in fields/array

// If a value object is used in a field or an array,
// can we flatten it ?

var array = new Song[3];
array[0] = null;
array[1] = new Song("Thriller", 3 * 60 + 22);

// We need a way to represent `null`!
// By adding a new field "null_marker" of type `byte`

// Sadly, it does not work with Song declared like this
value record Song(String title, int duration) {}

// The JLS said that references must be read/write in
// one atomic operation (CPUs use 64 bits general purpose register)

// But the best size of Song is 32 bits (title) + 32 bits (duration) + 8 bits (null_marker)


// ## Max Flattening size
// ` `

// To be flatten in a field, in an array, the size of a value object
// must not be greater than 56 bits (we have to add the `null_marker` size)

// This is worst if you use ZGC or use more than 32G of RAM
// in both case, the size of a pointer is extended to 64 bits


// ## Duration of song are not that big

value record Song(String title, short duration) {}

// if pointers are 32 bits, the flattening size of Song is
// 32 + 16 = 48 bits < 56 bits

// so an array of Song is flatten

var songs = new Song[4];  // array of values (no pointer)


// ## Benchmarks
// ` `

// ```
// Xeon(R) CPU E5-2683 v4 @ 2.10GHz
// Benchmark                           Mode  Cnt     Score     Error   Units
// valueNoFlatteningMixTapePrimitive    avgt    5    64.056 ±   0.346   ns/op
// valueNoFlatteningMixTapeObject       avgt    5    63.795 ±   0.284   ns/op
// valueNoFlatteningMixTapeReduce       avgt    5   658.346 ± 101.971   ns/op
// valueMixTapePrimitive                avgt    5   124.161 ±   0.116   ns/op
// valueMixTapeObject                   avgt    5   128.540 ±   0.107   ns/op
// valueMixTapeReduce                   avgt    5  6930.126 ± 766.630   ns/op
// ```

// It uses less memory but performance is worst :(


// ### Why?

// The value object from the array need to be decoded using shift and mask

// ```java
// null_marker = object & 0xFF
// title = object >>> 32
// duration = (object >>> 16) & 0xFFFF
// ```

// Usually, those operations are fast and because the RAM access is slow,
// micro-benchmark magnified them because the array is in the caches


// ## Benefits of Flattening

// - No allocation overhead (when writing)
// - Denser storage (no object header)

// - No extra pointer dereferencing
// - Localized storage (no pointer chasing)

// The last two points are hard to see because Java GCs
// are moving collectors, so they already group objects together


// # How it works under the hood ?


// ## Instead of a micro benchmark, let's use JFR
// Using JFR to measure allocations

// [JFR allocation using value class](images/jfr-TODO-value.png)

// [JFR allocation using identity class](images/jfr-TODO-identity.png)


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


// ## How to prepare your code to JEP 401

// You can already
// - use flexible constructor (JEP 513) to write super() call
//   at the end of the constructor
// - do not use == on objects, even when override equals()
// - do not synchronized on something you do not own
// - do not create a WeakReference on something you do not own


// ## Demo: super() in Java 25
// You can write the call to super() at the end

class Person {
  String name;
  ArrayList<String> pets = new ArrayList<>();
  public Person(String name) {
    this.name = Objects.requireNonNull(name);
    super();
    // the compiler adds "this.pets = ... " here, ahhhh
  }
}

// But this is not enough, because the field initialization
// is done by the compiler after the call to super()


// ## Demo: super() in Java 25
// For class you want to migrate to value class in the future

// - Do not initialize fields with '='
// - write super() calls at the end

class Person {
  String name;
  ArrayList<String> pets;
  public Person(String name) {
    this.name = Objects.requireNonNull(name);
    this.pets = new ArrayList<>();
    super();
  }
}


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


// # ... and beyond?
// ` `

// ... we are still in exploratory mode


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


// ## Exploration 1: Null-restricted types
// Let's help flattening by adding nullability markers

// '!' or '?' sigils at the end of a type

// '!' for strictness/certainty, and '?' for uncertainty

// `Boolean!`, `Optional!`, `Complex?`, etc.


// ## Bang '!' as a contract
// Extends Java to add '!' at the end of a type of a field

// ```java
// value record Person(String name, int age) {}
// class Car {
//   int seat;
//   Person! person;
// }
// ```


// ## Fields with '!' have to be initialized before super()
// A null-restricted field can **not be set** to 'null'

// ```java
// value record Person(String name, int age) {}
// class Car {
//   int seat;
//   Person! person;
//   Car(Person person, int seat) {
//     this.person = person;   // the VM can throw a NPE
//     this.seat = seat;
//     super();
//   }
// }
//
// new Car(null, 5);
// ```


// ## Flattening on Heap (with '!')

// ![Heap representation with bang](images/value-null-restricted-in-memory.png)


// ## Why not using '?' instead of '!'?
// Like in Kotlin?

String s = null;   // Invalid in Kotlin, valid in Java

// Adding '?' requires to change the semantics of Java

// This is **not a backward compatible** change


// ## Using '!' on parameters and with identity objects

// ```java
// class Car {
//   // String! driver;
//   Car(String! driver) {
//     // this.driver = driver;
//     super();
//   }
// }
//
// new Car(null);
// ```

// Equivalent to `Objects.requireNonNull()` on the parameter


// ## Creating an array with '!'
// The array elements **can not be initialized** to `null`

// Without initial elements
// ```java
// value record Euro(long amount) {}
// var array = new Euro![4];   // does not compile
// ```

// We need a special syntax:
// ```java
// new Complex![] (index -> new Complex(index, index))
// ```


// ## Method selection and '!'
// We want to be backward compatible, so '!' can not be used in method selection

// ```java
// class MediaPlayer {
//  void play(Object item) {}
// }
// class MusicPlayer extends MediaPlayer {
//   void play(Song! song) {}
// }
// MusicPlayer player = new MusicPlayer();
// player.play(null);
// ```

// # Is declaring '!' at type level a good idea?

// null-restricted or non-atomic looks more like **storage** keywords
// than markers on types

// Also those keywords are **implementation decisions**, not something the user should control


// ## Exploration 2: Primitive class

// ## Primitive class
// A value class that acts more like a `primitive`

/*primitive*/ record Complex(double re, double im) {}

class Holder {
  Complex complex;  // strict non-null
  Holder() { }      // this.complex is initialized with Complex(0, 0)
}

// - Non-atomic on heap like primitives (full flattening)
// - Non-null on heap like primitives
// - Have a default value like primitives (all fields at zero)


// ## No Encapsulation

// A primitive class -> no identity, no encapsulation

// Fields are all independent
// => No way to enforce invariants on fields

// A primitive class is a compound of all its fields


// ## Default value

// The default value is only used for fields (like primitives)

class Holder {
  Complex c1;
  void m() {
    Complex c2;
    IO.println(c1);  // Complex.default == Complex(0, 0)
    IO.println(c2);  // does not compile!
  }
}

// You can replace `Complex` by `int` to understand how it works


// ## Arrays
// Arrays are stored in a flattened representation

var array = new Complex[10];

// Each element is initialized with `Complex.default`

// The array is fully flattened

//array[2] = null;   // throws a NPE


// ## Backward compatibility issue

// primitive class instance on stack can be `null`

class MyMap extends AbstractMap<String, Complex> {
  public int size() { return 1; }
  public Complex get(Object o) {
    return o.equals("foo") ? new Complex(1, 2) : null;
  }
  public Set<Map.Entry<String, Complex>> entrySet() {
    return Set.of(Map.entry("foo", new Complex(1, 2)));
  }
}

var complex = new MyMap().get("bar");
IO.println(complex);     // complex is null


// ## `null` primitive instances are allowed on stack

class Holder {
  Complex c;   // strict non-null
  Holder(Complex c) {
    Objects.requireNonNull(c, "c is null");  // good practice
    this.c = c;
  }
  void f(Complex c) {  // nullable
    // Complex c2 = Complex.default;  // can ask the default value
  }
}


// ## From the VM POV

// We need a handshake between the field and the class
// - the field has to be marked as `null_restricted`
// - the VM has to check that the type is a primitive class

// The latter can be done using a stricter **LoadableDescriptors**
// (fails if not a primitive class)

// So no backward compatible change from an existing class
// to a primitive class and vice-versa


// # Conclusion


// ## Valhalla Features
// ` `

// ```text
// | Feature                    | Status                |
// |----------------------------|-----------------------|
// | `value` class              | Java 28 Preview       |
// | `!` null-restricted syntax | Failed experiment     |
// | `primitive` class          | Future proposal       |
// | parametric JVM             | Future direction      |
// ```


// ## Roadmap to Valhalla
// Subject to change

// 🚚 JEP 513: Flexible Constructor Bodies

// 🏗️ JEP 401: Value Objects (Java 28 Preview)

// 🏗️ JEP 539: Strict Field Initialization in the JVM (Java 28 Preview)

// 🚧 JEP Draft: Primitive Class + non-null fields?

// ☁️ JEP 402: Enhanced Primitive Boxing (int ≈ Integer!)

// 🚧 Type Classes (operator overloading for primitive class)

// ☁️ Parametric JVM (List<ValueType>)















