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
  public Instant time4jClock() {
    return time4JClock.instant();
  }

  @Benchmark
  public Instant systemClock() {
    return systemClock.instant();
  }

  // GC RESULTS! (When run with `-prof gc`)
  //
  // Benchmark                                                        Mode  Cnt    Score     Error   Units
  // Time4JMonoticClock.systemClock                                   avgt   10   49.077 ±   9.356   ns/op
  // Time4JMonoticClock.systemClock:·gc.alloc.rate                    avgt   10  314.980 ±  55.340  MB/sec
  // Time4JMonoticClock.systemClock:·gc.alloc.rate.norm               avgt   10   24.000 ±   0.001    B/op
  // Time4JMonoticClock.systemClock:·gc.churn.PS_Eden_Space           avgt   10  329.160 ±  97.821  MB/sec
  // Time4JMonoticClock.systemClock:·gc.churn.PS_Eden_Space.norm      avgt   10   24.975 ±   4.580    B/op
  // Time4JMonoticClock.systemClock:·gc.churn.PS_Survivor_Space       avgt   10    0.187 ±   0.116  MB/sec
  // Time4JMonoticClock.systemClock:·gc.churn.PS_Survivor_Space.norm  avgt   10    0.014 ±   0.008    B/op
  // Time4JMonoticClock.systemClock:·gc.count                         avgt   10   25.000            counts
  // Time4JMonoticClock.systemClock:·gc.time                          avgt   10   38.000                ms
  // Time4JMonoticClock.time4jClock                                   avgt   10   59.468 ±   9.524   ns/op
  // Time4JMonoticClock.time4jClock:·gc.alloc.rate                    avgt   10  517.813 ±  75.589  MB/sec
  // Time4JMonoticClock.time4jClock:·gc.alloc.rate.norm               avgt   10   48.000 ±   0.001    B/op
  // Time4JMonoticClock.time4jClock:·gc.churn.PS_Eden_Space           avgt   10  538.022 ± 113.730  MB/sec
  // Time4JMonoticClock.time4jClock:·gc.churn.PS_Eden_Space.norm      avgt   10   49.790 ±   6.463    B/op
  // Time4JMonoticClock.time4jClock:·gc.churn.PS_Survivor_Space       avgt   10    0.210 ±   0.238  MB/sec
  // Time4JMonoticClock.time4jClock:·gc.churn.PS_Survivor_Space.norm  avgt   10    0.019 ±   0.020    B/op
  // Time4JMonoticClock.time4jClock:·gc.count                         avgt   10   49.000            counts
  // Time4JMonoticClock.time4jClock:·gc.time                          avgt   10   67.000                ms
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
}
