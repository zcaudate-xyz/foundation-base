(ns js.lib.driver-redis
  (:require [hara.lang :as l]
            [std.lib.foundation :as f])
  (:refer-clojure :exclude [print send]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [js.core.util :as ut]
             [xt.protocol.impl.redis-connection :as redisrt]]
    :import  [["redis" :as [* Redis]]]})

(f/template-entries [l/tmpl-entry {:type :fragment
                                   :base "Redis"
                                   :tag "js"}]
  [createClient
   RedisClient
   print
   Multi
   AbortError
   RedisError
   ParserError
   ReplyError
   AggregateError
   addCommand])

(defn.js wrap-connection
  [conn]
  (return
   (redisrt/connection-create
     conn
     {"disconnect" (fn [raw]
                      (return (. raw (quit))))
       "exec"       (fn [raw command args]
                      (var input (xt/x:arr-assign [command] args))
                      (return (. raw (sendCommand input))))})))

(defn.js connect-constructor
  "creates a connection"
  {:added "4.0"}
  [m callback]
  (var #{host port} m)
  (var url (+ "redis://"
              (or host "127.0.0.1")
              ":"
              (or port "6379")))
  (var conn (-/createClient {:url url}))
  (var promise
       (. (. conn (connect))
          (then (fn []
                  (return conn)))))
  (if callback
    (return (ut/wrap-callback promise callback))
    (return promise)))

(defn.js driver
  []
  (return
   (redisrt/driver-create
    {"connect"
     (fn [m]
       (return
        (. (-/connect-constructor m)
           (then (fn [conn]
                   (return (-/wrap-connection conn)))))))})))
