^{:no-test true}
(ns lib.redis.integration-test
  (:require [lib.redis :as r]
             [lib.redis.bench :as bench]
             [lib.redis.event :as event]
             [net.resp.pool :as pool]
             [std.concurrent :as cc]
             [std.lib.component :as component]
             [std.lib.foundation :as f]
             [std.lib.network :as network])
  (:use code.test))

(def +redis-port+
  (or (network/port:check-available 17001)
      (network/port:check-available 0)))

(defn blank
  ([client]
   (doto client (pool/pool:request-single ["FLUSHDB"]))))

(fact:global
 {:setup    [(bench/start-redis-array [+redis-port+])]
  :teardown [(bench/stop-redis-array [+redis-port+])]})

(fact "creates a redis client and pings"
  (component/with-lifecycle [|client| {:start (-> (r/client-create {:id "localhost"
                                                                    :port +redis-port+})
                                                  r/client:start
                                                  blank)
                                     :stop r/client:stop}]
    (f/string (pool/pool:request-single |client| ["PING"])))
  => "PONG")
