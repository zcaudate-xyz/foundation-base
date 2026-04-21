(ns rt.redis-test
  (:require [kmi.queue.list :as list]
            [lib.redis.bench :as bench]
            [net.resp.connection :as conn]
            [rt.redis :refer :all]
            [rt.redis.eval-script :as script]
            [std.concurrent :as cc])
  (:use code.test))

(fact:global
 {:setup [(bench/start-redis-array [17001])]
  :teardown [(bench/stop-redis-array [17001])]})

^{:refer rt.redis/generate-script :added "4.0"}
(fact "generates a script given a pointer"

  (generate-script kmi.queue.list/mq-list-group-init)
  => string?)

^{:refer rt.redis/test:req :added "4.0"}
(fact "does a request on a single test connection")

^{:refer rt.redis/test:invoke :added "4.0"}
(fact "does a script call on a single test connection")
