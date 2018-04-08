package io.markovic.jmh.experiments;

import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@Fork(2)
public class VarArgsOverhead {
  private static String TEST_STRING1 = "foo";
  private static String TEST_STRING2 = "bar";
  private static String TEST_STRING3 = "goo";

  private void threeParams(Blackhole blackhole,
                           String s1, String s2, String s3) {
    blackhole.consume(s1);
    blackhole.consume(s2);
    blackhole.consume(s3);
  }

  private void threeParamsVarArgs(Blackhole blackhole,
                                  String... strings) {
    blackhole.consume(strings[0]);
    blackhole.consume(strings[1]);
    blackhole.consume(strings[2]);
  }

  private void NParamsVarArgs(Blackhole blackhole,
                              String... strings) {
    for (int i = 0; i < strings.length; i++) {
      blackhole.consume(strings[i]);
    }
  }

  @Benchmark
  public void noVarArgs(Blackhole blackhole) {
    threeParams(blackhole, TEST_STRING1, TEST_STRING2, TEST_STRING3);
  }

  @Benchmark
  public void withVarArgsFixed(Blackhole blackhole) {
    threeParamsVarArgs(blackhole, TEST_STRING1, TEST_STRING2, TEST_STRING3);
  }

  @Benchmark
  public void withVarArgsDyn(Blackhole blackhole) {
    NParamsVarArgs(blackhole, TEST_STRING1, TEST_STRING2, TEST_STRING3);
  }

  // RESULTS! (When run with `-prof gc`)
  //
  // Benchmark                                                        Mode  Cnt     Score    Error   Units
  // VarArgsOverhead.noVarArgs                                        avgt   10     7.661 ±  0.233   ns/op
  // VarArgsOverhead.noVarArgs:·gc.alloc.rate                         avgt   10    ≈ 10⁻⁴           MB/sec
  // VarArgsOverhead.noVarArgs:·gc.alloc.rate.norm                    avgt   10    ≈ 10⁻⁵             B/op
  // VarArgsOverhead.noVarArgs:·gc.count                              avgt   10       ≈ 0           counts
  // VarArgsOverhead.withVarArgsDyn                                   avgt   10    10.383 ±  0.063   ns/op
  // VarArgsOverhead.withVarArgsDyn:·gc.alloc.rate                    avgt   10  1958.857 ± 11.925  MB/sec
  // VarArgsOverhead.withVarArgsDyn:·gc.alloc.rate.norm               avgt   10    32.000 ±  0.001    B/op
  // VarArgsOverhead.withVarArgsDyn:·gc.churn.PS_Eden_Space           avgt   10  1956.343 ± 70.947  MB/sec
  // VarArgsOverhead.withVarArgsDyn:·gc.churn.PS_Eden_Space.norm      avgt   10    31.959 ±  1.152    B/op
  // VarArgsOverhead.withVarArgsDyn:·gc.churn.PS_Survivor_Space       avgt   10     0.098 ±  0.070  MB/sec
  // VarArgsOverhead.withVarArgsDyn:·gc.churn.PS_Survivor_Space.norm  avgt   10     0.002 ±  0.001    B/op
  // VarArgsOverhead.withVarArgsDyn:·gc.count                         avgt   10   195.000           counts
  // VarArgsOverhead.withVarArgsDyn:·gc.time                          avgt   10    95.000               ms
  // VarArgsOverhead.withVarArgsFixed                                 avgt   10     7.635 ±  0.021   ns/op
  // VarArgsOverhead.withVarArgsFixed:·gc.alloc.rate                  avgt   10    ≈ 10⁻⁴           MB/sec
  // VarArgsOverhead.withVarArgsFixed:·gc.alloc.rate.norm             avgt   10    ≈ 10⁻⁵             B/op
  // VarArgsOverhead.withVarArgsFixed:·gc.count                       avgt   10       ≈ 0           counts
  //
  // The interesting line to look at for each benchmark is gc.alloc.rate.norm
  // which shows the heap allocation rate per benchmark iteration.
  // We can see the only benchmark that allocates is the one that
  // "dynamically" uses a varargs array. If the callee knows the length of
  // the array (which is rare; what's the point of the varargs then?) and can
  // directly extract the values, then the array is optimized out.
  // In the common case where the callee doesn't know how many params are
  // passed and needs to iterate over them, a full array needs to be
  // allocated on the heap.
}
