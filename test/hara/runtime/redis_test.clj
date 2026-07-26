tahto/runtime/redis_test.clj:1:(ns tahto.runtime.redis-test
  (:require [kmi.queue.list :as list]
            [lib.redis.bench :as bench]
            [net.resp.connection :as conn]
tahto/runtime/redis_test.clj:5:            [tahto.runtime.redis :refer :all]
tahto/runtime/redis_test.clj:6:            [tahto.runtime.redis.eval-script :as script]
            [std.concurrent :as cc])
  (:use code.test))

(fact:global
 {:setup [(bench/start-redis-array [17001])]
  :teardown [(bench/stop-redis-array [17001])]})

tahto/runtime/redis_test.clj:14:^{:refer tahto.runtime.redis/generate-script :added "4.0"}
(fact "generates a script given a pointer"

  (generate-script kmi.queue.list/mq-list-group-init)
  => string?)

tahto/runtime/redis_test.clj:20:^{:refer tahto.runtime.redis/test:req :added "4.0"}
(fact "does a request on a single test connection")

tahto/runtime/redis_test.clj:23:^{:refer tahto.runtime.redis/test:invoke :added "4.0"}
(fact "does a script call on a single test connection")
