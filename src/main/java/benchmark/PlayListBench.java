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

// Benchmark                                                         Mode  Cnt     Score     Error   Units
//PlayListBench.identityFullSongObject                              avgt    5   211,903 ±  21,437   ns/op
//PlayListBench.identityFullSongPrimitive                           avgt    5    42,748 ±   1,097   ns/op
//PlayListBench.identityFullSongReduce                              avgt    5   320,026 ±   3,429   ns/op
//PlayListBench.valueFullSongObject                                 avgt    5    58,261 ±   0,598   ns/op
//PlayListBench.valueFullSongPrimitive                              avgt    5    57,730 ±   0,780   ns/op
//PlayListBench.valueFullSongReduce                                 avgt    5  3289,558 ±  34,361   ns/op
//PlayListBench.valueNoFlatteningFullSongObject                     avgt    5    40,944 ±   0,475   ns/op
//PlayListBench.valueNoFlatteningFullSongReduce                     avgt    5   348,372 ±   4,759   ns/op

//PlayListBench.identityFullSongObject:gc.alloc.rate                avgt    5  7276,198 ± 713,408  MB/sec
//PlayListBench.identityFullSongObject:gc.count                     avgt    5   204,000            counts
//PlayListBench.identityFullSongObject:gc.time                      avgt    5    87,000                ms

//PlayListBench.identityFullSongPrimitive                           avgt    5    42,748 ±   1,097   ns/op
//PlayListBench.identityFullSongPrimitive:gc.alloc.rate             avgt    5   356,933 ±   9,090  MB/sec
//PlayListBench.identityFullSongPrimitive:gc.count                  avgt    5   138,000            counts
//PlayListBench.identityFullSongPrimitive:gc.time                   avgt    5    25,000                ms

//PlayListBench.identityFullSongReduce                              avgt    5   320,026 ±   3,429   ns/op
//PlayListBench.identityFullSongReduce:gc.alloc.rate                avgt    5  5172,941 ±  55,504  MB/sec
//PlayListBench.identityFullSongReduce:gc.count                     avgt    5   125,000            counts
//PlayListBench.identityFullSongReduce:gc.time                      avgt    5    53,000                ms

//PlayListBench.valueFullSongObject                                 avgt    5    58,261 ±   0,598   ns/op
//PlayListBench.valueFullSongObject:gc.alloc.rate                   avgt    5     0,003 ±   0,001  MB/sec
//PlayListBench.valueFullSongObject:gc.count                        avgt    5       ≈ 0            counts

//PlayListBench.valueFullSongPrimitive                              avgt    5    57,730 ±   0,780   ns/op
//PlayListBench.valueFullSongPrimitive:gc.alloc.rate                avgt    5     0,003 ±   0,001  MB/sec
//PlayListBench.valueFullSongPrimitive:gc.count                     avgt    5       ≈ 0            counts

//PlayListBench.valueFullSongReduce                                 avgt    5  3289,558 ±  34,361   ns/op
//PlayListBench.valueFullSongReduce:gc.alloc.rate                   avgt    5   967,025 ±  10,381  MB/sec
//PlayListBench.valueFullSongReduce:gc.count                        avgt    5    60,000            counts
//PlayListBench.valueFullSongReduce:gc.time                         avgt    5    22,000                ms

//PlayListBench.valueNoFlatteningFullSongObject                     avgt    5    40,944 ±   0,475   ns/op
//PlayListBench.valueNoFlatteningFullSongObject:gc.alloc.rate       avgt    5     0,003 ±   0,001  MB/sec
//PlayListBench.valueNoFlatteningFullSongObject:gc.count            avgt    5       ≈ 0            counts

//PlayListBench.valueNoFlatteningFullSongReduce                     avgt    5   348,372 ±   4,759   ns/op
//PlayListBench.valueNoFlatteningFullSongReduce:gc.alloc.rate       avgt    5  4751,954 ±  64,399  MB/sec
//PlayListBench.valueNoFlatteningFullSongReduce:gc.count            avgt    5   220,000            counts
//PlayListBench.valueNoFlatteningFullSongReduce:gc.time             avgt    5    85,000                ms

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

    static Song fullSongPrimitive(PlayList playList) {
      var duration = (short) 0;
      for (var i = 0; i < playList.size(); i++) {
        duration += playList.get(i).duration;
      }
      return new Song(playList.title, duration);
    }

    static Song fullSongObject(PlayList playList) {
      var fullSong = new Song(playList.title, (short) 0);
      for (var i = 0; i < playList.size(); i++) {
        fullSong = new Song(fullSong.title,
            (short) (fullSong.duration + playList.get(i).duration));
      }
      return fullSong;
    }

    static Song fullSongReduce(PlayList playList) {
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

    static Song fullSongPrimitive(PlayList playList) {
      var duration = (short) 0;
      for (var i = 0; i < playList.size(); i++) {
        duration += playList.get(i).duration;
      }
      return new Song(playList.title, duration);
    }

    static Song fullSongObject(PlayList playList) {
      var fullSong = new Song(playList.title, (short) 0);
      for (var i = 0; i < playList.size(); i++) {
        fullSong = new Song(fullSong.title,
            (short) (fullSong.duration + playList.get(i).duration));
      }
      return fullSong;
    }

    static Song fullSongReduce(PlayList playList) {
      return Arrays.stream(playList.songs)
          .reduce(new Song(playList.title, (short) 0),
              (a, b) -> new Song(playList.title, (short) (a.duration + b.duration)));
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
  public Value.Song valueFullSongPrimitive() {
    return Value.fullSongPrimitive(valuePlayList);
  }

  @Benchmark
  public Value.Song valueFullSongObject() {
    return Value.fullSongObject(valuePlayList);
  }

  @Benchmark
  public Value.Song valueFullSongReduce() {
    return Value.fullSongReduce(valuePlayList);
  }

  @Benchmark
  public Value.Song valueNoFlatteningFullSongObject() {
    return Value.fullSongObject(valueNoFlatteningPlayList);
  }

  @Benchmark
  public Value.Song valueNoFlatteningFullSongReduce() {
    return Value.fullSongReduce(valueNoFlatteningPlayList);
  }

  private final Identity.PlayList identityPlayList = new Identity.PlayList("80s", IntStream.range(0, 100)
      .mapToObj(i -> new Identity.Song("Song " + i, (short) i))
      .toArray(Identity.Song[]::new));

  @Benchmark
  public Identity.Song identityFullSongPrimitive() {
    return Identity.fullSongPrimitive(identityPlayList);
  }

  @Benchmark
  public Identity.Song identityFullSongObject() {
    return Identity.fullSongObject(identityPlayList);
  }

  @Benchmark
  public Identity.Song identityFullSongReduce() {
    return Identity.fullSongReduce(identityPlayList);
  }
}

