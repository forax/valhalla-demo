package benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import data.Population;

import java.util.concurrent.TimeUnit;

// Benchmark                          Mode  Cnt     Score    Error  Units
// identity class
// ScalarizationBench.loopInt         avgt    5   283.340 ± 0.434  ns/op
// ScalarizationBench.loopPopulation  avgt    5  1229.171 ± 4.104  ns/op

// value class
// ScalarizationBench.loopInt         avgt    5  283.386 ± 0.620  ns/op
// ScalarizationBench.loopPopulation  avgt    5  283.414 ± 0.302  ns/op

// export JAVA_HOME=/Users/forax/valhalla-live/valhalla/build/macosx-aarch64-server-release/images/jdk/
// $JAVA_HOME/bin/java -jar target/benchmarks.jar
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = { "--enable-preview" })
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class ScalarizationBench {

  //@Benchmark
  public int loopInt() {
    var total = 0;
    for (var i = 0; i < 1_000; i++) {
      total = total + i;
    }
    return total;
  }

  //@Benchmark
  public Population loopPopulation() {
    var total = new Population(0);
    for (var i = 0; i < 1_000; i++) {
      total = total.add(new Population(i));
    }
    return total;
  }
}
