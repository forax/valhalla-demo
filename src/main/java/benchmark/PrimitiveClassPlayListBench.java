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

//Benchmark                                          Mode  Cnt     Score     Error  Units
//PrimitiveClassPlayListBench.valueMixTapeObject     avgt    5    16,900 ±   0,121  ns/op
//PrimitiveClassPlayListBench.valueMixTapePrimitive  avgt    5    16,903 ±   0,335  ns/op
//PrimitiveClassPlayListBench.valueMixTapeReduce     avgt    5  3168,916 ± 117,187  ns/op

@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = { "--enable-preview", "--add-exports=java.base/jdk.internal.value=ALL-UNNAMED" })
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class PrimitiveClassPlayListBench {

  static final class PrimitiveClass {
    @jdk.internal.vm.annotation.LooselyConsistentValue
    value record Song(String title, int duration) { }

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
      var duration = 0;
      for (var i = 0; i < playList.size(); i++) {
        duration += playList.get(i).duration;
      }
      return new Song(playList.title, duration);
    }

    static Song mixTapeObject(PlayList playList) {
      var mixTape = new Song(playList.title, 0);
      for (var i = 0; i < playList.size(); i++) {
        mixTape = new Song(mixTape.title,
            mixTape.duration + playList.get(i).duration);
      }
      return mixTape;
    }

    static Song mixTapeReduce(PlayList playList) {
      return Arrays.stream(playList.songs)
          .reduce(new Song(playList.title, (short) 0),
              (a, b) -> new Song(a.title, a.duration + b.duration));
    }
  }



  private final PrimitiveClass.PlayList primitiveClassPlayList = new PrimitiveClass.PlayList("80s", IntStream.range(0, 100)
      .mapToObj(i -> new PrimitiveClass.Song("" + i, i))
      .toArray(length -> (PrimitiveClass.Song[]) ValueClass.newNullRestrictedNonAtomicArray(PrimitiveClass.Song.class, length, new PrimitiveClass.Song(null, 0))));
  {
    if (!ValueClass.isFlatArray(primitiveClassPlayList.songs)) {
      throw new AssertionError("primitiveClassPlayList should be flattened");
    }
  }

  @Benchmark
  public PrimitiveClass.Song valueMixTapePrimitive() {
    return PrimitiveClass.mixTapePrimitive(primitiveClassPlayList);
  }

  @Benchmark
  public PrimitiveClass.Song valueMixTapeObject() {
    return PrimitiveClass.mixTapeObject(primitiveClassPlayList);
  }

  @Benchmark
  public PrimitiveClass.Song valueMixTapeReduce() {
    return PrimitiveClass.mixTapeReduce(primitiveClassPlayList);
  }
}


