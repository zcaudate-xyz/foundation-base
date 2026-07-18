(ns xt.event.util-throttle
  (:require [hara.lang :as l :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as spec-promise]]})

(defspec.xt throttle-create
  [:fn [[:fn [:xt/any] :xt/any] [:xt/maybe [:fn [] :xt/int]]] :xt/any])

(defspec.xt throttle-run-async
  [:fn [:xt/any :xt/any [:xt/maybe [:xt/array :xt/any]]] :xt/promise])

(defspec.xt throttle-run
  [:fn [:xt/any :xt/any [:xt/maybe [:xt/array :xt/any]]] :xt/any])

(defspec.xt throttle-waiting
  [:fn [:xt/any] [:xt/array :xt/any]])

(defspec.xt throttle-active
  [:fn [:xt/any] [:xt/array :xt/any]])

(defspec.xt throttle-queued
  [:fn [:xt/any] [:xt/array :xt/any]])

(defn.xt throttle-create
  "creates a throttle"
  {:added "4.1"}
  [handler now-fn]
  (return {:now-fn (:? (xt/x:nil? now-fn) xt/x:now-ms now-fn)
           :handler handler
           :active {}
           :queued {}}))

(defn.xt throttle-run-async
  "runs a throttled handler once and drains any queued rerun"
  {:added "4.1"}
  [throttle id args]
  (var #{active queued handler} throttle)
  (var key (xt/x:to-string id))
  (:= args (xtd/arrayify args))
  (var inputs [id])
  (xt/for:array [arg args]
    (xt/x:arr-push inputs arg))
  (var base-promise
       (spec-promise/x:promise
        (fn []
          (return (xt/x:apply handler inputs)))))
  (return
   (spec-promise/x:promise-finally
    base-promise
    (fn []
      (xt/x:del-key active key)
      (var qentry (xt/x:get-key queued key))
      (when (xt/x:not-nil? qentry)
        (xt/x:set-key active key qentry)
        (xt/x:del-key queued key)
        (-/throttle-run-async throttle
                              id
                              (xt/x:get-key qentry "args")))))))

(defn.xt throttle-run
  "returns the current throttle entry, queueing at most one rerun"
  {:added "4.1"}
  [throttle id args]
  (var #{active queued now-fn} throttle)
  (var key (xt/x:to-string id))
  (:= args (xtd/arrayify args))
  (var qentry (xt/x:get-key queued key))
  (when (xt/x:not-nil? qentry)
    (return qentry))
  (var aentry (xt/x:get-key active key))
  (when (xt/x:not-nil? aentry)
    (:= qentry {:promise (xt/x:get-key aentry "promise")
                :started (now-fn)
                :args (xt/x:get-key aentry "args")})
    (xt/x:set-key queued key qentry)
    (return qentry))
  (:= aentry {:promise nil
              :started (now-fn)
              :args args})
  (xt/x:set-key active key aentry)
  (var promise (-/throttle-run-async throttle id args))
  (xt/x:set-key aentry "promise" promise)
  (return aentry))

(defn.xt throttle-waiting
  "gets all waiting ids"
  {:added "4.1"}
  [throttle]
  (var #{active queued} throttle)
  (return (xtd/arr-union (xt/x:obj-keys active)
                         (xt/x:obj-keys queued))))

(defn.xt throttle-active
  "gets active ids"
  {:added "4.1"}
  [throttle]
  (var #{active} throttle)
  (return (xt/x:obj-keys active)))

(defn.xt throttle-queued
  "gets queued ids"
  {:added "4.1"}
  [throttle]
  (var #{queued} throttle)
  (return (xt/x:obj-keys queued)))
