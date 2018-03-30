# JMH Playground

[JMH][] is _the_ state-of-the-art Java Microbenchmarking Harness. Setting it up
and learning how to use it can be a bit difficult; hopefully this repo makes
this process easier for others.

[jmh]: http://openjdk.java.net/projects/code-tools/jmh/

## How to run benchmarks

```
# Runs both 'clean' and 'shadowJar' by default, which is what you want
./gradlew

# Runs the benchmark that matches the provide regex; pass -h instead of a regex
# to see all JMH options.
java -jar build/libs/benchmarks.jar "HelloWorld"

# Ex: 1 run (fork), 8 warmup iterations, 10 measurement iterations
java -jar build/libs/benchmarks.jar "HelloWorld" -f 1 -wi 8 -i 10
```

## Features

Does NOT use the jmh-gradle-plugin which is confusing, brittle and difficult
to use.

## Learning to use JMH

The absolute best way to learn how to use JMH is to read through the official
JMH samples. [All of them are included in this repo][samples]. Start by
reading the [first sample file][first-sample] (they are all numbered) and
proceed from there.

[samples]: https://github.com/Valloric/jmh-playground/tree/master/src/jmh/java/org/openjdk/jmh/samples
[first-sample]: https://github.com/Valloric/jmh-playground/blob/master/src/jmh/java/org/openjdk/jmh/samples/JMHSample_01_HelloWorld.java

## JMH command-line options

The full output is below; the "interesting" commands are:
- `-f <int>` for number of runs
- `-i <int>` for number of iterations within a run
- `-wi <int>` for number of *warmup* iterations within a run

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

