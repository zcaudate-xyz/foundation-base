(ns lua.nginx.driver-redis
  (:require [hara.lang :as l]
             [std.lib.foundation :as f]))

(l/script :lua.nginx
  {:import [["resty.redis" :as ngxredis]
            ["resty.redis" :as ngxredis]]
   :require [[xt.lang.spec-base :as xt]
             [xt.protocol.impl.connection-redis :as redisrt]]})

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

(defn.lua connect-constructor
  "creates a xt.sys compatible constructor"
  {:added "4.0"}
  [m]
  (local conn (. ngxredis (new)))
  (local #{host port} m)
  (local '[ok err] (-/connect conn
                              (or host "127.0.0.1")
                              (or port "6379")
                              m))
  (when ok
    (:= (. conn ["::disconnect"])
        (fn []
          (return (-/close conn))))
    (:= (. conn ["::exec"])
        (fn [command args cb]
          (var f (. (getmetatable conn)
                    ["__index"]
                    [command]))
          (when f
            (return (f conn (unpack args))))))
     (return conn))
  (return nil err))

(defn.lua wrap-connection
  [conn]
  (return
   (redisrt/connection-create
    conn
    {"disconnect" (fn [raw]
                    (var disconnect-fn (xt/x:get-key raw "::disconnect"))
                    (return (disconnect-fn)))
     "exec"       (fn [raw command args]
                    (var exec-fn (xt/x:get-key raw "::exec"))
                    (return (exec-fn command args)))})))

(defn.lua driver
  []
  (return
   (redisrt/driver-create
    {"connect" (fn [m]
                 (return (-/wrap-connection
                          (-/connect-constructor m))))})))

(comment
  (./create-tests)
  (./create-tests)
  )
