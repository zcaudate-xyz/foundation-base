(ns kmi.redis-test
  (:require [lib.redis.bench :as bench]
            [rt.redis]
            [std.lang :as l])
  (:use code.test))

(l/script- :lua
  {:runtime :redis.client
   :config {:rt/id :test.exchange
            :port 17001
            :bench true}
   :require [[xt.lang.common-lib :as k]
             [kmi.redis :as r]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer kmi.redis/zdiff :added "3.0" :adopt true
  :setup [(r/call "FLUSHDB")
          (r/call "ZADD" "test:a" 1 "A" 2 "B" 3 "C")
          (r/call "ZADD" "test:b" 1 "A" 2 "B" 3 "D")]}
(fact "calculates the difference in sets"

  (r/zdiff "test:a" "test:b")
  => ["C"])

^{:refer kmi.redis/zintersect :added "3.0" :adopt true
  :setup [(r/call "FLUSHDB")
          (r/call "ZADD" "test:a" 1 "A" 2 "B" 3 "C")
          (r/call "ZADD" "test:b" 1 "A" 2 "B" 3 "D")]}
(fact "calculates the difference in sets"

  (r/zintersect "test:a" "test:b")
  => ["A" "B"])

^{:refer kmi.redis/z-tmpl :added "3.0" :adopt true}
(fact "template for zdiff and zintersect")

^{:refer kmi.redis/cas-tmpl :added "3.0"
  :setup [(r/call "FLUSHDB")] :adopt true}
(fact "compares and swap for keys and hashs"

  (r/cas-set "test:a" false 1)
  => ["OK"]

  (r/cas-set "test:a" "1" "2")
  => ["OK"]

  (r/cas-set "test:a" "1" "2")
  => ["NEW" "2"]

  (r/cas-hset "test:b" "b" false "1")
  => ["OK"]

  (r/cas-hset "test:b" "b" "1" "2")
  => ["OK"]

  (r/cas-hset "test:b" "b" "1" "2")
  => ["NEW" "2"])

^{:refer kmi.redis/flushdb :added "4.0"}
(fact "clears the redis db")

^{:refer kmi.redis/log :added "4.0"}
(fact "outputs to std out")

^{:refer kmi.redis/call-fn :added "4.0"}
(fact "calles with redis")

^{:refer kmi.redis/as-num :added "3.0"}
(fact "converts to a number"

  (r/as-num false)
  => 0

  (r/as-num "4")
  => 4)

^{:refer kmi.redis/zscoremin :added "3.0"
  :setup [(r/call "FLUSHDB")
          (r/call "ZADD" "test:a" 1 "A" 2 "B" 3 "C")]}
(fact "finds the minimum score"

  (r/zscoremin "test:a")
  => "1")

^{:refer kmi.redis/zscoremax :added "3.0"
  :setup [(r/call "FLUSHDB")
          (r/call "ZADD" "test:a" 1 "A" 2 "B" 3 "C")]}
(fact "retrieves the maximum score"

  (r/zscoremax "test:a")
  => "3")

^{:refer kmi.redis/zscorerange :added "3.0"
  :setup [(r/call "FLUSHDB")
          (r/call "ZADD" "test:a" 1 "A" 2 "B" 3 "C")]}
(fact "retrieves the maximum score"

  (r/zscorerange "test:a")
  => [1 3])

^{:refer kmi.redis/key-copy :added "3.0"
  :setup [(r/call "FLUSHDB")

          (apply r/call "HSET" "test:a" (range 200))]}
(fact "copies a key"

  (r/key-copy "test:a" "test:b")

  (-> (r/call "HKEYS" "test:b")
      (count))
  => 100)

^{:refer kmi.redis/ttl-time :added "3.0"
  :setup [(r/call "FLUSHDB")
          (r/call "SET" "test:a" 1 "PX" 10001)]}
(fact "gets the absolute time for expiry"

  (r/ttl-time "test:a")
  => number?)

^{:refer kmi.redis/key-export :added "3.0"
  :setup [(r/call "FLUSHDB")
          (apply r/call "HSET" "test:a" (range 200))]}
(fact "exports a key"

  (r/key-export "test:a")
  => (contains ["hash" vector?]))

^{:refer kmi.redis/dump-db :added "4.0"
  :setup [(r/call "FLUSHDB")]}
(fact "dumps all keys in db"

  (r/dump-db)
  => {})

^{:refer kmi.redis/time-ms :added "4.0"}
(fact "gets time in ms"

  (r/time-ms)
  => int?)

^{:refer kmi.redis/time-us :added "4.0"}
(fact "gets time in us"

  (r/time-us)
  => float?)

^{:refer kmi.redis/bench :added "3.0"
  :setup [(r/call "FLUSHDB")
          (apply r/call "HSET" "test:a" (range 200))]}
(fact "benchmarkes a function in `us`"

  (!.lua
   (r/bench r/key-export "test:a"))
  => (contains [integer? vector?]))

^{:refer kmi.redis/bench-offset :added "4.0"}
(fact "provides measurement of additional `us` time for bench"

  (r/bench-offset)
  => int?)

^{:refer kmi.redis/bench-wrap :added "4.0"}
(fact "wraps a function so that it is benched")

^{:refer kmi.redis/do-regex :added "4.0"
  :setup [(r/call "FLUSHDB")
          (apply r/call "HSET" "test:a" (range 200))]}
(fact "helper function for key actions"

  (!.lua (local a [])
         (r/do-regex ".*" "*"
                     (fn [v]
                       (table.insert a v)))
         a)
  => ["test:a"])

^{:refer kmi.redis/scan-regex :added "3.0"}
(fact "provides a regex extension to scan"

  (r/scan-regex ".*" "*")
  => vector?)

^{:refer kmi.redis/scan-level :added "3.0"}
(fact "scan keys only at a given level"

  (r/scan-level "test")
  => vector?)

^{:refer kmi.redis/scan-sub :added "3.0"}
(fact "scan keys but return only subkey part"

  (r/scan-sub "test")
  => vector?)

^{:refer kmi.redis/flat-pairs-to-object :added "4.0"}
(fact "flat pairs to object"

  (r/flat-pairs-to-object ["a" 1 "b" 2])
  => {"a" 1, "b" 2})

^{:refer kmi.redis/flat-pairs-to-array :added "4.0"}
(fact "flat pairs to array"

  (r/flat-pairs-to-array ["a" 1 "b" 2])
  => [["a" 1] ["b" 2]])

^{:refer kmi.redis/call-batched :added "3.0"}
(fact "applies command to keys in batches"

  (!.lua
   (r/call-batched "DEL" ["a" "b" "c" "d"] {:batch 2}))
  => [0 0])
