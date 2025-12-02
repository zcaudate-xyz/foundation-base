(ns lib.redis-test
  (:use [code.test :exclude [run]])
  (:require [lib.redis.bench :as bench]
            [lib.redis.event :as event]
            [lib.redis :as r]
            [net.resp.connection :as conn]
            [net.resp.pool :as pool]
            [std.concurrent :as cc]
            [std.lib :as h]))

(defn mock-pool []
  (reify java.lang.AutoCloseable (close [_])))

^{:refer lib.redis/client-steps :added "3.0"}
(fact "clients steps for start up and shutdown"
  (r/client-steps) => vector?)

^{:refer lib.redis/client-string :added "4.0"}
(fact "creates a cliet string"
  (r/client-string {:host "h" :port 1 :pool :p})
  => string?)

^{:refer lib.redis/client-start :added "4.0"}
(fact "starts the client"
  (with-redefs [pool/pool (constantly :pool)
                pool/pool:start (fn [c] (assoc c :started true))]
    (r/client-start {:id :id :host "h" :port 1}))
  => (contains {:started true :pool :pool}))

^{:refer lib.redis/client-create :added "3.0"}
(fact "creates a redis client"
  (r/client-create {:id "localhost" :port 17001})
  => map?)
