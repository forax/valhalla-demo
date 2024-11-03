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
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import data3.Population;

import static java.util.stream.IntStream.range;

// Benchmark                            Mode  Cnt   Score   Error  Units
// FlatteningBench.totalInt             avgt    5  10.454 ± 0.007  us/op
// FlatteningBench.totalPopulation      avgt    5  19.271 ± 0.216  us/op
// FlatteningBench.totalPopulationBang  avgt    5  10.456 ± 0.013  us/op

// export JAVA_HOME=/Users/forax/valhalla-live/valhalla/build/macosx-aarch64-server-release/images/jdk/
// $JAVA_HOME/bin/java -jar target/benchmarks.jar
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = { "--enable-preview", "--add-exports=java.base/jdk.internal.vm.annotation=ALL-UNNAMED", "--add-exports=java.base/jdk.internal.value=ALL-UNNAMED" })
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class FlatteningBench {

  private final int[] ints;
  private final Population[] populations;
  private final Population[] populationBangs;
  {
    var ints = range(0, 36_000).toArray();
    this.ints = ints;

    var populations = range(0, 36_000)
        .mapToObj(Population::new)
        .toArray(Population[]::new);
    Collections.shuffle(Arrays.asList(populations), new Random(0));
    this.populations = populations;

    var populationBangs = (Population[]) ValueClass.newNullRestrictedArray(Population.class, 36_000);
    System.arraycopy(populations, 0, populationBangs, 0, populations.length);
    this.populationBangs = populationBangs;
  }

  //@Benchmark
  public int totalInt() {
    var total = 0;
    for (var population : ints) {
      total = total + population;
    }
    return total;
  }

  //@Benchmark
  public Population totalPopulation() {
    var total = new Population(0);
    for (var population : populations) {
      total = total.add(population);
    }
    return total;
  }

  //@Benchmark
  public Population totalPopulationBang() {
    var total = new Population(0);
    for (var population : populationBangs) {
      total = total.add(population);
    }
    return total;
  }
}
