(ns rt.redis-test
  (:use code.test)
  (:require [rt.redis :refer :all]
            [lib.redis.bench :as bench]
            [rt.redis.eval-script :as script]
            [std.lib :as h]
            [net.resp.connection :as conn]
            [std.concurrent :as cc]
            [kmi.queue.list :as list]))

(fact:global
 {:setup [(bench/start-redis-array [17001])]
  :teardown [(bench/stop-redis-array [17001])]})

^{:refer rt.redis/generate-script :added "4.0"}
(fact "generates a script given a pointer"
  ^:hidden
  
  (generate-script kmi.queue.list/mq-list-group-init)
  => string?)

^{:refer rt.redis/test:req :added "4.0"}
(fact "does a request on a single test connection")

^{:refer rt.redis/test:invoke :added "4.0"}
(fact "does a script call on a single test connection")
