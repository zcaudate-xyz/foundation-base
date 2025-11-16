(ns xt.lang.event-log
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.lang.event-common :as event-common]]})

(defn.xt new-log
  "creates a new log"
  {:added "4.0"}
  [m]
  (return
   (event-common/blank-container
    "event.log"
    (k/obj-assign
     {:last      nil
      :processed []
      :cache     {}
      :interval  30000
      :maximum   100
      :callback  nil
      :listeners {}}
     m))))

(defn.xt get-count
  "gets the current count"
  {:added "4.0"}
  [log]
  (var #{processed} log)
  (return (k/len processed)))

(defn.xt get-last
  "gets the last log entry"
  {:added "4.0"}
  [log]
  (var #{processed} log)
  (return (k/last processed)))

(defn.xt get-head
  "gets `n` elements from beginning"
  {:added "4.0"}
  [log n]
  (var #{processed} log)
  (var total (k/len processed))
  (return (k/arr-slice processed 0
                       (k/min n total))))

(defn.xt get-filtered
  "filters entries using predicate"
  {:added "4.0"}
  [log pred]
  (var #{processed} log)
  (return (k/arr-filter processed pred)))

(defn.xt get-tail
  "gets `n` elements from tail"
  {:added "4.0"}
  [log n]
  (var #{processed} log)
  (var total (k/len processed))
  (return (k/arr-rslice processed
                        (k/max 0 (- total n))
                        total)))

(defn.xt get-slice
  "gets a slice of the log entries"
  {:added "4.0"}
  [log start finish]
  (var #{processed} log)
  (var total (k/len processed))
  (return (k/arr-slice processed
                       (k/min (k/max 0 start) total)
                       (k/min (k/max 0 finish) total))))

(defn.xt clear
  "clears all processed entries"
  {:added "4.0"}
  [log]
  (var #{processed} log)
  (k/set-key log "processed" [])
  (return processed))

(defn.xt clear-cache
  "clears log cache"
  {:added "4.0"}
  [log t]
  (:= t (or t (k/now-ms)))
  (var #{last interval cache} log)
  (var out [])
  (when (and last
             (>= interval (- t last)))
    (return out))
  
  (k/set-key log "last" t)
  (k/for:object [[k kt] cache]
    (when (< interval (- t kt))
      (k/del-key cache k)
      (x:arr-push out k)))
  (return out))


(def.xt METHODS
  {:count {:handler -/get-count
           :input []}
   :last  {:handler -/get-last
           :input []}
   :tail  {:handler -/get-tail
           :input [{:symbol "n"
                   :type "integer"}]}
   :head  {:handler -/get-head
           :input [{:symbol "n"
                   :type "integer"}]}
   :slice {:handler -/get-slice
           :input [{:symbol "start"
                   :type "integer"}
                  {:symbol "finish"
                   :type "integer"}]}
   :clear {:handler -/clear
           :input []}
   :clear-cache {:handler -/clear-cache
                 :input [{:symbol "t"
                         :type "integer"}]}})

;;
;;
;;

(defn.xt queue-entry
  "queues a log entry"
  {:added "4.0"}
  [log input key-fn data-fn t]
  (:= t (or t (k/now-ms)))
  (var #{processed cache maximum callback listeners} log)
  (var key  (:? key-fn
                (key-fn input t)
                t))
  (var data (data-fn input))
  (-/clear-cache log t)
  
  (cond (or (k/nil? key)
            (k/get-key cache key))
        (return nil)
        
        :else
        (do (k/set-key cache key t)
            (k/arr-pushl processed
                         (k/clone-nested data)
                         maximum)
            (when callback (callback data t))
            (k/for:object [[id entry] listeners]
              (var #{callback meta} entry)
              (callback id data t meta))
            (return data))))

(defn.xt add-listener
  "adds a listener to the log"
  {:added "4.0"}
  [log listener-id callback meta]
  (return
   (event-common/add-listener
    log listener-id "log" callback
    meta
    nil)))

(def.xt ^{:arglists '([log listener-id])}
  remove-listener
  event-common/remove-listener)

(def.xt ^{:arglists '([log])}
  list-listeners
  event-common/list-listeners)

