(ns xt.lang.common-promise
  (:require [std.lang :as l :refer [defspec.xt]]
            [xt.lang.spec-promise :as spec-promise])
  (:refer-clojure :exclude [promise]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]]})

(declare promise-finish!
         promise-resolve!
         promise-reject!
         promise-apply-resolve!
         promise-apply-reject!
         promise-dispatch!
         promise-subscribe!
         promise-adopt!)

(defspec.xt promise-native? [:fn [:xt/any] :xt/bool])

(defn.xt promise-native?
  "checks for the common xt.promise wrapper"
  {:added "4.1"}
  [value]
  (return (and (xt/x:is-object? value)
               (== "xt.promise" (xt/x:get-key value "::")))))

(defn.xt promise-resolve
  "creates a resolved common promise wrapper"
  {:added "4.1"}
  [value]
  (return {"::" "xt.promise"
           "status" "resolved"
           "value" value}))

(defn.xt promise-reject
  "creates a rejected common promise wrapper"
  {:added "4.1"}
  [err]
  (return {"::" "xt.promise"
           "status" "rejected"
           "error" err}))

(defn.xt promise-pending
  "creates a pending common promise wrapper"
  {:added "4.1"}
  [async]
  (return {"::" "xt.promise"
            "status" "pending"
            "async" async
            "children" []}))

(defspec.xt promise-finish! [:fn [:xt/any :xt/str :xt/any] :xt/any])

(defspec.xt promise-resolve! [:fn [:xt/any :xt/any] :xt/any])

(defspec.xt promise-reject! [:fn [:xt/any :xt/any] :xt/any])

(defspec.xt promise-apply-resolve!
  [:fn [:xt/any [:xt/maybe [:xt/fn]] :xt/any] :xt/any])

(defspec.xt promise-apply-reject!
  [:fn [:xt/any [:xt/maybe [:xt/fn]] :xt/any] :xt/any])

(defspec.xt promise-dispatch! [:fn [:xt/any :xt/any] :xt/any])

(defspec.xt promise-subscribe!
  [:fn [:xt/any :xt/any [:xt/maybe [:xt/fn]] [:xt/maybe [:xt/fn]]] :xt/any])

(defspec.xt promise-adopt! [:fn [:xt/any :xt/any] :xt/any])

(defn.xt promise-finish-fn
  "settles a common promise wrapper and dispatches any children"
  {:added "4.1"}
  [p status payload children-fn]
  (when (== "pending" (xt/x:get-key p "status"))
    (xt/x:set-key p "status" status)
    (if (== "rejected" status)
      (do (xt/x:set-key p "error" payload)
          (xt/x:set-key p "value" nil))
      (do (xt/x:set-key p "value" payload)
          (xt/x:set-key p "error" nil)))
    (var children (xt/x:get-key p "children"))
    (xt/x:set-key p "children" [])
    (xt/for:array [entry children]
      (children-fn p entry)))
  (return p))

(defn.xt promise-resolve-fn
  "settles a promise as resolved"
  {:added "4.1"}
  [p value children-fn]
  (return (-/promise-finish-fn p "resolved" value children-fn)))

(defn.xt promise-reject-fn
  "settles a promise as rejected"
  {:added "4.1"}
  [p error children-fn]
  (return (-/promise-finish-fn p "rejected" error children-fn)))

(defn.xt promise-apply-resolve!
  "applies a success callback or forwards the resolved value"
  {:added "4.1"}
  [target thunk value children-fn]
  (if (xt/x:nil? thunk)
    (return (-/promise-resolve-fn target value children-fn))
    (try
      (return (-/promise-adopt-fn target (thunk value) children-fn))
      (catch err
        (return (-/promise-reject-fn target err children-fn))))))

(defn.xt promise-apply-reject!
  "applies an error callback or forwards the rejection"
  {:added "4.1"}
  [target thunk err]
  (if (xt/x:nil? thunk)
    (return (-/promise-reject! target err))
    (try
      (return (-/promise-adopt! target (thunk err)))
      (catch inner-err
        (return (-/promise-reject! target inner-err))))))

(defn.xt promise-dispatch!
  "dispatches a settled parent promise to a subscribed child"
  {:added "4.1"}
  [promise entry]
  (var status (xt/x:get-key promise "status"))
  (var child (xt/x:get-key entry "child"))
  (var on-resolve (xt/x:get-key entry "resolve"))
  (var on-reject (xt/x:get-key entry "reject"))
  (if (== "rejected" status)
    (return (-/promise-apply-reject! child on-reject (xt/x:get-key promise "error")))
    (return (-/promise-apply-resolve! child on-resolve (xt/x:get-key promise "value")))))

(defn.xt promise-subscribe!
  "subscribes a child promise to a parent promise"
  {:added "4.1"}
  [promise child on-resolve on-reject]
  (var status (xt/x:get-key promise "status"))
  (if (== "pending" status)
    (do (xt/x:arr-push
         (xt/x:get-key promise "children")
         {"child" child
          "resolve" on-resolve
          "reject" on-reject})
        (return child))
    (return (-/promise-dispatch!
             promise
             {"child" child
              "resolve" on-resolve
              "reject" on-reject}))))

(defn.xt promise-adopt!
  "adopts either a raw value or another common promise"
  {:added "4.1"}
  [target value]
  (if (-/promise-native? value)
    (do (var status (xt/x:get-key value "status"))
        (cond (== "pending" status)
              (return (-/promise-subscribe! value target nil nil))

              (== "rejected" status)
              (return (-/promise-reject! target (xt/x:get-key value "error")))

              :else
              (return (-/promise-resolve! target (xt/x:get-key value "value")))))
    (return (-/promise-resolve! target value))))

(defspec.xt promise [:fn [[:xt/fn]] :xt/promise])

(defn.xt promise
  "wraps thunk execution in the common xt.promise model"
  {:added "4.1"}
  [thunk]
  (var out (-/promise-pending nil))
  (try
    (xt/x:set-key
     out
     "async"
     (xt/x:async-run
      (fn []
        (try
          (return (-/promise-adopt! out (thunk)))
          (catch err
            (return (-/promise-reject! out err)))))))
    (return out)
    (catch err
      (return (-/promise-reject err)))))

(defspec.xt promise-run [:fn [:xt/any] :xt/promise])

(defn.xt promise-run
  "normalises plain values and common wrappers"
  {:added "4.1"}
  [value]
  (if (-/promise-native? value)
    (return value)
    (return (-/promise-resolve value))))

(defspec.xt promise-then [:fn [:xt/promise [:xt/fn]] :xt/promise])

(defn.xt promise-then
  "chains a success callback onto a common promise"
  {:added "4.1"}
  [promise thunk]
  (var current (-/promise-run promise))
  (var child (-/promise-pending nil))
  (return (-/promise-subscribe! current child thunk nil)))

(defspec.xt promise-catch [:fn [:xt/promise [:xt/fn]] :xt/promise])

(defn.xt promise-catch
  "chains an error callback onto a common promise"
  {:added "4.1"}
  [promise thunk]
  (var current (-/promise-run promise))
  (var child (-/promise-pending nil))
  (return (-/promise-subscribe! current child nil thunk)))

(defspec.xt promise-all [:fn [[:xt/array :xt/any]] :xt/promise])

(defn.xt promise-all
  "waits for all promise values in order"
  {:added "4.1"}
  [promises]
  (var values (:? (xt/x:nil? promises) [] promises))
  (var out [])
  (var chain (-/promise-run nil))
  (xt/for:array [value values]
    (:= chain
        (-/promise-then
         chain
         (fn [_]
           (return
            (-/promise-then
             (-/promise-run value)
             (fn [resolved]
               (xt/x:arr-push out resolved)
               (return nil))))))))
  (return
   (-/promise-then
    chain
    (fn [_]
      (return out)))))

(defspec.xt promise-finally [:fn [:xt/promise [:xt/fn]] :xt/promise])

(defn.xt promise-finally
  "runs a finalizer and preserves the original settlement unless cleanup fails"
  {:added "4.1"}
  [promise thunk]
  (return
   (-/promise-catch
    (-/promise-then
     promise
     (fn [value]
       (return
        (-/promise-then
         (-/promise thunk)
         (fn [_]
           (return value))))))
    (fn [err]
      (return
       (-/promise-catch
        (-/promise-then
         (-/promise thunk)
         (fn [_]
           (throw err)))
        (fn [cleanup-err]
          (throw cleanup-err))))))))
