(ns lua.nginx.driver-redis
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]))

(l/script :lua
  {:import [["resty.redis" :as ngxredis]
            ["resty.redis" :as ngxredis]]})

(h/template-entries [l/tmpl-macro {:base "redis"
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

(comment
  (./create-tests)
  (./create-tests)
  )
