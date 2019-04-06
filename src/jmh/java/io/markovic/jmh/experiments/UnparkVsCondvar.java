package io.markovic.jmh.experiments;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
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
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@State(Scope.Thread)
@Fork(2)
public class UnparkVsCondvar {
  @Param({"1", "10", "100"})
  public int numThreads;

  ConcurrentLinkedQueue<Thread> waiters = new ConcurrentLinkedQueue<>();
  volatile boolean canDie = false;
  Lock lock = new ReentrantLock();
  Condition condition = lock.newCondition();

  @Setup
  public void setup() {
    for (int i = 0; i < numThreads; i++) {
      Thread thread = new Thread(() -> {
        lock.lock();
        try {
          while (!canDie) {
            condition.await();
          }
        } catch (InterruptedException e) {
          return;
        } finally {
          lock.unlock();
        }
      });
      thread.setDaemon(true);
      thread.start();
    }

    for (int i = 0; i < numThreads; i++) {
      Thread thread = new Thread(() -> {
        boolean wasInterrupted = false;
        waiters.add(Thread.currentThread());
        while (!canDie) {
          LockSupport.park(this);
          // Ignore interrupts while waiting
          if (Thread.interrupted()) {
            wasInterrupted = true;
          }
        }

        // Restore interrupt status on exit
        if (wasInterrupted) {
          Thread.currentThread().interrupt();
        }
      });
      thread.setDaemon(true);
      thread.start();
    }
  }

  @TearDown
  public void teardown() {
    canDie = true;
  }

  @Benchmark
  public void condVarSignalAll() {
    lock.lock();
    try {
      condition.signalAll();
    } finally {
      lock.unlock();
    }
  }

  @Benchmark
  public void unparkAll() {
    Iterator<Thread> it = waiters.iterator();
    while (it.hasNext()) {
      Thread thread = it.next();
      LockSupport.unpark(thread);
    }
  }

  // RESULTS!
  //
  // Benchmark                  (numThreads)  Mode  Cnt       Score      Error  Units
  // UnparkVsCondvar.condVarSignalAll      1  avgt   10      98.859 ±    3.107  ns/op
  // UnparkVsCondvar.condVarSignalAll     10  avgt   10     321.160 ±   30.316  ns/op
  // UnparkVsCondvar.condVarSignalAll    100  avgt   10    3783.884 ±  730.839  ns/op
  // UnparkVsCondvar.unparkAll             1  avgt   10     177.295 ±   14.871  ns/op
  // UnparkVsCondvar.unparkAll            10  avgt   10   16058.986 ±  406.366  ns/op
  // UnparkVsCondvar.unparkAll           100  avgt   10  153492.890 ± 4900.969  ns/op
  //
  // Don't understimate condvars, they're really fast.
}

