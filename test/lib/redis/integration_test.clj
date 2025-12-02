^{:no-test true}
(ns lib.redis.integration-test
  (:use code.test)
  (:require [lib.redis.bench :as bench]
            [lib.redis.event :as event]
            [lib.redis :as r]
            [net.resp.pool :as pool]
            [std.concurrent :as cc]
            [std.lib :as h]))

(defn blank
  ([client]
   (doto client (pool/pool:request-single ["FLUSHDB"]))))

(fact:global
 {:setup    [(bench/start-redis-array [17001])]
  :teardown [(bench/stop-redis-array [17001])]
  :component
  {|client|   {:create   (r/client-create {:port 17001})
               :setup    (comp blank r/client:start)
               :teardown r/client:stop}}})

(fact "creates a redis client and pings"
  ^:hidden
  (cc/req (h/start (r/client-create {:id "localhost"
                                     :port 17001}))
          ["PING"]))
