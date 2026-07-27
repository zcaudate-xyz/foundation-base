(ns documentation.std-lib-time
  (:require [std.lib.time :refer :all])
  (:use code.test))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Overview"}]]

"`std.lib.time` is part of the standard foundation library set. This page collects the public API reference for the namespace."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Reading the clock"}]]

"`std.lib.time` provides thin wrappers around the system and high-resolution clocks. These are useful for timestamps, timeouts, and profiling."

(fact "system and high-resolution time are numbers"
  (system-ns) => number?
  (system-ms) => number?
  (time-ns)   => number?
  (time-us)   => number?
  (time-ms)   => number?)

[[:section {:title "Formatting and parsing durations"}]]

"`format-ms` turns a millisecond count into a human readable string, while `parse-ms` does the reverse. `format-ns` and `parse-ns` work with nanosecond precision."

(fact "format milliseconds for display"
  (format-ms 10000000)
  => "02h 46m 40s"

  (format-ms 500)
  => "500ms"

  (format-ms 65000)
  => "01m 05s")

(fact "parse duration strings to milliseconds"
  (parse-ms "1s")
  => 1000

  (parse-ms "0.5h")
  => 1800000

  (parse-ms "2m")
  => 120000)

(fact "format and parse nanosecond values"
  (format-ns 1000000)
  => "1.000ms"

  (parse-ns "2ns")
  => 2

  (parse-ns "0.3s")
  => 300000000)

[[:section {:title "Elapsed time and benchmarking"}]]

"Use `elapsed-ms` and `elapsed-ns` to measure how much time has passed since a captured timestamp. The `bench-ms` and `bench-ns` macros run a body multiple times and report the average duration."

(fact "measure elapsed time"
  (elapsed-ms (- (time-ms) 10) true)
  => string?

  (elapsed-ns (time-ns) true)
  => string?)

(fact "benchmark a block"
  (bench-ms (Thread/sleep 10))
  => integer?

  (bench-ns (Thread/sleep 1))
  => integer?)

[[:section {:title "End-to-end: timing a small computation"}]]

"Combine parsing, benchmarking, and formatting to describe how long an operation takes in friendly units."

(fact "parse a duration, sleep, then report elapsed time"
  (let [duration (parse-ms "100ms")
        start    (time-ms)]
    (Thread/sleep duration)
    (format-ms (elapsed-ms start)))
  => string?)

[[:chapter {:title "API" :link "std.lib.time"}]]

[[:api {:namespace "std.lib.time"}]]

;; BEGIN merged documentation: plans/slop/summary/std_lib_time_summary.md
;; sha256: bc5c07f3771a3259033f933cfd2d80f4c364663ef59d72d2e99f7a96dd2e01ce
[[:chapter {:title "std.lib.time: A Comprehensive Summary" :link "merged-plans-slop-summary-std-lib-time-summary-md"}]]

"The `std.lib.time` namespace provides a set of utility functions for working with time measurements in Clojure, offering high-resolution timing, human-readable formatting, and benchmarking capabilities. It leverages `System/nanoTime` and `System/currentTimeMillis` for precise measurements and includes functions for parsing time strings."

"**Key Features and Concepts:**"

"1.  **High-Resolution Time Measurement:**\n    *   `system-ns`: Returns the system's current time in nanoseconds (from `System/nanoTime`).\n    *   `system-ms`: Returns the system's current time in milliseconds (from `System/currentTimeMillis`).\n    *   `time-ns`: Returns the current time in nanoseconds using `hara.lib.foundation.Clock/currentTimeNanos` for potentially more consistent or specialized timing.\n    *   `time-us`: Returns the current time in microseconds using `hara.lib.foundation.Clock/currentTimeMicros`.\n    *   `time-ms`: Returns the current time in milliseconds using `hara.lib.foundation.Clock/currentTimeMillis`).\n\n2.  **Time Formatting:**\n    *   `format-ms`: Converts a duration in milliseconds into a human-readable string format (e.g., \"02h 46m 40s\").\n    *   `format-ns`: Converts a duration in nanoseconds into a human-readable string format, scaling to nanoseconds, microseconds, milliseconds, seconds, kiloseconds, megaseconds, or gigaseconds.\n\n3.  **Time Parsing:**\n    *   `parse-ms`: Parses a string representation of time (e.g., \"1s\", \"0.5h\") into its equivalent duration in milliseconds.\n    *   `parse-ns`: Parses a string representation of time (e.g., \"2ns\", \"0.3s\") into its equivalent duration in nanoseconds.\n\n4.  **Elapsed Time Calculation:**\n    *   `elapsed-ms`: Calculates the time elapsed in milliseconds since a given starting millisecond timestamp, with an option to format the output.\n    *   `elapsed-ns`: Calculates the time elapsed in nanoseconds since a given starting nanosecond timestamp, with an option to format the output.\n\n5.  **Benchmarking Macros:**\n    *   `bench-ns`: A macro to measure the execution time of a code block in nanoseconds. It supports options for formatting, disabling garbage collection during measurement, and running the block multiple times.\n    *   `bench-ms`: A macro to measure the execution time of a code block in milliseconds, with similar options to `bench-ns`."

"**Usage and Importance:**"

"The `std.lib.time` module is essential for performance analysis, logging, and any application logic that requires precise timing or human-friendly time representations. Its benchmarking macros provide a convenient way to profile code execution, while the formatting and parsing functions facilitate user interaction and data processing involving time durations. This module contributes to the `foundation-base` project by offering robust and flexible tools for managing time-related aspects of applications."
;; END merged documentation: plans/slop/summary/std_lib_time_summary.md
