(ns rt.redis
  (:require [lib.redis.bench :as bench]
            [lib.redis.event :as event]
            [net.resp.connection :as conn]
            [rt.redis.client :as client]
            [rt.redis.eval-script :as script]
            [std.concurrent :as cc]
            [std.lib.foundation :as f]))

(f/intern-in client/client
             client/client?
             client/client:create
             client/test:client
             bench/start-redis-array
             bench/stop-redis-array
             bench/all-redis-ports
             conn/connection
             conn/test:activate
             conn/test:deactivate
             conn/test:config
             conn/test:connection
             conn/with-test:connection
             conn/with-connection
             event/subscribe
             event/unsubscribe
             event/psubscribe
             event/punsubscribe
             event/notify
             event/unnotify
             event/list-notify
             event/has-notify
             
             [invoke client/invoke-ptr-redis]
             script/raw-compile)

(defn generate-script
  "generates a script given a pointer"
  {:added "4.0"}
  [ptr]
  (:body (script/raw-compile ptr)))

(defn test:req
  "does a request on a single test connection"
  {:added "4.0"}
  [& args]
  (with-test:connection conn
    (cc/req conn args)))

(defn test:invoke
  "does a script call on a single test connection"
  {:added "4.0"}
  [ptr & args]
  (with-test:connection conn
    (client/invoke-ptr-redis conn ptr args)))
