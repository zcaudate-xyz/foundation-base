(ns xt.lang.common-async
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as spec-promise]]})

(defspec.xt promise-run
  [:fn [[:fn [] :xt/any]] :xt/promise])

(defspec.xt promise-all
  [:fn [[:xt/array :xt/any]] :xt/promise])

(defspec.xt promise-delay
  [:fn [:xt/int [:fn [] :xt/any]] :xt/promise])

(defspec.xt promise-next
  [:fn [[:fn [] :xt/any]] :xt/promise])

(defspec.xt async-fn
  [:fn [[:fn [:xt/any] :xt/any]
        :xt/any
        [:xt/dict :xt/str [:fn [:xt/any] :xt/any]]]
   :xt/promise])

(defmacro.xt ^{:standalone true}
  promise-run
  "runs a thunk in the host promise interface"
  {:added "4.1"}
  [thunk]
  (list 'xt.lang.spec-promise/x:promise thunk))

(defmacro.xt ^{:standalone true}
  promise-all
  "waits for all values or promises in an array"
  {:added "4.1"}
  [promises]
  (list 'xt.lang.spec-promise/x:promise-all promises))

(defmacro.xt ^{:standalone true}
  promise-delay
  "runs a thunk after a timeout"
  {:added "4.1"}
  [ms thunk]
  (list 'xt.lang.spec-promise/x:with-delay ms thunk))

(defmacro.xt ^{:standalone true}
  promise-next
  "schedules a thunk on the next tick"
  {:added "4.1"}
  [thunk]
  (list 'xt.lang.spec-promise/x:with-delay 0 thunk))

(defn.xt async-fn
  "adapts a handler function to the event-view async pipeline contract"
  {:added "4.1"}
  [handler-fn context #{success error}]
  (return
   (spec-promise/x:promise-catch
    (spec-promise/x:promise-then
     (-/promise-run
      (fn []
        (return (handler-fn context))))
     success)
    error)))
