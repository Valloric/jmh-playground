package io.markovic.jmh.experiments;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import net.time4j.SystemClock;
import net.time4j.TemporalType;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@State(Scope.Thread)
@Fork(2)
public class Time4JMonoticClock {
  // Yes, the time4j clock is monotonic while the system one isn't. It's not
  // an apples-to-apples comparison, but it's exactly the comparison we want
  // to make! (That is, what's the cost of using the monotonic clock?)
  private Clock time4JClock = TemporalType.CLOCK.from(SystemClock.MONOTONIC);
  private Clock systemClock = Clock.systemUTC();

  @Benchmark
  public Instant time4jClockWithInstant() {
    return time4JClock.instant();
  }

  @Benchmark
  public Instant systemClockWithInstant() {
    return systemClock.instant();
  }

  @Benchmark
  public long time4jClockRawMillis() {
    return time4JClock.millis();
  }

  @Benchmark
  public long systemClockRawMillis() {
    return systemClock.millis();
  }

  // GC RESULTS! (When run with `-prof gc`)
  //
  // Benchmark                                                                   Mode  Cnt    Score    Error   Units
  // Time4JMonoticClock.systemClockRawMillis                                     avgt   10   34.354 ±  1.621   ns/op
  // Time4JMonoticClock.systemClockRawMillis:·gc.alloc.rate                      avgt   10   ≈ 10⁻⁴           MB/sec
  // Time4JMonoticClock.systemClockRawMillis:·gc.alloc.rate.norm                 avgt   10   ≈ 10⁻⁵             B/op
  // Time4JMonoticClock.systemClockRawMillis:·gc.count                           avgt   10      ≈ 0           counts
  // Time4JMonoticClock.systemClockWithInstant                                   avgt   10   45.255 ±  5.272   ns/op
  // Time4JMonoticClock.systemClockWithInstant:·gc.alloc.rate                    avgt   10  338.761 ± 36.662  MB/sec
  // Time4JMonoticClock.systemClockWithInstant:·gc.alloc.rate.norm               avgt   10   24.000 ±  0.001    B/op
  // Time4JMonoticClock.systemClockWithInstant:·gc.churn.PS_Eden_Space           avgt   10  356.308 ± 87.294  MB/sec
  // Time4JMonoticClock.systemClockWithInstant:·gc.churn.PS_Eden_Space.norm      avgt   10   25.110 ±  4.133    B/op
  // Time4JMonoticClock.systemClockWithInstant:·gc.churn.PS_Survivor_Space       avgt   10    0.212 ±  0.071  MB/sec
  // Time4JMonoticClock.systemClockWithInstant:·gc.churn.PS_Survivor_Space.norm  avgt   10    0.015 ±  0.006    B/op
  // Time4JMonoticClock.systemClockWithInstant:·gc.count                         avgt   10   28.000           counts
  // Time4JMonoticClock.systemClockWithInstant:·gc.time                          avgt   10   42.000               ms
  // Time4JMonoticClock.time4jClockRawMillis                                     avgt   10   58.347 ±  8.978   ns/op
  // Time4JMonoticClock.time4jClockRawMillis:·gc.alloc.rate                      avgt   10  263.534 ± 34.303  MB/sec
  // Time4JMonoticClock.time4jClockRawMillis:·gc.alloc.rate.norm                 avgt   10   24.000 ±  0.001    B/op
  // Time4JMonoticClock.time4jClockRawMillis:·gc.churn.PS_Eden_Space             avgt   10  269.663 ± 75.456  MB/sec
  // Time4JMonoticClock.time4jClockRawMillis:·gc.churn.PS_Eden_Space.norm        avgt   10   24.608 ±  7.150    B/op
  // Time4JMonoticClock.time4jClockRawMillis:·gc.churn.PS_Survivor_Space         avgt   10    0.077 ±  0.136  MB/sec
  // Time4JMonoticClock.time4jClockRawMillis:·gc.churn.PS_Survivor_Space.norm    avgt   10    0.007 ±  0.014    B/op
  // Time4JMonoticClock.time4jClockRawMillis:·gc.count                           avgt   10   19.000           counts
  // Time4JMonoticClock.time4jClockRawMillis:·gc.time                            avgt   10   30.000               ms
  // Time4JMonoticClock.time4jClockWithInstant                                   avgt   10   59.870 ±  6.797   ns/op
  // Time4JMonoticClock.time4jClockWithInstant:·gc.alloc.rate                    avgt   10  512.263 ± 59.488  MB/sec
  // Time4JMonoticClock.time4jClockWithInstant:·gc.alloc.rate.norm               avgt   10   48.000 ±  0.001    B/op
  // Time4JMonoticClock.time4jClockWithInstant:·gc.churn.PS_Eden_Space           avgt   10  514.041 ± 86.099  MB/sec
  // Time4JMonoticClock.time4jClockWithInstant:·gc.churn.PS_Eden_Space.norm      avgt   10   48.180 ±  6.101    B/op
  // Time4JMonoticClock.time4jClockWithInstant:·gc.churn.PS_Survivor_Space       avgt   10    0.115 ±  0.112  MB/sec
  // Time4JMonoticClock.time4jClockWithInstant:·gc.churn.PS_Survivor_Space.norm  avgt   10    0.011 ±  0.011    B/op
  // Time4JMonoticClock.time4jClockWithInstant:·gc.count                         avgt   10   51.000           counts
  // Time4JMonoticClock.time4jClockWithInstant:·gc.time                          avgt   10   72.000               ms
  //
  // We can see that speed-wise, the system clock and the time4j clock are
  // pretty much the same. Time4j is a bit slower, but not appreciably.
  //
  // GC-wise, the interesting line to look at for each benchmark is
  // gc.alloc.rate.norm which shows the heap allocation rate per benchmark
  // iteration.
  // Time4j allocates 48 bytes per iteration while the system clock allocates
  // 24 bytes per iteration. That's twice as much, but probably not an amount
  // you'd care about.
  // Interestingly, reading raw millis from the time4j clock still allocates
  // (unlike doing the same with the system clock).
}
