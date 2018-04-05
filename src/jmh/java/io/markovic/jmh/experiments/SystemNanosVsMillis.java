package io.markovic.jmh.experiments;

import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@Fork(2)
public class SystemNanosVsMillis {
  @Benchmark
  public long currentTimeMillis() {
    return System.currentTimeMillis();
  }

  @Benchmark
  public long nanoTime() {
    return System.nanoTime();
  }

  // RESULTS!
  //
  // SystemNanosVsMillis.currentTimeMillis  avgt   10  20.649 ± 0.042  ns/op
  // SystemNanosVsMillis.nanoTime           avgt   10  19.191 ± 0.061  ns/op
  //
  // This was measured on a system running Linux 4.9. Seems like the concern
  // that System.nanoTime() is much slower than currentTimeMillis is unfounded!
  // (Well, at least on Linux.)
}
