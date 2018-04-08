package io.markovic.jmh.experiments;

import java.util.ArrayList;
import java.util.List;
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
import org.openjdk.jmh.infra.Blackhole;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@State(Scope.Thread)
@Fork(2)
public class IteratorPerf {
  @Param({"10", "100", "1000", "10000"})
  public int numItems;

  // Use something more complex than an int/long/double because it's more
  // likely that app code is manipulating complex objects.
  List<String> strings;
  List<Integer> ints;

  private static String getRandomString() {
    return RandomStringUtils.random(
        ThreadLocalRandom.current().nextInt(5, 10));
  }

  @Setup
  public void setup() {
    strings = new ArrayList<>(numItems);
    ints = new ArrayList<>(numItems);
    for (int i = 0; i < numItems; i++) {
      strings.add(getRandomString());
      ints.add(ThreadLocalRandom.current().nextInt(5, 10));
    }
  }

  // Needed to prevent the JIT from realizing that summing the list always
  // returns the same result and thus the whole loop can just be replaced
  // with a constant!
  private void perturbStringList() {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    int index = random.nextInt(0, strings.size());
    strings.set(index, getRandomString());
  }

  // Needed to prevent the JIT from realizing that summing the list always
  // returns the same result and thus the whole loop can just be replaced
  // with a constant!
  private void perturbIntList() {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    int index = random.nextInt(0, strings.size());
    ints.set(index, random.nextInt(10, 50));
  }

  @Benchmark
  public int rawForLoopStrings() {
    perturbStringList();
    int sum = 0;
    for (int i = 0; i < strings.size(); i++) {
      sum += strings.get(i).length();
    }
    return sum;
  }

  @Benchmark
  public int rawForLoopInts() {
    perturbIntList();
    int sum = 0;
    for (int i = 0; i < ints.size(); i++) {
      sum += ints.get(i);
    }
    return sum;
  }

  @Benchmark
  public int forEachLoopStrings(Blackhole blackhole) {
    perturbStringList();
    int sum = 0;
    for (String s : strings) {
      sum += s.length();
    }
    return sum;
  }

  @Benchmark
  public int forEachLoopInts(Blackhole blackhole) {
    perturbIntList();
    int sum = 0;
    for (int i : ints) {
      sum += i;
    }
    return sum;
  }

  // RESULTS!
  //
  // Benchmark                        (numItems)  Mode  Cnt      Score     Error  Units
  // IteratorPerf.forEachLoopInts             10  avgt   10     24.127 ±   0.443  ns/op
  // IteratorPerf.forEachLoopInts            100  avgt   10     92.017 ±   0.726  ns/op
  // IteratorPerf.forEachLoopInts           1000  avgt   10    661.325 ±   1.638  ns/op
  // IteratorPerf.forEachLoopInts          10000  avgt   10   6191.103 ±  13.076  ns/op
  // IteratorPerf.forEachLoopStrings          10  avgt   10   1004.910 ±   5.967  ns/op
  // IteratorPerf.forEachLoopStrings         100  avgt   10   1088.029 ±   5.823  ns/op
  // IteratorPerf.forEachLoopStrings        1000  avgt   10   2325.451 ±  25.217  ns/op
  // IteratorPerf.forEachLoopStrings       10000  avgt   10  24893.045 ± 320.995  ns/op
  // IteratorPerf.rawForLoopInts              10  avgt   10     24.148 ±   0.368  ns/op
  // IteratorPerf.rawForLoopInts             100  avgt   10     87.894 ±   4.566  ns/op
  // IteratorPerf.rawForLoopInts            1000  avgt   10    611.584 ±   2.214  ns/op
  // IteratorPerf.rawForLoopInts           10000  avgt   10   5656.809 ±  13.956  ns/op
  // IteratorPerf.rawForLoopStrings           10  avgt   10   1006.259 ±   4.681  ns/op
  // IteratorPerf.rawForLoopStrings          100  avgt   10   1091.263 ±   9.166  ns/op
  // IteratorPerf.rawForLoopStrings         1000  avgt   10   2327.705 ±  19.821  ns/op
  // IteratorPerf.rawForLoopStrings        10000  avgt   10  25147.685 ± 368.331  ns/op
  //
  // When looping over a list of primitives like ints, the raw for loop is
  // about ~10% faster... but that's pretty much only for primitives. As soon
  // as you switch to objects like Strings, the overhead of cache misses
  // (since object contents need to be dereferenced) eliminates any benefit
  // for loops might have.
}
