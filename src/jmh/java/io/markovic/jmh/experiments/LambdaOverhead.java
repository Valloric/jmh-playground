package io.markovic.jmh.experiments;

import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

// You MUST run this with `-prof GC` on the command line to see GC
// statistics; without those, the results are useless (we don't care about
// time in these benchmarks, only GC utilization).
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@Fork(2)
public class LambdaOverhead {
  private static final int TOKENS_TO_CONSUME = 10;

  @Param({"10"})
  public int _tokens;

  public static void workDirectly(int tokens) {
    Blackhole.consumeCPU(tokens);
  }

  public static void workWithRunnable(Runnable runnable) {
    runnable.run();
  }

  public static void runAndBlackhole(Runnable runnable, Blackhole blackhole) {
    runnable.run();
    blackhole.consume(runnable);
  }

  // No lambda is used at all; this shows baseline GC perf because nothing
  // gets allocated.
  @Benchmark
  public void noLambda() {
    workDirectly(TOKENS_TO_CONSUME);
  }

  // Does this allocate? It shouldn't because the lambda doesn't capture any
  // vars from its environment. It should compile down to just a simple
  // function that can easily be inlined, right?
  @Benchmark
  public void withNoCaptureLambda() {
    workWithRunnable(() -> Blackhole.consumeCPU(TOKENS_TO_CONSUME));
  }

  // Now this version *definitely* creates an object; it must, because the
  // lambda we provides captures a var and that needs to be stored somewhere.
  // But the question is does the JVM's escape analysis kick in to ensure the
  // object is allocated on the stack (instead of the heap), thus removing
  // all GC impact?
  @Benchmark
  public void withCaptureLambda() {
    workWithRunnable(() -> Blackhole.consumeCPU(_tokens));
  }

  // This both creates an object *and* it ensures no escape-analysis happens
  // because runAndBlackhole sends the runnable into a Blackhole, ensuring the
  // object "escapes" the stack frame (and thus, the object MUST be allocated
  // on the heap).
  @Benchmark
  public void control(Blackhole blackhole) {
    runAndBlackhole(() -> Blackhole.consumeCPU(_tokens), blackhole);
  }

  // RESULTS! (When run with `-prof gc`)
  //
  // Benchmark                                                (_tokens)  Mode  Cnt    Score    Error   Units
  // LambdaOverhead.control                                          10  avgt   10   17.068 ±  0.559   ns/op
  // LambdaOverhead.control:·gc.alloc.rate                           10  avgt   10  595.805 ± 19.603  MB/sec
  // LambdaOverhead.control:·gc.alloc.rate.norm                      10  avgt   10   16.000 ±  0.001    B/op
  // LambdaOverhead.control:·gc.churn.PS_Eden_Space                  10  avgt   10  603.161 ± 28.736  MB/sec
  // LambdaOverhead.control:·gc.churn.PS_Eden_Space.norm             10  avgt   10   16.197 ±  0.552    B/op
  // LambdaOverhead.control:·gc.churn.PS_Survivor_Space              10  avgt   10    0.094 ±  0.037  MB/sec
  // LambdaOverhead.control:·gc.churn.PS_Survivor_Space.norm         10  avgt   10    0.003 ±  0.001    B/op
  // LambdaOverhead.control:·gc.count                                10  avgt   10  149.000           counts
  // LambdaOverhead.control:·gc.time                                 10  avgt   10   74.000               ms
  // LambdaOverhead.noLambda                                         10  avgt   10   12.585 ±  0.441   ns/op
  // LambdaOverhead.noLambda:·gc.alloc.rate                          10  avgt   10   ≈ 10⁻⁴           MB/sec
  // LambdaOverhead.noLambda:·gc.alloc.rate.norm                     10  avgt   10   ≈ 10⁻⁵             B/op
  // LambdaOverhead.noLambda:·gc.count                               10  avgt   10      ≈ 0           counts
  // LambdaOverhead.withCaptureLambda                                10  avgt   10   12.766 ±  0.542   ns/op
  // LambdaOverhead.withCaptureLambda:·gc.alloc.rate                 10  avgt   10   ≈ 10⁻⁴           MB/sec
  // LambdaOverhead.withCaptureLambda:·gc.alloc.rate.norm            10  avgt   10   ≈ 10⁻⁵             B/op
  // LambdaOverhead.withCaptureLambda:·gc.count                      10  avgt   10      ≈ 0           counts
  // LambdaOverhead.withNoCaptureLambda                              10  avgt   10   12.711 ±  0.417   ns/op
  // LambdaOverhead.withNoCaptureLambda:·gc.alloc.rate               10  avgt   10   ≈ 10⁻⁴           MB/sec
  // LambdaOverhead.withNoCaptureLambda:·gc.alloc.rate.norm          10  avgt   10   ≈ 10⁻⁵             B/op
  // LambdaOverhead.withNoCaptureLambda:·gc.count                    10  avgt   10      ≈ 0           counts
  //
  // The interesting line to look at for each benchmark is gc.alloc.rate.norm
  // which shows the heap allocation rate per benchmark iteration.
  // You can see that ONLY the "control" ends up allocating anything. In all
  // other benchmarks, nothing gets allocated. This is either because no
  // objects are being created in the first place, or because they're being
  // created on the stack instead of the heap (escape analysis FTW).
}
