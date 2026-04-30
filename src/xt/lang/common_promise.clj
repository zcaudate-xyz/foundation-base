(ns xt.lang.common-promise
  (:require [std.lang :as l :refer [defspec.xt]]
            [xt.lang.spec-promise :as spec-promise])
  (:refer-clojure :exclude [promise]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as spec-promise]]})

(defn.xt raise-error
  [err]
  (throw err))

(defspec.xt promise [:fn [[:xt/fn]] :xt/promise])

(defn.xt promise
  "wraps thunk execution in the async host model"
  {:added "4.1"}
  [thunk]
  (return
   (spec-promise/x:async-run thunk)))

(defspec.xt promise-run [:fn [:xt/any] :xt/promise])

(defn.xt promise-run
  "normalises a value into a host promise via async-run"
  {:added "4.1"}
  [value]
  (return
   (spec-promise/x:async-run
    (fn []
      (return value)))))

(defspec.xt promise-all [:fn [[:xt/array :xt/any]] :xt/promise])

(defn.xt promise-all
  "waits for all values in an array by sequencing async-bind adoption"
  {:added "4.1"}
  [promises]
  (var values (:? (xt/x:nil? promises) [] promises))
  (var out [])
  (var chain
       (spec-promise/x:async-run
        (fn []
          (return nil))))
  (xt/for:array [value values]
    (:= chain
        (spec-promise/x:async-bind
         chain
         (fn [_]
           (return
            (spec-promise/x:async-bind
             (-/promise-run value)
             (fn [resolved]
               (xt/x:arr-push out resolved)
               (return nil))
             -/raise-error)))
         -/raise-error)))
  (return
   (spec-promise/x:async-bind
    chain
    (fn [_]
      (return out))
    -/raise-error)))

(defspec.xt promise-then [:fn [:xt/promise [:xt/fn]] :xt/promise])

(defn.xt promise-then
  "chains a success callback onto a host promise via async-bind"
  {:added "4.1"}
  [promise thunk]
  (return
   (spec-promise/x:async-bind promise thunk nil)))

(defspec.xt promise-catch [:fn [:xt/promise [:xt/fn]] :xt/promise])

(defn.xt promise-catch
  "chains an error callback onto a host promise via async-bind"
  {:added "4.1"}
  [promise thunk]
  (return
   (spec-promise/x:async-bind promise nil thunk)))

(defspec.xt promise-finally [:fn [:xt/promise [:xt/fn]] :xt/promise])

(defn.xt promise-finally
  "runs a finalizer and preserves the original settlement unless cleanup fails"
  {:added "4.1"}
  [promise thunk]
  (return
   (spec-promise/x:async-bind
    promise
    (fn [value]
      (return
       (spec-promise/x:async-bind
        (spec-promise/x:async-run thunk)
        (fn [_]
          (return value))
        -/raise-error)))
    (fn [err]
      (return
       (spec-promise/x:async-bind
        (spec-promise/x:async-run thunk)
        (fn [_]
          (throw err))
        -/raise-error))))))

(defspec.xt promise-native? [:fn [:xt/any] :xt/bool])

(defn.xt promise-native?
  "checks whether a value is already a native host promise"
  {:added "4.1"}
  [value]
  (return
   (spec-promise/x:promise-native? value)))

(defspec.xt with-delay [:fn [[:xt/fn] :xt/int] :xt/any])

(defn.xt with-delay
  "wraps thunk execution in the native host with-delay type"
  {:added "4.1"}
  [ms thunk]
  (return
   (spec-promise/x:with-delay ms thunk)))
