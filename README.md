# JMH Playground

[JMH][] is _the_ state-of-the-art Java Microbenchmarking Harness. Setting it up
(_especially_ with Gradle) and learning how to use it can be a bit difficult;
hopefully this repo makes this process easier for others.

[jmh]: http://openjdk.java.net/projects/code-tools/jmh/

## How to run benchmarks

```
# Runs both 'clean' and 'shadowJar' by default, which is what you want
./gradlew

# Runs the benchmark that matches the provided regex; pass -h instead of a regex
# to see all JMH options.
java -jar build/libs/benchmarks.jar "HelloWorld"

# Ex: 1 run (fork), 8 warmup iterations, 10 measurement iterations
java -jar build/libs/benchmarks.jar "HelloWorld" -f 1 -wi 8 -i 10
```

## Features

Does NOT use the jmh-gradle-plugin which is confusing, brittle and difficult
to use correctly.

## Learning to use JMH

The absolute best way to learn how to use JMH is to read through the official
JMH samples. [All of them are included in this repo][samples]. Start by
reading the [first sample file][] (they are all numbered) and
proceed from there.

[samples]: https://github.com/Valloric/jmh-playground/tree/master/src/jmh/java/org/openjdk/jmh/samples
[first sample file]: https://github.com/Valloric/jmh-playground/blob/master/src/jmh/java/org/openjdk/jmh/samples/JMHSample_01_HelloWorld.java

## Benchmarking pitfalls to be aware of

READ **ALL** THE JMH SAMPLES before creating any benchmarks!
There are many, many pitfalls that need to be avoided, lest your
benchmarks end up producing false results. The JMH samples list out
issues to watch out for and how to avoid them. Here's a summary:

- Unintentional dead code elimination ([JMH sample 8][]).
- Computations getting constant-folded ([JMH sample 10][]).
- Loops in benchmarks having iterations merged by JIT ([JMH sample 11][]).
    - [JMH sample 34][] covers the same issue and provides good advice.
- False Sharing AKA independent fields on the same [cache line][] affecting
each other ([JMH sample 22][]).
- Test data helping or hurting branch prediction ([JMH sample 36][]).
- Differences in cache access ([JMH sample 37][]).
- Incorrect per-invocation benchmark setup ([JMH sample 38][]).

[cache line]: https://en.wikipedia.org/wiki/CPU_cache

[JMH sample 8]: https://github.com/Valloric/jmh-playground/blob/master/src/jmh/java/org/openjdk/jmh/samples/JMHSample_08_DeadCode.java
[JMH sample 10]: https://github.com/Valloric/jmh-playground/blob/master/src/jmh/java/org/openjdk/jmh/samples/JMHSample_10_ConstantFold.java
[JMH sample 11]: https://github.com/Valloric/jmh-playground/blob/master/src/jmh/java/org/openjdk/jmh/samples/JMHSample_11_Loops.java
[JMH sample 22]: https://github.com/Valloric/jmh-playground/blob/master/src/jmh/java/org/openjdk/jmh/samples/JMHSample_22_FalseSharing.java
[JMH sample 34]: https://github.com/Valloric/jmh-playground/blob/master/src/jmh/java/org/openjdk/jmh/samples/JMHSample_34_SafeLooping.java
[JMH sample 36]: https://github.com/Valloric/jmh-playground/blob/master/src/jmh/java/org/openjdk/jmh/samples/JMHSample_36_BranchPrediction.java
[JMH sample 37]: https://github.com/Valloric/jmh-playground/blob/master/src/jmh/java/org/openjdk/jmh/samples/JMHSample_37_CacheAccess.java
[JMH sample 38]: https://github.com/Valloric/jmh-playground/blob/master/src/jmh/java/org/openjdk/jmh/samples/JMHSample_38_PerInvokeSetup.java

## JMH tips & tricks

- You _need_ warmup iterations because of JVM and JIT warmup. How many
depends on the benchmark, but probably no less than 5. A safer number is 10.
- The more measurement iterations you use, the smaller the error margin
reported by JMH at the end! A solid choice is 20 iterations.
- If you really want to get that measurement error margin to be as small as
possible, reboot your machine and run your JMH benchmarks without anything
else running on your system. No browser, IDE etc taking up CPU time.
You can go even further by turning off dynamic CPU frequency scaling in your
system BIOS.
- Sometimes you need to know more than just the average time to run your
benchmark. `@BenchmarkMode(Mode.SampleTime)` can show you a distribution
with percentiles for the time it takes to run your bench method.
- Be _extremely_ wary of dead code elimination; see
[JMH sample 8][]. You should pretty much always be returning a
value from your benchmark or using a Blackhole (see
[JMH sample 9][]).
- To test performance of code where several threads are doing different
work (e.g. one thread reading data, one writing data), use JMH thread
groups. See [JMH sample 15][] for details.
- If you need to use some custom operation/event counters in your
benchmarks, use JMH's `AuxCounters`. See [JMH sample 23][].
- If you really need a specific invocation count to avoid variance,
instead of looping in the benchmark, use JMH `batchSize` to control the
number of calls per invocation. See [JMH sample 26][].
- Use JMH `@Param` to control benchmark configuration. For instance,
seeing performance change as the size of an array changes. See
[JMH sample 27][].
- JMH also has some built-in profilers you can use. See [JMH sample
35][]. NOTE: Using multiple benchmark forks is even more important if
using profilers to reduce measurement error margin.
  - the `stack` profiler is a simple sampling profiler that can show
  hot methods.
  - the **`gc` profiler is _amazing_ for understanding garbage creation
  rate** etc.
  - the `perfnorm` profiler uses Linux [`perf`][] command to read **CPU
  hardware counters for branch misses, cache loads/stores/misses etc.**
  It also normalizes the values to benchmark iterations.

[JMH sample 8]:https://github.com/Valloric/jmh-playground/blob/master/src/jmh/java/org/openjdk/jmh/samples/JMHSample_08_DeadCode.java
[JMH sample 9]: https://github.com/Valloric/jmh-playground/blob/master/src/jmh/java/org/openjdk/jmh/samples/JMHSample_09_Blackholes.java
[JMH sample 15]: https://github.com/Valloric/jmh-playground/blob/master/src/jmh/java/org/openjdk/jmh/samples/JMHSample_15_Asymmetric.java
[JMH sample 23]: https://github.com/Valloric/jmh-playground/blob/master/src/jmh/java/org/openjdk/jmh/samples/JMHSample_23_AuxCounters.java
[JMH sample 26]: https://github.com/Valloric/jmh-playground/blob/master/src/jmh/java/org/openjdk/jmh/samples/JMHSample_26_BatchSize.java
[JMH sample 27]: https://github.com/Valloric/jmh-playground/blob/master/src/jmh/java/org/openjdk/jmh/samples/JMHSample_27_Params.java
[JMH sample 35]: https://github.com/Valloric/jmh-playground/blob/master/src/jmh/java/org/openjdk/jmh/samples/JMHSample_35_Profilers.java
[perf]: https://en.wikipedia.org/wiki/Perf_(Linux)

## JMH command-line options

The full output is below; the "interesting" commands are:
- `-f <int>` for number of runs
- `-wi <int>` for number of *warmup* iterations within a run
- `-i <int>` for number of *measurement* iterations within a run

Note that all of these can be changed with JMH annotations on the benchmark
itself (command line options override).

```
Usage: java -jar ... [regexp*] [options]
 [opt] means optional argument.
 <opt> means required argument.
 "+" means comma-separated list of values.
 "time" arguments accept time suffixes, like "100ms".

Command line options usually take precedence over annotations.

  [arguments]                 Benchmarks to run (regexp+). (default: .*)

  -bm <mode>                  Benchmark mode. Available modes are: [Throughput/thrpt,
                              AverageTime/avgt, SampleTime/sample, SingleShotTime/ss,
                              All/all]. (default: Throughput)

  -bs <int>                   Batch size: number of benchmark method calls per
                              operation. Some benchmark modes may ignore this
                              setting, please check this separately. (default:
                              1)

  -e <regexp+>                Benchmarks to exclude from the run.

  -f <int>                    How many times to fork a single benchmark. Use 0 to
                              disable forking altogether. Warning: disabling
                              forking may have detrimental impact on benchmark
                              and infrastructure reliability, you might want
                              to use different warmup mode instead. (default:
                              10)

  -foe <bool>                 Should JMH fail immediately if any benchmark had
                              experienced an unrecoverable error? This helps
                              to make quick sanity tests for benchmark suites,
                              as well as make the automated runs with checking error
                              codes. (default: false)

  -gc <bool>                  Should JMH force GC between iterations? Forcing
                              the GC may help to lower the noise in GC-heavy benchmarks,
                              at the expense of jeopardizing GC ergonomics decisions.
                              Use with care. (default: false)

  -h                          Display help, and exit.

  -i <int>                    Number of measurement iterations to do. Measurement
                              iterations are counted towards the benchmark score.
                              (default: 1 for SingleShotTime, and 20 for all other
                              modes)

  -jvm <string>               Use given JVM for runs. This option only affects forked
                              runs.

  -jvmArgs <string>           Use given JVM arguments. Most options are inherited
                              from the host VM options, but in some cases you want
                              to pass the options only to a forked VM. Either single
                              space-separated option line, or multiple options
                              are accepted. This option only affects forked runs.

  -jvmArgsAppend <string>     Same as jvmArgs, but append these options after the
                              already given JVM args.

  -jvmArgsPrepend <string>    Same as jvmArgs, but prepend these options before
                              the already given JVM arg.

  -l                          List the benchmarks that match a filter, and exit.

  -lp                         List the benchmarks that match a filter, along with
                              parameters, and exit.

  -lprof                      List profilers, and exit.

  -lrf                        List machine-readable result formats, and exit.

  -o <filename>               Redirect human-readable output to a given file.

  -opi <int>                  Override operations per invocation, see @OperationsPerInvocation
                              Javadoc for details. (default: 1)

  -p <param={v,}*>            Benchmark parameters. This option is expected to
                              be used once per parameter. Parameter name and parameter
                              values should be separated with equals sign. Parameter
                              values should be separated with commas.

  -prof <profiler>            Use profilers to collect additional benchmark data.
                              Some profilers are not available on all JVMs and/or
                              all OSes. Please see the list of available profilers
                              with -lprof.

  -r <time>                   Minimum time to spend at each measurement iteration.
                              Benchmarks may generally run longer than iteration
                              duration. (default: 1 s)

  -rf <type>                  Format type for machine-readable results. These
                              results are written to a separate file (see -rff).
                              See the list of available result formats with -lrf.
                              (default: CSV)

  -rff <filename>             Write machine-readable results to a given file.
                              The file format is controlled by -rf option. Please
                              see the list of result formats for available formats.
                              (default: jmh-result.<result-format>)

  -si <bool>                  Should JMH synchronize iterations? This would significantly
                              lower the noise in multithreaded tests, by making
                              sure the measured part happens only when all workers
                              are running. (default: true)

  -t <int>                    Number of worker threads to run with. 'max' means
                              the maximum number of hardware threads available
                              on the machine, figured out by JMH itself. (default:
                              1)

  -tg <int+>                  Override thread group distribution for asymmetric
                              benchmarks. This option expects a comma-separated
                              list of thread counts within the group. See @Group/@GroupThreads
                              Javadoc for more information.

  -to <time>                  Timeout for benchmark iteration. After reaching
                              this timeout, JMH will try to interrupt the running
                              tasks. Non-cooperating benchmarks may ignore this
                              timeout. (default: 10 min)

  -tu <TU>                    Override time unit in benchmark results. Available
                              time units are: [m, s, ms, us, ns]. (default: SECONDS)

  -v <mode>                   Verbosity mode. Available modes are: [SILENT, NORMAL,
                              EXTRA]. (default: NORMAL)

  -w <time>                   Minimum time to spend at each warmup iteration. Benchmarks
                              may generally run longer than iteration duration.
                              (default: 1 s)

  -wbs <int>                  Warmup batch size: number of benchmark method calls
                              per operation. Some benchmark modes may ignore this
                              setting. (default: 1)

  -wf <int>                   How many warmup forks to make for a single benchmark.
                              All iterations within the warmup fork are not counted
                              towards the benchmark score. Use 0 to disable warmup
                              forks. (default: 0)

  -wi <int>                   Number of warmup iterations to do. Warmup iterations
                              are not counted towards the benchmark score. (default:
                              0 for SingleShotTime, and 20 for all other modes)

  -wm <mode>                  Warmup mode for warming up selected benchmarks.
                              Warmup modes are: INDI = Warmup each benchmark individually,
                              then measure it. BULK = Warmup all benchmarks first,
                              then do all the measurements. BULK_INDI = Warmup
                              all benchmarks first, then re-warmup each benchmark
                              individually, then measure it. (default: INDI)

  -wmb <regexp+>              Warmup benchmarks to include in the run in addition
                              to already selected by the primary filters. Harness
                              will not measure these benchmarks, but only use them
                              for the warmup.
```

## License

JMH samples (which are included in the source code) are licensed under the
[3-clause BSD license][bsd].

[bsd]: http://hg.openjdk.java.net/code-tools/jmh/file/25d8b2695bac/jmh-samples/LICENSE

The rest of the code in this repo is licensed under [Apache v2][apache].

[apache]: https://www.apache.org/licenses/LICENSE-2.0

