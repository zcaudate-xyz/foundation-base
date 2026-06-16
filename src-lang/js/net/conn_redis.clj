(ns js.net.conn-redis
  (:require [hara.lang :as l]
            [std.lib.foundation :as f]
            [xt.lang.common-protocol :refer [defimpl.xt]])
  (:refer-clojure :exclude [print send]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-protocol :as protocol]
             [xt.net.conn-redis :as conn-redis]]
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

(defn.js client-connect
  [client opts]
  (var #{defaults} client)
  (var env (xtd/obj-assign (xtd/obj-clone defaults) opts))
  (var url (+ "redis://"
              (or (xt/x:get-key env "host") "127.0.0.1")
              ":"
              (or (xt/x:get-key env "port") "6379")))
  (var raw (-/createClient {:url url}))
  (return
   (. (. raw (connect))
      (then (fn []
              (xt/x:set-key client "raw" raw)
              (return client))))))

(defn.js client-disconnect
  [client]
  (var #{raw} client)
  (return (. raw (quit))))

(defn.js client-exec
  [client command args]
  (var #{raw} client)
  (var input (xt/x:arr-assign [command] args))
  (return (. raw (sendCommand input))))

(defimpl.xt ^{:lang :js}
  JsRedisClient
  [defaults raw]
  conn-redis/IRedisClient
  {conn-redis/connect    -/client-connect
   conn-redis/disconnect -/client-disconnect
   conn-redis/exec       -/client-exec})

(defn.js create
  [defaults]
  (return
   (-/JsRedisClient (or defaults {}) nil)))
