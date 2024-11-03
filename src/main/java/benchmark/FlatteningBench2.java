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

import data3.City;
import data3.Population;

import static java.util.stream.IntStream.range;

// Benchmark                       Mode  Cnt   Score   Error  Units
// FlatteningBench2.totalInt       avgt    5  42.294 ± 0.255  us/op
// FlatteningBench2.totalCity      avgt    5  41.791 ± 0.587  us/op
// FlatteningBench2.totalCityBang  avgt    5  20.822 ± 0.032  us/op

// export JAVA_HOME=/Users/forax/valhalla-live/valhalla/build/macosx-aarch64-server-release/images/jdk/
// $JAVA_HOME/bin/java -jar target/benchmarks.jar
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = { "--enable-preview", "--add-exports=java.base/jdk.internal.vm.annotation=ALL-UNNAMED", "--add-exports=java.base/jdk.internal.value=ALL-UNNAMED" })
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class FlatteningBench2 {

  private final City[] cities;
  private final City[] cityBangs;
  {
    var populations = range(0, 36_000)
        .mapToObj(Population::new)
        .toArray(Population[]::new);
    Collections.shuffle(Arrays.asList(populations), new Random(0));

    var cities = range(0, 36_000)
        .mapToObj(i -> new City("" + i, populations[i]))
        .toArray(City[]::new);
    Collections.shuffle(Arrays.asList(cities), new Random(0));
    this.cities = cities;

    var cityBangs = (City[]) ValueClass.newNullRestrictedArray(City.class, 36_000);
    System.arraycopy(cities, 0, cityBangs, 0, cities.length);
    this.cityBangs = cityBangs;
  }

  //@Benchmark
  public int totalInt() {
    var total = 0;
    for (var city : cities) {
      total = total + city.population().value();
    }
    return total;
  }

  //@Benchmark
  public Population totalCity() {
    var total = new Population(0);
    for (var city : cities) {
      total = total.add(city.population());
    }
    return total;
  }

  //@Benchmark
  public Population totalCityBang() {
    var total = new Population(0);
    for (var city : cityBangs) {
      total = total.add(city.population());
    }
    return total;
  }
}
