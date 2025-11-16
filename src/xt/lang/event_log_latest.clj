(ns xt.lang.event-log-latest
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.lang.event-common :as event-common]]})

(defn.xt new-log-latest
  "creates a new log-latest"
  {:added "4.0"}
  [m]
  (return
   (event-common/blank-container
    "event.log-latest"
    (k/obj-assign
     {:last      nil
      :cache     {}
      :interval  30000
      :callback  nil}
     m))))

(defn.xt clear-cache
  "clears the cache given a time point"
  {:added "4.0"}
  [log t]
  (:= t (or t (k/now-ms)))
  (var #{last interval cache} log)
  (var out [])
  (when (and last (>= interval (- t last)))
    (return out))
  (k/set-key log "last" t)
  (k/for:object [[k entry] cache]
    (when (< interval (- t (. entry t)))
      (k/del-key cache k)
      (x:arr-push out k)))
  (return out))

(defn.xt queue-latest
  "queues the latest time to log"
  {:added "4.0"}
  [log key latest]
  (var #{cache} log)
  (var entry (k/get-key cache key))
  (var t (k/now-ms))
  (cond (k/nil? entry)
        (do (k/set-key cache key {:t t
                                  :latest latest})
            (-/clear-cache log t)
            (return true))

        (< (. entry latest) latest)
        (do (k/set-key cache key {:t t
                                  :latest latest})
            (-/clear-cache log t)
            (return true))
        
        :else
        (return false)))

