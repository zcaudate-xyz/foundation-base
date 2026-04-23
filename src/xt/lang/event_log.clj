(ns xt.lang.event-log
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.event-common :as event-common]]})

(defn.xt new-log
  "creates a new log"
  {:added "4.0"}
  [m]
  (return
   (event-common/blank-container
    "event.log"
    (xt/x:obj-assign
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
  (return (xt/x:len processed)))

(defn.xt get-last
  "gets the last log entry"
  {:added "4.0"}
  [log]
  (var #{processed} log)
  (return (xt/x:last processed)))

(defn.xt get-head
  "gets `n` elements from beginning"
  {:added "4.0"}
  [log n]
  (var #{processed} log)
  (var total (xt/x:len processed))
  (return (xt/x:arr-slice processed 0
                       (xt/x:m-min n total))))

(defn.xt get-filtered
  "filters entries using predicate"
  {:added "4.0"}
  [log pred]
  (var #{processed} log)
  (return (xt/x:arr-filter processed pred)))

(defn.xt get-tail
  "gets `n` elements from tail"
  {:added "4.0"}
  [log n]
  (var #{processed} log)
  (var total (xt/x:len processed))
  (return (xtd/arr-rslice processed
                          (xt/x:m-max 0 (- total n))
                          total)))

(defn.xt get-slice
  "gets a slice of the log entries"
  {:added "4.0"}
  [log start finish]
  (var #{processed} log)
  (var total (xt/x:len processed))
  (return (xt/x:arr-slice processed
                       (xt/x:m-min (xt/x:m-max 0 start) total)
                       (xt/x:m-min (xt/x:m-max 0 finish) total))))

(defn.xt clear
  "clears all processed entries"
  {:added "4.0"}
  [log]
  (var #{processed} log)
  (xt/x:set-key log "processed" [])
  (return processed))

(defn.xt clear-cache
  "clears log cache"
  {:added "4.0"}
  [log t]
  (when (xt/x:nil? t)
    (:= t (xt/x:now-ms)))
  (var #{last interval cache} log)
  (var out [])
  (when (and (xt/x:not-nil? last)
             (>= interval (- t last)))
    (return out))
  
  (xt/x:set-key log "last" t)
  (xt/for:object [[k kt] cache]
    (when (< interval (- t kt))
      (xt/x:del-key cache k)
      (xt/x:arr-push out k)))
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
  (when (xt/x:nil? t)
    (:= t (xt/x:now-ms)))
  (var #{processed cache maximum callback listeners} log)
  (var key  (:? key-fn
                (key-fn input t)
                t))
  (var data (data-fn input))
  (-/clear-cache log t)
  
  (cond (or (xt/x:nil? key)
            (not (xt/x:nil? (xt/x:get-key cache key))))
        (return nil)
        
        :else
        (do (xt/x:set-key cache key t)
            (xtd/arr-pushl processed
                           (xtd/clone-nested data)
                           maximum)
            (when callback
              (callback data t))
            (xt/for:object [[id listener-entry] listeners]
              (var #{callback meta} listener-entry)
              (when callback
                (callback id data t meta)))
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
