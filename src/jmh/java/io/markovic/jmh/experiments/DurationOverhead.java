package io.markovic.jmh.experiments;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

// You MUST run this with `-prof GC` on the command line to see GC
// statistics; without those, the results are useless (we don't care about
// time in these benchmarks, only GC utilization).
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@Fork(2)
public class DurationOverhead {
  private static long getMillis() {
    return ThreadLocalRandom.current().nextLong(1, 500);
  }

  private static Duration getDuration() {
    return Duration.ofMillis(getMillis());
  }

  private static void useRawMillis(long millis, Blackhole blackhole) {
    blackhole.consume(millis);
  }

  private static void useDuration(Duration duration, Blackhole blackhole) {
    useRawMillis(duration.toMillis(), blackhole);
  }

  private static void useDuration2(Duration duration, Blackhole blackhole) {
    useDuration(duration, blackhole);
  }

  private static void useDuration3(Duration duration, Blackhole blackhole) {
    useDuration2(duration, blackhole);
  }

  private static void useDuration4(Duration duration, Blackhole blackhole) {
    useDuration3(duration, blackhole);
  }

  private static void useDuration5(Duration duration, Blackhole blackhole) {
    useDuration4(duration, blackhole);
  }

  private static void useDuration6(Duration duration, Blackhole blackhole) {
    useDuration5(duration, blackhole);
  }

  private static void useDuration7(Duration duration, Blackhole blackhole) {
    useDuration6(duration, blackhole);
  }

  private static void useDuration8(Duration duration, Blackhole blackhole) {
    useDuration7(duration, blackhole);
  }

  private static void useDuration9(Duration duration, Blackhole blackhole) {
    useDuration8(duration, blackhole);
  }

  // No Duration object created anywhere, just a plain long being blackholed.
  // Won't create any objects and thus won't allocated. This is our baseline.
  @Benchmark
  public void noDuration(Blackhole blackhole) {
    useRawMillis(getMillis(), blackhole);
  }

  // Creates one Duration object in the benchmark function which is then
  // handed off to one function which extracts millis and blackholes the long.
  // Does this get escape-analyzed and thus avoids heap allocations?
  @Benchmark
  public void withDuration(Blackhole blackhole) {
    Duration duration = Duration.ofMillis(getMillis());
    useDuration(duration, blackhole);
  }

  // Creates one Duration object in the benchmark function which is then
  // passed through FIVE function calls before millis extracted and blackholed.
  // This should make escape-analysis harder for the JVM. Does this avoid heap
  // allocations?
  @Benchmark
  public void withDurationStack5(Blackhole blackhole) {
    Duration duration = Duration.ofMillis(getMillis());
    useDuration5(duration, blackhole);
  }

  // Creates one Duration object in the benchmark function which is then
  // passed through NINE function calls before millis extracted and blackholed.
  // This should make escape-analysis MUCH harder for the JVM. Does this avoid
  // heap allocations?
  @Benchmark
  public void withDurationStack9(Blackhole blackhole) {
    Duration duration = Duration.ofMillis(getMillis());
    useDuration9(duration, blackhole);
  }

  // Creates one Duration object in a separate function that returns it to the
  // benchmark function! This is then directly passed to a function
  // that extracts and blackholes millis. This should also make escape-analysis
  // harder for the JVM, but in a different way. Does this avoid heap
  // allocations?
  @Benchmark
  public void withDurationCreatedElsewhere(Blackhole blackhole) {
    useDuration(getDuration(), blackhole);
  }

  // Duration is created and blackholed DIRECTLY. No escape-analysis can
  // happen and the object MUST be allocated on the heap.
  @Benchmark
  public void control(Blackhole blackhole) {
    Duration duration = Duration.ofMillis(getMillis());
    blackhole.consume(duration);
  }

  // RESULTS! (When run with `-prof gc`)
  //
  // Benchmark                                                             Mode  Cnt     Score    Error   Units
  // DurationOverhead.control                                              avgt   10     9.722 ±  0.288   ns/op
  // DurationOverhead.control:·gc.alloc.rate                               avgt   10  1569.234 ± 44.836  MB/sec
  // DurationOverhead.control:·gc.alloc.rate.norm                          avgt   10    24.000 ±  0.001    B/op
  // DurationOverhead.control:·gc.churn.PS_Eden_Space                      avgt   10  1586.404 ± 85.873  MB/sec
  // DurationOverhead.control:·gc.churn.PS_Eden_Space.norm                 avgt   10    24.261 ±  1.040    B/op
  // DurationOverhead.control:·gc.churn.PS_Survivor_Space                  avgt   10     0.085 ±  0.050  MB/sec
  // DurationOverhead.control:·gc.churn.PS_Survivor_Space.norm             avgt   10     0.001 ±  0.001    B/op
  // DurationOverhead.control:·gc.count                                    avgt   10   142.000           counts
  // DurationOverhead.control:·gc.time                                     avgt   10    71.000               ms
  // DurationOverhead.noDuration                                           avgt   10     5.400 ±  0.256   ns/op
  // DurationOverhead.noDuration:·gc.alloc.rate                            avgt   10    ≈ 10⁻⁴           MB/sec
  // DurationOverhead.noDuration:·gc.alloc.rate.norm                       avgt   10    ≈ 10⁻⁶             B/op
  // DurationOverhead.noDuration:·gc.count                                 avgt   10       ≈ 0           counts
  // DurationOverhead.withDuration                                         avgt   10     9.900 ±  0.405   ns/op
  // DurationOverhead.withDuration:·gc.alloc.rate                          avgt   10    ≈ 10⁻⁴           MB/sec
  // DurationOverhead.withDuration:·gc.alloc.rate.norm                     avgt   10    ≈ 10⁻⁵             B/op
  // DurationOverhead.withDuration:·gc.count                               avgt   10       ≈ 0           counts
  // DurationOverhead.withDurationCreatedElsewhere                         avgt   10     9.851 ±  0.396   ns/op
  // DurationOverhead.withDurationCreatedElsewhere:·gc.alloc.rate          avgt   10    ≈ 10⁻⁴           MB/sec
  // DurationOverhead.withDurationCreatedElsewhere:·gc.alloc.rate.norm     avgt   10    ≈ 10⁻⁵             B/op
  // DurationOverhead.withDurationCreatedElsewhere:·gc.count               avgt   10       ≈ 0           counts
  // DurationOverhead.withDurationStack5                                   avgt   10    10.254 ±  0.388   ns/op
  // DurationOverhead.withDurationStack5:·gc.alloc.rate                    avgt   10    ≈ 10⁻⁴           MB/sec
  // DurationOverhead.withDurationStack5:·gc.alloc.rate.norm               avgt   10    ≈ 10⁻⁵             B/op
  // DurationOverhead.withDurationStack5:·gc.count                         avgt   10       ≈ 0           counts
  // DurationOverhead.withDurationStack9                                   avgt   10    15.344 ±  0.508   ns/op
  // DurationOverhead.withDurationStack9:·gc.alloc.rate                    avgt   10   994.606 ± 32.400  MB/sec
  // DurationOverhead.withDurationStack9:·gc.alloc.rate.norm               avgt   10    24.000 ±  0.001    B/op
  // DurationOverhead.withDurationStack9:·gc.churn.PS_Eden_Space           avgt   10  1002.115 ± 67.472  MB/sec
  // DurationOverhead.withDurationStack9:·gc.churn.PS_Eden_Space.norm      avgt   10    24.175 ±  1.129    B/op
  // DurationOverhead.withDurationStack9:·gc.churn.PS_Survivor_Space       avgt   10     0.075 ±  0.040  MB/sec
  // DurationOverhead.withDurationStack9:·gc.churn.PS_Survivor_Space.norm  avgt   10     0.002 ±  0.001    B/op
  // DurationOverhead.withDurationStack9:·gc.count                         avgt   10   134.000           counts
  // DurationOverhead.withDurationStack9:·gc.time                          avgt   10    67.000               ms
  //
  // The interesting line to look at for each benchmark is gc.alloc.rate.norm
  // which shows the heap allocation rate per benchmark iteration.
  // You can see that there's NO garbage being created in any benchmarks
  // except two: the control and the Stack9 version of the benchmark. So
  // escape-analysis is smart enough to inline the Duration over 5 stack
  // frames, and even if the Duration is first created in one function,
  // passed up the stack, and then passed to another function. But
  // naturally, every optimization has a limit (as Stack9 shows).
}
