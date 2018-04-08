package io.markovic.jmh.experiments;

import java.util.ArrayList;
import java.util.List;
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
import org.openjdk.jmh.infra.Blackhole;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@State(Scope.Benchmark)
@Fork(2)
public class IteratorGC {
  @Param({"4", "10", "20", "50", "1000"})
  public int numStrings;

  List<String> strings;

  @Setup
  public void setup() {
    strings = new ArrayList<>(numStrings);
    for (int i = 0; i < numStrings; i++) {
      strings.add(RandomStringUtils.random(10));
    }
  }

  @Benchmark
  public void rawForLoop(Blackhole blackhole) {
    for (int i = 0; i < strings.size(); i++) {
      blackhole.consume(strings.get(i));
    }
  }

  @Benchmark
  public void forEachLoop(Blackhole blackhole) {
    for (String s : strings) {
      blackhole.consume(s);
    }
  }

  // RESULTS! (When run with `-prof gc`)
  //
  // Benchmark                                   (numItems)  Mode  Cnt     Score     Error   Units
  // IteratorGC.forEachLoop                                 4  avgt   10    19.030 ±   0.386   ns/op
  // IteratorGC.forEachLoop:·gc.alloc.rate                  4  avgt   10    ≈ 10⁻⁴            MB/sec
  // IteratorGC.forEachLoop:·gc.alloc.rate.norm             4  avgt   10    ≈ 10⁻⁵              B/op
  // IteratorGC.forEachLoop:·gc.count                       4  avgt   10       ≈ 0            counts
  // IteratorGC.forEachLoop                                10  avgt   10    50.100 ±   1.497   ns/op
  // IteratorGC.forEachLoop:·gc.alloc.rate                 10  avgt   10    ≈ 10⁻⁴            MB/sec
  // IteratorGC.forEachLoop:·gc.alloc.rate.norm            10  avgt   10    ≈ 10⁻⁵              B/op
  // IteratorGC.forEachLoop:·gc.count                      10  avgt   10       ≈ 0            counts
  // IteratorGC.forEachLoop                                20  avgt   10    97.925 ±   2.449   ns/op
  // IteratorGC.forEachLoop:·gc.alloc.rate                 20  avgt   10    ≈ 10⁻⁴            MB/sec
  // IteratorGC.forEachLoop:·gc.alloc.rate.norm            20  avgt   10    ≈ 10⁻⁴              B/op
  // IteratorGC.forEachLoop:·gc.count                      20  avgt   10       ≈ 0            counts
  // IteratorGC.forEachLoop                                50  avgt   10   253.550 ±  13.140   ns/op
  // IteratorGC.forEachLoop:·gc.alloc.rate                 50  avgt   10    ≈ 10⁻⁴            MB/sec
  // IteratorGC.forEachLoop:·gc.alloc.rate.norm            50  avgt   10    ≈ 10⁻⁴              B/op
  // IteratorGC.forEachLoop:·gc.count                      50  avgt   10       ≈ 0            counts
  // IteratorGC.forEachLoop                              1000  avgt   10  5065.450 ± 149.429   ns/op
  // IteratorGC.forEachLoop:·gc.alloc.rate               1000  avgt   10    ≈ 10⁻⁴            MB/sec
  // IteratorGC.forEachLoop:·gc.alloc.rate.norm          1000  avgt   10     0.002 ±   0.001    B/op
  // IteratorGC.forEachLoop:·gc.count                    1000  avgt   10       ≈ 0            counts
  // IteratorGC.rawForLoop                                  4  avgt   10    17.050 ±   0.407   ns/op
  // IteratorGC.rawForLoop:·gc.alloc.rate                   4  avgt   10    ≈ 10⁻⁴            MB/sec
  // IteratorGC.rawForLoop:·gc.alloc.rate.norm              4  avgt   10    ≈ 10⁻⁵              B/op
  // IteratorGC.rawForLoop:·gc.count                        4  avgt   10       ≈ 0            counts
  // IteratorGC.rawForLoop                                 10  avgt   10    41.231 ±   1.307   ns/op
  // IteratorGC.rawForLoop:·gc.alloc.rate                  10  avgt   10    ≈ 10⁻⁴            MB/sec
  // IteratorGC.rawForLoop:·gc.alloc.rate.norm             10  avgt   10    ≈ 10⁻⁵              B/op
  // IteratorGC.rawForLoop:·gc.count                       10  avgt   10       ≈ 0            counts
  // IteratorGC.rawForLoop                                 20  avgt   10    82.340 ±   3.602   ns/op
  // IteratorGC.rawForLoop:·gc.alloc.rate                  20  avgt   10    ≈ 10⁻⁴            MB/sec
  // IteratorGC.rawForLoop:·gc.alloc.rate.norm             20  avgt   10    ≈ 10⁻⁴              B/op
  // IteratorGC.rawForLoop:·gc.count                       20  avgt   10       ≈ 0            counts
  // IteratorGC.rawForLoop                                 50  avgt   10   212.377 ±   4.300   ns/op
  // IteratorGC.rawForLoop:·gc.alloc.rate                  50  avgt   10    ≈ 10⁻⁴            MB/sec
  // IteratorGC.rawForLoop:·gc.alloc.rate.norm             50  avgt   10    ≈ 10⁻⁴              B/op
  // IteratorGC.rawForLoop:·gc.count                       50  avgt   10       ≈ 0            counts
  // IteratorGC.rawForLoop                               1000  avgt   10  3960.977 ±  44.419   ns/op
  // IteratorGC.rawForLoop:·gc.alloc.rate                1000  avgt   10    ≈ 10⁻⁴            MB/sec
  // IteratorGC.rawForLoop:·gc.alloc.rate.norm           1000  avgt   10     0.002 ±   0.001    B/op
  // IteratorGC.rawForLoop:·gc.count                     1000  avgt   10       ≈ 0            counts
  //
  // The interesting line to look at for each benchmark is gc.alloc.rate.norm
  // which shows the heap allocation rate per benchmark iteration.
  // NONE OF THE BENCHMARK RUNS ALLOCATE! None whatsoever. The iterator
  // _always_ gets escape-analyzed away by the JVM; that's very unsurprising
  // gives that removing iterator GC overhead was one of the primary reasons
  // why escape analysis was added to the JVM!
  // DON'T BE FOOLED BY THE TIMING RESULTS! Using blackhole.consume prevents
  // clever loop optimizations the JVM can perform, like iteration fusing,
  // loop unrolling etc.
  // We use this benchmark strictly to test GC overhead. To see perf overhead
  // of iterators, head over to IteratorPerf.java.
}
