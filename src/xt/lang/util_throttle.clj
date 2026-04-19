(ns xt.lang.util-throttle
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-data :as xtd]]})

(defn.xt throttle-create
  "creates a throttle"
  {:added "4.0"}
  [handler now-fn]
  (return {:now-fn (:? (xt/x:nil? now-fn) xt/x:now-ms now-fn)
           :handler handler
           :active {}
           :queued {}}))

(defn.xt throttle-run-async
  "runs an async throttle"
  {:added "4.0"}
  [throttle id args]
  (var #{active queued handler now-fn} throttle)
  (when (xt/x:nil? args)
    (:= args []))
  (var inputs [id])
  (xt/x:arr-append inputs args)
  (xt/for:async [[ret err] (xt/x:apply handler inputs)]
    {:finally (do (xt/x:del-key active id)
                  (let [qentry (xt/x:get-key queued id)]
                    (when (xt/x:not-nil? qentry)
                      (xt/x:set-key active id qentry)
                      (xt/x:del-key queued id)
                      (return (-/throttle-run-async throttle id args)))))}))

(defn.xt throttle-run
  "throttles a function so that it only runs a single thread"
  {:added "4.0"}
  [throttle id args]
  (var #{active queued handler now-fn} throttle)
  (var qentry (xt/x:get-key queued id))
  (when (xt/x:not-nil? qentry)
    (return qentry))
  
  (var aentry (xt/x:get-key active id))
  (when (xt/x:not-nil? aentry)
    (:= qentry [(xt/x:first aentry) (now-fn)])
    (xt/x:set-key queued id qentry)
    (return qentry))
  
  (var thread (-/throttle-run-async throttle id args))
  (:= aentry [thread (now-fn)])
  (xt/x:set-key active id aentry)
  (return aentry))

(defn.xt throttle-waiting
  "gets all the waiting ids"
  {:added "4.0"}
  [throttle]
  (var #{active queued} throttle)
  (return (xtd/arr-union (xt/x:obj-keys active)
                         (xt/x:obj-keys queued))))

(defn.xt throttle-active
  "gets the active ids in a throttle"
  {:added "4.0"}
  [throttle]
  (var #{active} throttle)
  (return (xt/x:obj-keys active)))

(defn.xt throttle-queued
  "gets all the queued ids"
  {:added "4.0"}
  [throttle]
  (var #{queued} throttle)
  (return (xt/x:obj-keys queued)))
