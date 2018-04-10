package io.markovic.jmh.experiments;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
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
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@Fork(2)
public class InstantOverhead {
  private static final Clock clock = Clock.systemUTC();

  @Benchmark
  public void rawMillis(Blackhole blackhole) throws InterruptedException {
    long start = System.currentTimeMillis();
    Thread.sleep(2);
    blackhole.consume(System.currentTimeMillis() - start);
  }

  // Uses Instants and Durations to measure time; does this allocate?
  @Benchmark
  public void withInstant(Blackhole blackhole) throws InterruptedException {
    Instant start = Instant.now();
    Thread.sleep(2);
    blackhole.consume(Duration.between(start, Instant.now()).toMillis());
  }

  // Instant.now() actually allocates a Clock internally every time it's
  // called, so let's see what happens if we avoid that.
  @Benchmark
  public void withClock(Blackhole blackhole) throws InterruptedException {
    Instant start = clock.instant();
    Thread.sleep(2);
    blackhole.consume(Duration.between(start, clock.instant()).toMillis());
  }

  // This definitely allocates at least the Duration object because it's sent
  // into a Blackhole.
  @Benchmark
  public void control(Blackhole blackhole) throws InterruptedException {
    Instant start = Instant.now();
    Thread.sleep(2);
    blackhole.consume(Duration.between(start, Instant.now()));  // NO toMillis!
  }

  @Benchmark
  public void instantCreationWithoutClock(Blackhole blackhole) {
    Instant instant = Instant.ofEpochMilli(
        ThreadLocalRandom.current().nextInt(1, 1000));
    useInstant(instant, blackhole);
  }

  private void useInstant(Instant instant, Blackhole blackhole) {
    blackhole.consume(instant.toEpochMilli());
  }

  // RESULTS! (When run with `-prof gc`)
  //
  // Benchmark                                                        Mode  Cnt    Score    Error   Units
  // InstantOverhead.control                                          avgt   10    2.109 ±  0.005   ms/op
  // InstantOverhead.control:·gc.alloc.rate                           avgt   10    0.032 ±  0.001  MB/sec
  // InstantOverhead.control:·gc.alloc.rate.norm                      avgt   10  105.167 ±  0.714    B/op
  // InstantOverhead.control:·gc.count                                avgt   10      ≈ 0           counts
  // InstantOverhead.instantCreationWithoutClock                      avgt   10   ≈ 10⁻⁵            ms/op
  // InstantOverhead.instantCreationWithoutClock:·gc.alloc.rate       avgt   10   ≈ 10⁻⁴           MB/sec
  // InstantOverhead.instantCreationWithoutClock:·gc.alloc.rate.norm  avgt   10   ≈ 10⁻⁵             B/op
  // InstantOverhead.instantCreationWithoutClock:·gc.count            avgt   10      ≈ 0           counts
  // InstantOverhead.rawMillis                                        avgt   10    2.109 ±  0.003   ms/op
  // InstantOverhead.rawMillis:·gc.alloc.rate                         avgt   10   ≈ 10⁻⁴           MB/sec
  // InstantOverhead.rawMillis:·gc.alloc.rate.norm                    avgt   10    0.917 ±  0.033    B/op
  // InstantOverhead.rawMillis:·gc.count                              avgt   10      ≈ 0           counts
  // InstantOverhead.withClock                                        avgt   10    2.107 ±  0.010   ms/op
  // InstantOverhead.withClock:·gc.alloc.rate                         avgt   10    0.022 ±  0.001  MB/sec
  // InstantOverhead.withClock:·gc.alloc.rate.norm                    avgt   10   73.145 ±  0.724    B/op
  // InstantOverhead.withClock:·gc.count                              avgt   10      ≈ 0           counts
  // InstantOverhead.withInstant                                      avgt   10    2.108 ±  0.004   ms/op
  // InstantOverhead.withInstant:·gc.alloc.rate                       avgt   10    0.032 ±  0.001  MB/sec
  // InstantOverhead.withInstant:·gc.alloc.rate.norm                  avgt   10  105.159 ±  0.743    B/op
  // InstantOverhead.withInstant:·gc.count                            avgt   10      ≈ 0           counts
  //
  // The interesting line to look at for each benchmark is gc.alloc.rate.norm
  // which shows the heap allocation rate per benchmark iteration.
  // Using Instants & Durations to measure time sadly allocates... but
  // creating an Instant "manually" won't, much like with Duration (see
  // DurationOverhead.java).
}
