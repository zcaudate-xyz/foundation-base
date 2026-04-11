(ns xt.lang.event-log-latest
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.event-common :as event-common]]})

(defn.xt new-log-latest
  "creates a new log-latest"
  {:added "4.0"}
  [m]
  (return
   (event-common/blank-container
    "event.log-latest"
    (xt/x:obj-assign
     {:last      nil
      :cache     {}
      :interval  30000
      :callback  nil}
     m))))

(defn.xt clear-cache
  "clears the cache given a time point"
  {:added "4.0"}
  [log t]
  (:= t (or t (xt/x:now-ms)))
  (var #{last interval cache} log)
  (var out [])
  (when (and last (>= interval (- t last)))
    (return out))
  (xt/x:set-key log "last" t)
  (xt/for:object [[k entry] cache]
    (when (< interval (- t (. entry t)))
      (xt/x:del-key cache k)
      (xt/x:arr-push out k)))
  (return out))

(defn.xt queue-latest
  "queues the latest time to log"
  {:added "4.0"}
  [log key latest]
  (var #{cache} log)
  (var entry (xt/x:get-key cache key))
  (var t (xt/x:now-ms))
  (cond (xt/x:nil? entry)
        (do (xt/x:set-key cache key {:t t
                                  :latest latest})
            (-/clear-cache log t)
            (return true))

        (< (. entry latest) latest)
        (do (xt/x:set-key cache key {:t t
                                  :latest latest})
            (-/clear-cache log t)
            (return true))
        
        :else
        (return false)))

