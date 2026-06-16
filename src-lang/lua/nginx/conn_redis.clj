(ns lua.nginx.conn-redis
  (:require [hara.lang :as l]
            [std.lib.foundation :as f]
            [xt.lang.common-protocol :refer [defimpl.xt]]))

(l/script :lua.nginx
  {:import [["resty.redis" :as ngxredis]
            ["resty.redis" :as ngxredis]]
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-protocol :as protocol]
             [xt.net.conn-redis :as conn-redis]]})

(f/template-entries [l/tmpl-macro {:base "redis"
                                   :inst "rds"
                                   :tag "lua"}]
  [[connect          [host port] {:optional [ops]}]
   [set_timeout      [time]]
   [set_timeouts     [connect send read]]
   [set_keepalive    [max-timeout pool-size]]
   [get_reused_times []]
   [close            []]
   [init_pipeline    [] {:optional [n]}]
   [commit_pipeline  []]
   [cancel_pipeline  []]
   [read_reply       []]
   [add_commands     [cmd] {:vargs cmds}]
   [subscribe        [key]]
   [psubscribe       [key]]])

(defn.lua client-connect
  [client opts]
  (var #{defaults} client)
  (var env (xtd/obj-assign defaults opts))
  (var #{host port} env)
  (var raw (. ngxredis (new)))
  (var '[ok err] (-/connect raw
                             (or host "127.0.0.1")
                             (or port "6379")
                             env))
  (when (not ok) (xt/x:err err))
  (xt/x:set-key client "raw" raw)
  (return client))

(defn.lua client-disconnect
  [client]
  (var #{raw} client)
  (return (-/close raw)))

(defn.lua client-exec
  [client command args]
  (var #{raw} client)
  (var f (. (getmetatable raw)
            ["__index"]
            [command]))
  (when f
    (return (f raw (unpack args))))
  (xt/x:err (xt/x:cat "Unknown redis command " command)))

(defimpl.xt ^{:lang :lua}
  LuaNginxRedisClient
  [defaults raw]
  conn-redis/IRedisClient
  {conn-redis/connect    -/client-connect
   conn-redis/disconnect -/client-disconnect
   conn-redis/exec       -/client-exec})

(defn.lua create
  [defaults]
  (return
   (-/LuaNginxRedisClient (or defaults {}) nil)))
