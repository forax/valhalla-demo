package benchmark;

import jdk.internal.value.ValueClass;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/*

// java -jar target/benchmarks.jar -prof gc

// Xeon(R) CPU E5-2683 v4 @ 2.10GHz
//Benchmark                                               (arrayLength)  Mode  Cnt        Score        Error  Units
//PlayListFlatteningBench.identityMixTapePrimitive                  100  avgt    5       71.805 ±      3.407  ns/op
//PlayListFlatteningBench.identityMixTapePrimitive                 1000  avgt    5      993.584 ±     43.966  ns/op
//PlayListFlatteningBench.identityMixTapePrimitive                10000  avgt    5    20280.178 ±    401.236  ns/op
//PlayListFlatteningBench.identityMixTapePrimitive               100000  avgt    5   212504.226 ±  19021.351  ns/op
//PlayListFlatteningBench.identityMixTapePrimitive              1000000  avgt    5  7578279.428 ±  53575.320  ns/op
//PlayListFlatteningBench.valueMixTapeObject                        100  avgt    5      129.680 ±      0.266  ns/op
//PlayListFlatteningBench.valueMixTapeObject                       1000  avgt    5     1203.405 ±      5.206  ns/op
//PlayListFlatteningBench.valueMixTapeObject                      10000  avgt    5    11231.185 ±     71.869  ns/op
//PlayListFlatteningBench.valueMixTapeObject                     100000  avgt    5   128277.728 ±  17904.514  ns/op
//PlayListFlatteningBench.valueMixTapeObject                    1000000  avgt    5  1346210.425 ±  90182.375  ns/op
//PlayListFlatteningBench.valueMixTapePrimitive                     100  avgt    5      125.295 ±      0.075  ns/op
//PlayListFlatteningBench.valueMixTapePrimitive                    1000  avgt    5     1202.251 ±      4.785  ns/op
//PlayListFlatteningBench.valueMixTapePrimitive                   10000  avgt    5    12043.989 ±    287.158  ns/op
//PlayListFlatteningBench.valueMixTapePrimitive                  100000  avgt    5   130601.255 ±  12947.919  ns/op
//PlayListFlatteningBench.valueMixTapePrimitive                 1000000  avgt    5  1192768.007 ±   1413.961  ns/op
//PlayListFlatteningBench.valueNoFlatteningMixTapeObject            100  avgt    5       64.491 ±      0.196  ns/op
//PlayListFlatteningBench.valueNoFlatteningMixTapeObject           1000  avgt    5      969.445 ±     31.721  ns/op
//PlayListFlatteningBench.valueNoFlatteningMixTapeObject          10000  avgt    5    21190.143 ±     99.167  ns/op
//PlayListFlatteningBench.valueNoFlatteningMixTapeObject         100000  avgt    5   218990.027 ±   2818.897  ns/op
//PlayListFlatteningBench.valueNoFlatteningMixTapeObject        1000000  avgt    5  7887826.631 ± 150406.302  ns/op

// MacBook Air M2
//Benchmark                                               (arrayLength)  Mode  Cnt        Score       Error  Units
//PlayListFlatteningBench.identityMixTapePrimitive                  100  avgt    5       42,689 ±     0,700  ns/op
//PlayListFlatteningBench.identityMixTapePrimitive                 1000  avgt    5      567,601 ±    21,706  ns/op
//PlayListFlatteningBench.identityMixTapePrimitive                10000  avgt    5     6505,286 ±   108,200  ns/op
//PlayListFlatteningBench.identityMixTapePrimitive               100000  avgt    5    64707,764 ±  1413,198  ns/op
//PlayListFlatteningBench.identityMixTapePrimitive              1000000  avgt    5  1157001,545 ± 61244,463  ns/op
//PlayListFlatteningBench.valueMixTapeObject                        100  avgt    5       59,532 ±     8,743  ns/op
//PlayListFlatteningBench.valueMixTapeObject                       1000  avgt    5      607,321 ±     9,360  ns/op
//PlayListFlatteningBench.valueMixTapeObject                      10000  avgt    5     6115,694 ±    77,616  ns/op
//PlayListFlatteningBench.valueMixTapeObject                     100000  avgt    5    61901,384 ±  1382,218  ns/op
//PlayListFlatteningBench.valueMixTapeObject                    1000000  avgt    5   615983,621 ±  7561,750  ns/op
//PlayListFlatteningBench.valueMixTapePrimitive                     100  avgt    5       58,427 ±     0,616  ns/op
//PlayListFlatteningBench.valueMixTapePrimitive                    1000  avgt    5      607,167 ±     8,589  ns/op
//PlayListFlatteningBench.valueMixTapePrimitive                   10000  avgt    5     6150,839 ±   177,506  ns/op
//PlayListFlatteningBench.valueMixTapePrimitive                  100000  avgt    5    61636,249 ±   970,145  ns/op
//PlayListFlatteningBench.valueMixTapePrimitive                 1000000  avgt    5   615596,484 ± 10141,675  ns/op
//PlayListFlatteningBench.valueNoFlatteningMixTapeObject            100  avgt    5       41,506 ±     4,066  ns/op
//PlayListFlatteningBench.valueNoFlatteningMixTapeObject           1000  avgt    5      566,769 ±     8,952  ns/op
//PlayListFlatteningBench.valueNoFlatteningMixTapeObject          10000  avgt    5     6504,729 ±    97,225  ns/op
//PlayListFlatteningBench.valueNoFlatteningMixTapeObject         100000  avgt    5    66902,148 ±  1100,894  ns/op
//PlayListFlatteningBench.valueNoFlatteningMixTapeObject        1000000  avgt    5  1176077,505 ± 63050,652  ns/op

@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = { "--enable-preview", "--add-exports=java.base/jdk.internal.value=ALL-UNNAMED" })
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class PlayListFlatteningBench {

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
              (a, b) -> new Song(playList.title, (short) (a.duration + b.duration)));
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
              (a, b) -> new Song(playList.title, (short) (a.duration + b.duration)));
    }
  }

  @Param({"100", "1000", "10000", "100000", "1000000"})
  public int arrayLength;

  private Value.PlayList valuePlayList;
  private Value.PlayList valueNoFlatteningPlayList;
  private Identity.PlayList identityPlayList;

  @org.openjdk.jmh.annotations.Setup(Level.Trial)
  public void setup() {
    valuePlayList = new Value.PlayList("80s", IntStream.range(0, arrayLength)
        .mapToObj(i -> new Value.Song("Song " + i, (short) i))
        .toArray(Value.Song[]::new));
    valueNoFlatteningPlayList = new Value.PlayList("80s", IntStream.range(0, arrayLength)
        .mapToObj(i -> new Value.Song("Song " + i, (short) i))
        .toArray(length -> (Value.Song[]) ValueClass.newReferenceArray(Value.Song.class, length)));
    identityPlayList = new Identity.PlayList("80s", IntStream.range(0, arrayLength)
        .mapToObj(i -> new Identity.Song("Song " + i, (short) i))
        .toArray(Identity.Song[]::new));
    {
      if (!ValueClass.isFlatArray(valuePlayList.songs)) {
        throw new AssertionError("valuePlayList should be flattened");
      }
      if (ValueClass.isFlatArray(valueNoFlatteningPlayList.songs)) {
        throw new AssertionError("valueNoFlatteningPlayList should not be flattened");
      }
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

  //@Benchmark
  public Value.Song valueMixTapeReduce() {
    return Value.mixTapeReduce(valuePlayList);
  }

  //@Benchmark
  public Value.Song valueNoFlatteningMixTapePrimitive() {
    return Value.mixTapePrimitive(valueNoFlatteningPlayList);
  }

  @Benchmark
  public Value.Song valueNoFlatteningMixTapeObject() {
    return Value.mixTapeObject(valueNoFlatteningPlayList);
  }

  //@Benchmark
  public Value.Song valueNoFlatteningMixTapeReduce() {
    return Value.mixTapeReduce(valueNoFlatteningPlayList);
  }

  @Benchmark
  public Identity.Song identityMixTapePrimitive() {
    return Identity.mixTapePrimitive(identityPlayList);
  }

  //@Benchmark
  public Identity.Song identityMixTapeObject() {
    return Identity.mixTapeObject(identityPlayList);
  }

  //@Benchmark
  public Identity.Song identityMixTapeReduce() {
    return Identity.mixTapeReduce(identityPlayList);
  }
}
 */

