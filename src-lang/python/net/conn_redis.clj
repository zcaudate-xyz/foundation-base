(ns python.net.conn-redis
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :refer [defimpl.xt]]))

(l/script :python
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [python.core :as py]
             [xt.net.conn-redis :as conn-redis]]})

(defn.py load-module
  "Loads redis client library."
  {:added "4.1"}
  []
  (try
    (return (py/__import__ "redis"))
    (catch e
      (xt/x:err "Python redis module not found"))))

(defn.py client-connect
  [client opts]
  (var #{defaults} client)
  (var env (xtd/obj-assign defaults opts))
  (var redis (-/load-module))
  (var host (or (xt/x:get-key env "host") "127.0.0.1"))
  (var port (or (xt/x:get-key env "port") 6379))
  (var db (or (xt/x:get-key env "db") 0))
  (var raw (. redis (Redis :host host :port port :db db)))
  (xt/x:set-key client "raw" raw)
  (return client))

(defn.py client-disconnect
  [client]
  (var #{raw} client)
  (. raw (close))
  (return true))

(defn.py client-exec
  [client command args]
  (var #{raw} client)
  (var method (getattr raw command nil))
  (when (xt/x:nil? method)
    (xt/x:err (xt/x:cat "Unknown redis command " command)))
  (return (xt/x:apply method args)))

(defimpl.xt ^{:lang :python}
  PythonRedisClient
  [defaults raw]
  conn-redis/IRedisClient
  {conn-redis/connect    -/client-connect
   conn-redis/disconnect -/client-disconnect
   conn-redis/exec       -/client-exec})

(defn.py create
  [defaults]
  (return
   (-/PythonRedisClient (or defaults {}) nil)))
