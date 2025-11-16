(ns xt.lang.util-throttle
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]]})

(defn.xt throttle-create
  "creates a throttle"
  {:added "4.0"}
  [handler now-fn]
  (return {:now-fn (or now-fn k/now-ms)
           :handler handler
           :active {}
           :queued {}}))

(defn.xt throttle-run-async
  "runs an async throttle"
  {:added "4.0"}
  [throttle id args]
  (var #{active queued handler now-fn} throttle)
  (:= args (or args []))
  (return (k/for:async [[ret err] (handler id (k/unpack args))]
            {:finally (do (k/del-key active id)
                          (let [qentry (k/get-key queued id)]
                            (when qentry
                              (k/set-key active id qentry)
                              (k/del-key queued id)
                              (return (-/throttle-run-async throttle id args)))))})))

(defn.xt throttle-run
  "throttles a function so that it only runs a single thread"
  {:added "4.0"}
  [throttle id args]
  (var #{active queued handler now-fn} throttle)
  (var qentry (k/get-key queued id))
  (when qentry
    (return qentry))
  
  (var aentry (k/get-key active id))
  (when aentry
    (:= qentry [(k/first aentry) (now-fn)])
    (k/set-key queued id qentry)
    (return qentry))
  
  (var thread (-/throttle-run-async throttle id args))
  (:= aentry [thread (now-fn)])
  (k/set-key active id aentry)
  (return aentry))

(defn.xt throttle-waiting
  "gets all the waiting ids"
  {:added "4.0"}
  [throttle]
  (var #{active queued} throttle)
  (return (k/arr-union (k/obj-keys active)
                       (k/obj-keys queued))))

(defn.xt throttle-active
  "gets the active ids in a throttle"
  {:added "4.0"}
  [throttle]
  (var #{active} throttle)
  (return (k/obj-keys active)))

(defn.xt throttle-queued
  "gets all the queued ids"
  {:added "4.0"}
  [throttle]
  (var #{queued} throttle)
  (return (k/obj-keys queued)))

