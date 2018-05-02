package io.markovic.jmh.experiments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.RandomStringUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@State(Scope.Thread)
@Fork(2)
public class Streams {
  @Param({"4", "20", "1000"})
  public int numKeys;

  private Map<String, List<String>> data = new HashMap<>(numKeys);

  @Setup
  public void setup() {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    for (int i = 0; i < numKeys; i++) {
      List<String> list = new ArrayList<>(20);
      for (int j = 0; j < random.nextInt(20, 50); j++) {
        list.add(RandomStringUtils.random(random.nextInt(5, 10)));
      }
      data.put(RandomStringUtils.random(10), list);
    }
  }

  @Benchmark
  public int withStreams() {
    return data.values()
               .stream()
               .mapToInt(List::size)
               .sum();
  }

  @Benchmark
  public int withIterator() {
    int sum = 0;
    for (List<String> list : data.values()) {
      sum += list.size();
    }
    return sum;
  }

  // RESULTS! (When run with `-prof gc`)
  //
  //  Benchmark                                             (numKeys)  Mode  Cnt      Score      Error   Units
  //  Streams.withIterator                                          4  avgt   10     21.863 ±    2.012   ns/op
  //  Streams.withIterator:·gc.alloc.rate                           4  avgt   10     ≈ 10⁻⁴             MB/sec
  //  Streams.withIterator:·gc.alloc.rate.norm                      4  avgt   10     ≈ 10⁻⁵               B/op
  //  Streams.withIterator:·gc.count                                4  avgt   10        ≈ 0             counts
  //  Streams.withIterator                                         20  avgt   10     66.355 ±    6.878   ns/op
  //  Streams.withIterator:·gc.alloc.rate                          20  avgt   10     ≈ 10⁻⁴             MB/sec
  //  Streams.withIterator:·gc.alloc.rate.norm                     20  avgt   10     ≈ 10⁻⁵               B/op
  //  Streams.withIterator:·gc.count                               20  avgt   10        ≈ 0             counts
  //  Streams.withIterator                                       1000  avgt   10   4220.107 ±  485.298   ns/op
  //  Streams.withIterator:·gc.alloc.rate                        1000  avgt   10     ≈ 10⁻⁴             MB/sec
  //  Streams.withIterator:·gc.alloc.rate.norm                   1000  avgt   10      0.002 ±    0.001    B/op
  //  Streams.withIterator:·gc.count                             1000  avgt   10        ≈ 0             counts
  //  Streams.withStreams                                           4  avgt   10     66.328 ±    1.318   ns/op
  //  Streams.withStreams:·gc.alloc.rate                            4  avgt   10   2223.591 ±   44.065  MB/sec
  //  Streams.withStreams:·gc.alloc.rate.norm                       4  avgt   10    232.000 ±    0.001    B/op
  //  Streams.withStreams:·gc.churn.PS_Eden_Space                   4  avgt   10   2298.034 ±  395.648  MB/sec
  //  Streams.withStreams:·gc.churn.PS_Eden_Space.norm              4  avgt   10    239.878 ±   42.587    B/op
  //  Streams.withStreams:·gc.churn.PS_Survivor_Space               4  avgt   10      0.108 ±    0.127  MB/sec
  //  Streams.withStreams:·gc.churn.PS_Survivor_Space.norm          4  avgt   10      0.011 ±    0.013    B/op
  //  Streams.withStreams:·gc.count                                 4  avgt   10     28.000             counts
  //  Streams.withStreams:·gc.time                                  4  avgt   10     38.000                 ms
  //  Streams.withStreams                                          20  avgt   10    179.163 ±   29.395   ns/op
  //  Streams.withStreams:·gc.alloc.rate                           20  avgt   10    888.173 ±  129.228  MB/sec
  //  Streams.withStreams:·gc.alloc.rate.norm                      20  avgt   10    248.000 ±    0.001    B/op
  //  Streams.withStreams:·gc.churn.PS_Eden_Space                  20  avgt   10    911.828 ±  216.359  MB/sec
  //  Streams.withStreams:·gc.churn.PS_Eden_Space.norm             20  avgt   10    254.250 ±   42.265    B/op
  //  Streams.withStreams:·gc.churn.PS_Survivor_Space              20  avgt   10      0.127 ±    0.091  MB/sec
  //  Streams.withStreams:·gc.churn.PS_Survivor_Space.norm         20  avgt   10      0.036 ±    0.024    B/op
  //  Streams.withStreams:·gc.count                                20  avgt   10     55.000             counts
  //  Streams.withStreams:·gc.time                                 20  avgt   10     71.000                 ms
  //  Streams.withStreams                                        1000  avgt   10  12090.225 ± 1128.039   ns/op
  //  Streams.withStreams:·gc.alloc.rate                         1000  avgt   10     13.079 ±    1.087  MB/sec
  //  Streams.withStreams:·gc.alloc.rate.norm                    1000  avgt   10    248.005 ±    0.001    B/op
  //  Streams.withStreams:·gc.count                              1000  avgt   10        ≈ 0             counts
  //
  // The interesting line to look at for each benchmark is gc.alloc.rate.norm
  // which shows the heap allocation rate per benchmark iteration.
  // Streams does allocate, but at 248 bytes, it's not a huge amount. Don't
  // call it in a loop though.
  // Perf-wise, the raw for-each loop is clearly ~3x faster, and consistently
  // so. Even as the number of elements increase, the perf advantage remains.
}
