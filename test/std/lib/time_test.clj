(ns std.lib.time-test
  (:require [std.lib.time :refer :all])
  (:use code.test))

^{:refer std.lib.time/system-ns :added "3.0"}
(fact "returns the system nano time"

  (system-ns)
  ;; 8158606456270
  => number?)

^{:refer std.lib.time/system-ms :added "3.0"}
(fact "returns the system milli time"

  (system-ms)
  => number?)

^{:refer std.lib.time/time-ns :added "3.0"}
(fact "returns current time in nano seconds"

  (time-ns)
  ;; 1593922814991449423
  => number?)

^{:refer std.lib.time/time-us :added "3.0"}
(fact "returns current time in micro seconds"

  (time-us)
  ;; 1593922882412533
  => number?)

^{:refer std.lib.time/time-ms :added "3.0"}
(fact "returns current time in milli seconds"

  (time-ms)
  ;; 1593922917603
  => number?)

^{:refer std.lib.time/format-ms :added "3.0"}
(fact "returns ms time is human readable format"

  (format-ms 10000000)
  => "02h 46m 40s")

^{:refer std.lib.time/parse-ms :added "3.0"}
(fact "parses the string representation of time in ms"

  (parse-ms "1s")
  => 1000

  (parse-ms "0.5h")
  => 1800000)

^{:refer std.lib.time/format-ns :added "3.0"}
(fact "returns ns time in seconds"

  (format-ns 1000000)
  => "1.000ms")

^{:refer std.lib.time/elapsed-ms :added "3.0"}
(fact "determines the time in ms that has elapsed"

  (elapsed-ms (- (time-ms) 10) true)
  => string?)

^{:refer std.lib.time/elapsed-ns :added "3.0"}
(fact "determines the time in ns that has elapsed"

  (elapsed-ns (time-ns) true)
  ;; "35.42us"
  => string?)

^{:refer std.lib.time/bench-ns :added "3.0" :style/indent 1}
(fact "measures a block in nanoseconds"

  (bench-ns (Thread/sleep 1))
  => integer?)

^{:refer std.lib.time/bench-ms :added "3.0" :style/indent 1}
(fact "measures a block in milliseconds"

  (bench-ms (Thread/sleep 10))
  => integer?)

^{:refer std.lib.time/parse-ns :added "3.0"}
(fact "parses the string repesentation of time in ns"

  (parse-ns "2ns")
  => 2

  (parse-ns "0.3s")
  => 300000000)