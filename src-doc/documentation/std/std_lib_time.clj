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
