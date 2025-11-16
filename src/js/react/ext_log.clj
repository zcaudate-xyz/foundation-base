(ns js.react.ext-log
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[xt.lang.event-log :as event-log]
             [js.react :as r]
             [js.core :as j]]})

(defn.js makeLog
  "creates a log for react"
  {:added "4.0"}
  [m]
  (return (r/const (event-log/new-log m))))

(defn.js listenLogLatest
  "uses the latest log entry"
  {:added "4.0"}
  [log meta]
  (var [latest setLatest] (r/local (event-log/get-last log)))
  (r/watch [log]
    (var listener-id (j/randomId 4))
    (event-log/add-listener
     log
     listener-id
     (fn [id data t meta]
       (setLatest #{id data t meta}))
     meta)
    (return (fn [] (event-log/remove-listener log listener-id))))
  (return latest))

