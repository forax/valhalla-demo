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
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

// java -jar target/benchmarks.jar -prof gc

// Xeon(R) CPU E5-2683 v4 @ 2.10GHz
//Benchmark                                                         Mode  Cnt     Score     Error   Units
//PlayListBench.identityMixTapeObject                              avgt    5   431.049 ±  73.446   ns/op
//PlayListBench.identityMixTapePrimitive                           avgt    5    72.097 ±   3.423   ns/op
//PlayListBench.identityMixTapeReduce                              avgt    5   647.751 ±  99.281   ns/op
//PlayListBench.valueMixTapeObject                                 avgt    5   128.540 ±   0.107   ns/op
//PlayListBench.valueMixTapePrimitive                              avgt    5   124.161 ±   0.116   ns/op
//PlayListBench.valueMixTapeReduce                                 avgt    5  6930.126 ± 766.630   ns/op
//PlayListBench.valueNoFlatteningMixTapePrimitive                  avgt    5    64.056 ±   0.346   ns/op
//PlayListBench.valueNoFlatteningMixTapeObject                     avgt    5    63.795 ±   0.284   ns/op
//PlayListBench.valueNoFlatteningMixTapeReduce                     avgt    5   658.346 ± 101.971   ns/op

//PlayListBench.identityMixTapeObject:gc.alloc.rate                avgt    5  3579.812 ± 577.552  MB/sec
//PlayListBench.identityMixTapeObject:gc.count                     avgt    5    51.000            counts
//PlayListBench.identityMixTapeObject:gc.time                      avgt    5    72.000                ms

//PlayListBench.identityMixTapePrimitive:gc.alloc.rate             avgt    5   211.625 ±   9.935  MB/sec
//PlayListBench.identityMixTapePrimitive:gc.count                  avgt    5     9.000            counts
//PlayListBench.identityMixTapePrimitive:gc.time                   avgt    5    18.000                ms

//PlayListBench.identityMixTapeReduce:gc.alloc.rate                avgt    5  2558.609 ± 397.773  MB/sec
//PlayListBench.identityMixTapeReduce:gc.count                     avgt    5    11.000            counts
//PlayListBench.identityMixTapeReduce:gc.time                      avgt    5    32.000                ms

//PlayListBench.valueMixTapeObject:gc.alloc.rate                   avgt    5     0.003 ±   0.001  MB/sec
//PlayListBench.valueMixTapeObject:gc.count                        avgt    5       ≈ 0            counts

//PlayListBench.valueMixTapePrimitive:gc.alloc.rate                avgt    5     0.003 ±   0.001  MB/sec
//PlayListBench.valueMixTapePrimitive:gc.count                     avgt    5       ≈ 0            counts

//PlayListBench.valueMixTapeReduce:gc.alloc.rate                   avgt    5   459.275 ±  50.589  MB/sec
//PlayListBench.valueMixTapeReduce:gc.count                        avgt    5     2.000            counts
//PlayListBench.valueMixTapeReduce:gc.time                         avgt    5     6.000                ms

//PlayListBench.valueNoFlatteningMixTapePrimitive:gc.alloc.rate    avgt    5     0.003 ?    0.001  MB/sec
//PlayListBench.valueNoFlatteningMixTapePrimitive:gc.count         avgt    5       ≈ 0             counts

//PlayListBench.valueNoFlatteningMixTapeObject:gc.alloc.rate       avgt    5     0.003 ±   0.001  MB/sec
//PlayListBench.valueNoFlatteningMixTapeObject:gc.count            avgt    5       ≈ 0            counts

//PlayListBench.valueNoFlatteningMixTapeReduce:gc.alloc.rate       avgt    5  2517.432 ± 389.752  MB/sec
//PlayListBench.valueNoFlatteningMixTapeReduce:gc.count            avgt    5    11.000            counts
//PlayListBench.valueNoFlatteningMixTapeReduce:gc.time             avgt    5    24.000                ms

// MacBook Air M2
//Benchmark                                                         Mode  Cnt     Score     Error   Units
//PlayListBench.identityMixTapeObject                              avgt    5   211,903 ±  21,437   ns/op
//PlayListBench.identityMixTapePrimitive                           avgt    5    42,748 ±   1,097   ns/op
//PlayListBench.identityMixTapeReduce                              avgt    5   320,026 ±   3,429   ns/op
//PlayListBench.valueMixTapeObject                                 avgt    5    58,261 ±   0,598   ns/op
//PlayListBench.valueMixTapePrimitive                              avgt    5    57,730 ±   0,780   ns/op
//PlayListBench.valueMixTapeReduce                                 avgt    5  3289,558 ±  34,361   ns/op
//PlayListBench.valueNoFlatteningMixTapePrimitive                  avgt    5    40,723 ±   0,437   ns/op
//PlayListBench.valueNoFlatteningMixTapeObject                     avgt    5    40,944 ±   0,475   ns/op
//PlayListBench.valueNoFlatteningMixTapeReduce                     avgt    5   348,372 ±   4,759   ns/op

//PlayListBench.identityMixTapeObject:gc.alloc.rate                avgt    5  7276,198 ± 713,408  MB/sec
//PlayListBench.identityMixTapeObject:gc.count                     avgt    5   204,000            counts
//PlayListBench.identityMixTapeObject:gc.time                      avgt    5    87,000                ms

//PlayListBench.identityMixTapePrimitive:gc.alloc.rate             avgt    5   356,933 ±   9,090  MB/sec
//PlayListBench.identityMixTapePrimitive:gc.count                  avgt    5   138,000            counts
//PlayListBench.identityMixTapePrimitive:gc.time                   avgt    5    25,000                ms

//PlayListBench.identityMixTapeReduce:gc.alloc.rate                avgt    5  5172,941 ±  55,504  MB/sec
//PlayListBench.identityMixTapeReduce:gc.count                     avgt    5   125,000            counts
//PlayListBench.identityMixTapeReduce:gc.time                      avgt    5    53,000                ms

//PlayListBench.valueMixTapeObject:gc.alloc.rate                   avgt    5     0,003 ±   0,001  MB/sec
//PlayListBench.valueMixTapeObject:gc.count                        avgt    5       ≈ 0            counts

//PlayListBench.valueMixTapePrimitive:gc.alloc.rate                avgt    5     0,003 ±   0,001  MB/sec
//PlayListBench.valueMixTapePrimitive:gc.count                     avgt    5       ≈ 0            counts

//PlayListBench.valueMixTapeReduce:gc.alloc.rate                   avgt    5   967,025 ±  10,381  MB/sec
//PlayListBench.valueMixTapeReduce:gc.count                        avgt    5    60,000            counts
//PlayListBench.valueMixTapeReduce:gc.time                         avgt    5    22,000                ms

//PlayListBench.valueNoFlatteningMixTapeObject:gc.alloc.rate       avgt    5     0,003 ±   0,001  MB/sec
//PlayListBench.valueNoFlatteningMixTapeObject:gc.count            avgt    5       ≈ 0            counts

//PlayListBench.valueNoFlatteningMixTapeReduce:gc.alloc.rate       avgt    5  4751,954 ±  64,399  MB/sec
//PlayListBench.valueNoFlatteningMixTapeReduce:gc.count            avgt    5   220,000            counts
//PlayListBench.valueNoFlatteningMixTapeReduce:gc.time             avgt    5    85,000                ms


@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = { "--enable-preview", "--add-exports=java.base/jdk.internal.value=ALL-UNNAMED" })
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class PlayListBench {

  static final class Value {
    value record Song(String title, short duration) { }

    static final value class PlayList {
      String title;
      Song[] songs;

      public PlayList(String title, Song... songs) {
        this.title = Objects.requireNonNull(title);
        this.songs = songs.clone();
        super();
      }

      int size() {
        return songs.length;
      }
      Song get(int i) {
        return songs[i];
      }
    }

    static Song mixTapePrimitive(PlayList playList) {
      var duration = (short) 0;
      for (var i = 0; i < playList.size(); i++) {
        duration += playList.get(i).duration;
      }
      return new Song(playList.title, duration);
    }

    static Song mixTapeObject(PlayList playList) {
      var mixTape = new Song(playList.title, (short) 0);
      for (var i = 0; i < playList.size(); i++) {
        mixTape = new Song(mixTape.title,
            (short) (mixTape.duration + playList.get(i).duration));
      }
      return mixTape;
    }

    static Song mixTapeReduce(PlayList playList) {
      return Arrays.stream(playList.songs)
          .reduce(new Song(playList.title, (short) 0),
              (a, b) -> new Song(a.title, (short) (a.duration + b.duration)));
    }
  }

  static final class Identity {
    record Song(String title, short duration) { }

    static final class PlayList {
      String title;
      Song[] songs;

      public PlayList(String title, Song... songs) {
        this.title = Objects.requireNonNull(title);
        this.songs = songs.clone();
        super();
      }

      int size() {
        return songs.length;
      }
      Song get(int i) {
        return songs[i];
      }
    }

    static Song mixTapePrimitive(PlayList playList) {
      var duration = (short) 0;
      for (var i = 0; i < playList.size(); i++) {
        duration += playList.get(i).duration;
      }
      return new Song(playList.title, duration);
    }

    static Song mixTapeObject(PlayList playList) {
      var mixTape = new Song(playList.title, (short) 0);
      for (var i = 0; i < playList.size(); i++) {
        mixTape = new Song(mixTape.title,
            (short) (mixTape.duration + playList.get(i).duration));
      }
      return mixTape;
    }

    static Song mixTapeReduce(PlayList playList) {
      return Arrays.stream(playList.songs)
          .reduce(new Song(playList.title, (short) 0),
              (a, b) -> new Song(a.title, (short) (a.duration + b.duration)));
    }
  }

  private final Value.PlayList valuePlayList = new Value.PlayList("80s", IntStream.range(0, 100)
      .mapToObj(i -> new Value.Song("Song " + i, (short) i))
      .toArray(Value.Song[]::new));
  private final Value.PlayList valueNoFlatteningPlayList = new Value.PlayList("80s", IntStream.range(0, 100)
      .mapToObj(i -> new Value.Song("Song " + i, (short) i))
      .toArray(length -> (Value.Song[]) ValueClass.newReferenceArray(Value.Song.class, length)));
  {
    if (!ValueClass.isFlatArray(valuePlayList.songs)) {
      throw new AssertionError("PlayList should be flattened");
    }
    if (ValueClass.isFlatArray(valueNoFlatteningPlayList.songs)) {
      throw new AssertionError("PlayList should not be flattened");
    }
  }

  @Benchmark
  public Value.Song valueMixTapePrimitive() {
    return Value.mixTapePrimitive(valuePlayList);
  }

  @Benchmark
  public Value.Song valueMixTapeObject() {
    return Value.mixTapeObject(valuePlayList);
  }

  @Benchmark
  public Value.Song valueMixTapeReduce() {
    return Value.mixTapeReduce(valuePlayList);
  }

  @Benchmark
  public Value.Song valueNoFlatteningMixTapePrimitive() {
    return Value.mixTapePrimitive(valueNoFlatteningPlayList);
  }

  @Benchmark
  public Value.Song valueNoFlatteningMixTapeObject() {
    return Value.mixTapeObject(valueNoFlatteningPlayList);
  }

  @Benchmark
  public Value.Song valueNoFlatteningMixTapeReduce() {
    return Value.mixTapeReduce(valueNoFlatteningPlayList);
  }

  private final Identity.PlayList identityPlayList = new Identity.PlayList("80s", IntStream.range(0, 100)
      .mapToObj(i -> new Identity.Song("Song " + i, (short) i))
      .toArray(Identity.Song[]::new));

  @Benchmark
  public Identity.Song identityMixTapePrimitive() {
    return Identity.mixTapePrimitive(identityPlayList);
  }

  @Benchmark
  public Identity.Song identityMixTapeObject() {
    return Identity.mixTapeObject(identityPlayList);
  }

  @Benchmark
  public Identity.Song identityMixTapeReduce() {
    return Identity.mixTapeReduce(identityPlayList);
  }
}


