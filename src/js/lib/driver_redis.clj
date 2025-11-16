(ns js.lib.driver-redis
  (:require [std.lang :as l]
            [std.lib :as h])
  (:refer-clojure :exclude [print send]))

(l/script :js
  {:require [[js.core.util :as ut]]
   :import  [["redis" :as [* Redis]]]})

(h/template-entries [l/tmpl-entry {:type :fragment
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

(defn.js connect-constructor
  "creates a connection"
  {:added "4.0"}
  [m callback]
  (:= callback (or callback ut/pass-callback))
  (var #{host port} m)
  (var url (+ "redis://"
              (or host "127.0.0.1")
              ":"
              (or port "6379")))
  (var conn (-/createClient {:url url}))
  
  (:= (. conn ["::disconnect"])
      (fn [callback]
        (return (ut/wrap-callback (. conn (quit))
                                  (or callback ut/pass-callback)))))
  (:= (. conn ["::exec"])
      (fn [command args callback]
        (return (ut/wrap-callback (. conn (sendCommand [command (:.. args)] callback))
                                  (or callback ut/pass-callback)))))
  (. conn
     (connect)
     (then (fn [] (callback nil conn))))
  (return conn))

