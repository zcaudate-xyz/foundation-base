(ns xt.lang.common-promise
  (:require [hara.lang :as l :refer [defspec.xt]])
  (:refer-clojure :exclude [promise]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]]})

(defspec.xt promise-native? [:fn [:xt/any] :xt/bool])

(defn.xt promise-native?
  "checks for the common xt.promise wrapper"
  {:added "4.1"}
  [value]
  (return (and (xt/x:is-object? value)
               (== "xt.promise" (xt/x:get-key value "::")))))

(defn.xt make-resolve-state
  "creates a resolved common promise wrapper"
  {:added "4.1"}
  [value]
  (return {"::" "xt.promise"
           "status" "resolved"
           "value" value}))

(defn.xt make-rejected-state
  "creates a cancelled common promise wrapper"
  {:added "4.1"}
  [err]
  (return {"::" "xt.promise"
           "status" "rejected"
           "error" err}))

(defn.xt make-pending-state
  "creates a pending common promise wrapper"
  {:added "4.1"}
  [is-async]
  (return {"::" "xt.promise"
           "status" "pending"
           "is_async" is-async
           "children" []}))

(defn.xt internal-settle-action
  "settles a common promise wrapper and dispatches any children"
  {:added "4.1"}
  [p status payload drive-fn]
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
      (drive-fn p entry drive-fn)))
  (return p))

(defn.xt internal-link-action
  "subscribes a child promise to a parent promise"
  {:added "4.1"}
  [promise child on-resolve on-reject drive-fn]
  (var status (xt/x:get-key promise "status"))
  (if (== "pending" status)
    (do (xt/x:arr-push
         (xt/x:get-key promise "children")
         {"child" child
           "resolve" on-resolve
           "reject" on-reject})
        (return child))
    (return (drive-fn
             promise
             {"child" child
              "resolve" on-resolve
              "reject" on-reject}
             drive-fn))))

(defn.xt internal-adopt-action
  "adopts either a raw value or another common promise"
  {:added "4.1"}
  [target value drive-fn]
  (if (-/promise-native? value)
    (do (var status (xt/x:get-key value "status"))
        (cond (== "pending" status)
              (return (-/internal-link-action value target nil nil drive-fn))

              (== "rejected" status)
              (return (-/internal-settle-action
                       target
                       "rejected"
                       (xt/x:get-key value "error")
                       drive-fn))

              :else
              (return (-/internal-settle-action
                       target
                       "resolved"
                       (xt/x:get-key value "value")
                       drive-fn))))
    (return (-/internal-settle-action target "resolved" value drive-fn))))

(defn.xt internal-drive-action
  "dispatches a settled parent promise to a subscribed child"
  {:added "4.1"}
  [promise entry drive-fn]
  (var status (xt/x:get-key promise "status"))
  (var child (xt/x:get-key entry "child"))
  (var rejected? (== "rejected" status))
  (var thunk (xt/x:get-key entry (:? rejected? "reject" "resolve")))
  (var payload (xt/x:get-key promise (:? rejected? "error" "value")))
  (if (xt/x:nil? thunk)
    (return (-/internal-settle-action
             child
             (:? rejected? "rejected" "resolved")
             payload
             drive-fn))
    (try
      (return (-/internal-adopt-action child (thunk payload) drive-fn))
      (catch err
        (return (-/internal-settle-action child "rejected" err drive-fn))))))

(defspec.xt promise [:fn [[:xt/fn]] :xt/promise])

(defn.xt promise
  "wraps thunk execution in the common xt.promise model"
  {:added "4.1"}
  [thunk]
  (var out (-/make-pending-state nil))
  (try
    (xt/x:set-key
     out
     "is_async"
     (xt/x:async-run
      (fn []
        (try
          (return (-/internal-adopt-action out (thunk) -/internal-drive-action))
          (catch err
            (return (-/internal-settle-action out "rejected" err -/internal-drive-action)))))))
    (return out)
    (catch err
        (return (-/make-rejected-state err)))))

(defspec.xt promise-run [:fn [:xt/any] :xt/promise])

(defn.xt promise-run
  "normalises plain values and common wrappers"
  {:added "4.1"}
  [value]
  (if (-/promise-native? value)
    (return value)
    (return (-/make-resolve-state value))))

(defspec.xt promise-then [:fn [:xt/promise [:xt/fn]] :xt/promise])

(defn.xt promise-then
  "chains a success callback onto a common promise"
  {:added "4.1"}
  [promise thunk]
  (var current (-/promise-run promise))
  (var child (-/make-pending-state nil))
  (return (-/internal-link-action current child thunk nil -/internal-drive-action)))

(defspec.xt promise-catch [:fn [:xt/promise [:xt/fn]] :xt/promise])

(defn.xt promise-catch
  "chains an error callback onto a common promise"
  {:added "4.1"}
  [promise thunk]
  (var current (-/promise-run promise))
  (var child (-/make-pending-state nil))
  (return (-/internal-link-action current child nil thunk -/internal-drive-action)))

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

(defspec.xt with-delay [:fn [:xt/int [:xt/fn]] :xt/promise])

(defn.xt with-delay
  "delays thunk execution inside the common xt.promise model"
  {:added "4.1"}
  [ms thunk]
  (return
   (-/promise
    (fn []
      (var start (xt/x:now-ms))
      (while (< (- (xt/x:now-ms) start) ms)
        (:= start start))
      (return (thunk))))))

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
           (return (-/make-rejected-state err))))
        (fn [cleanup-err]
          (return (-/make-rejected-state cleanup-err)))))))))
