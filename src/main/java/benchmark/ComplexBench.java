package benchmark;

import jdk.internal.value.ValueClass;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

// java -jar target/benchmarks.jar -prof gc

//Benchmark                          Mode  Cnt     Score    Error  Units
//ComplexBench.identitySumObject     avgt    5   346,278 ±  1,804  ns/op
//ComplexBench.identitySumPrimitive  avgt    5    60,797 ±  0,865  ns/op
//ComplexBench.identitySumReduce     avgt    5   369,301 ±  1,921  ns/op
//ComplexBench.valueSumObject        avgt    5    50,466 ±  0,846  ns/op
//ComplexBench.valueSumPrimitive     avgt    5    50,278 ±  0,165  ns/op
//ComplexBench.valueSumReduce        avgt    5  2820,367 ± 27,865  ns/op

/*
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = { "--enable-preview", "--add-exports=java.base/jdk.internal.value=ALL-UNNAMED" })
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class ComplexBench {

  static final class Value {
    @jdk.internal.vm.annotation.LooselyConsistentValue
    //primitive
    value record Complex(double re, double im) {
      Complex add(Complex c) {
        return new Complex(re + c.re, im + c.im);
      }
    }

    static final value class ComplexList {
      Complex[] complexes;

      public ComplexList(Complex... complexes) {
        this.complexes = complexes.clone();
        super();
      }

      int size() {
        return complexes.length;
      }
      Complex get(int i) {
        return complexes[i];
      }
    }

    static Complex sumPrimitive(ComplexList complexList) {
      var re = 0.0;
      var im = 0.0;
      for (var i = 0; i < complexList.size(); i++) {
        var complex = complexList.get(i);
        re += complex.re;
        im += complex.im;
      }
      return new Complex(re, im);
    }

    static Complex sumObject(ComplexList complexList) {
     var complex = new Complex(0.0, 0.0);
      for (var i = 0; i < complexList.size(); i++) {
        complex = complex.add(complexList.get(i));
      }
      return complex;
    }

    static Complex sumReduce(ComplexList complexList) {
      return Arrays.stream(complexList.complexes)
          .reduce(new Complex(0.0, 0.0), Complex::add);
    }
  }

  static final class Identity {
    record Complex(double re, double im) {
      Complex add(Complex c) {
        return new Complex(re + c.re, im + c.im);
      }
    }

    static final class ComplexList {
      Complex[] complexes;

      public ComplexList(Complex... complexes) {
        this.complexes = complexes.clone();
        super();
      }

      int size() {
        return complexes.length;
      }
      Complex get(int i) {
        return complexes[i];
      }
    }

    static Complex sumPrimitive(ComplexList complexList) {
      var re = 0.0;
      var im = 0.0;
      for (var i = 0; i < complexList.size(); i++) {
        var complex = complexList.get(i);
        re += complex.re;
        im += complex.im;
      }
      return new Complex(re, im);
    }

    static Complex sumObject(ComplexList complexList) {
      var complex = new Complex(0.0, 0.0);
      for (var i = 0; i < complexList.size(); i++) {
        complex = complex.add(complexList.get(i));
      }
      return complex;
    }

    static Complex sumReduce(ComplexList complexList) {
      return Arrays.stream(complexList.complexes)
          .reduce(new Complex(0.0, 0.0), Complex::add);
    }
  }

  private final Value.ComplexList valueComplexList = new Value.ComplexList(IntStream.range(0, 100)
      .mapToObj(i -> new Value.Complex(i, i))
      .toArray(length -> (Value.Complex[]) ValueClass.newNullRestrictedNonAtomicArray(Value.Complex.class, length, new Value.Complex(0, 0))));
  {
    if (!ValueClass.isFlatArray(valueComplexList.complexes)) {
      throw new AssertionError("valueComplexList should be flattened");
    }
  }

  @Benchmark
  public Value.Complex valueSumPrimitive() {
    return Value.sumPrimitive(valueComplexList);
  }

  @Benchmark
  public Value.Complex valueSumObject() {
    return Value.sumObject(valueComplexList);
  }

  @Benchmark
  public Value.Complex valueSumReduce() {
    return Value.sumReduce(valueComplexList);
  }

  private final Identity.ComplexList identityComplexList = new Identity.ComplexList(IntStream.range(0, 100)
      .mapToObj(i -> new Identity.Complex(i, i))
      .toArray(Identity.Complex[]::new));

  @Benchmark
  public Identity.Complex identitySumPrimitive() {
    return Identity.sumPrimitive(identityComplexList);
  }

  @Benchmark
  public Identity.Complex identitySumObject() {
    return Identity.sumObject(identityComplexList);
  }

  @Benchmark
  public Identity.Complex identitySumReduce() {
    return Identity.sumReduce(identityComplexList);
  }
}
*/


