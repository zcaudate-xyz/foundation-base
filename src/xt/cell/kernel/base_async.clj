(ns xt.cell.kernel.base-async
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :xtalk)

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

(defn.xt promise-run
  "runs a thunk in a host promise"
  {:added "4.1"}
  [thunk]
  (return
   (new Promise
    (fn [resolve reject]
      (try
        (resolve (thunk))
        (catch err
          (reject err)))))))

(defn.xt promise-all
  "waits for all promises in an array"
  {:added "4.1"}
  [promises]
  (return (. Promise (all promises))))

(defn.xt promise-delay
  "runs a thunk after a timeout and resolves or rejects with its result"
  {:added "4.1"}
  [ms thunk]
  (return
   (new Promise
    (fn [resolve reject]
      (setTimeout
       (fn []
         (try
           (resolve (thunk))
           (catch err
             (reject err))))
       ms)))))

(defn.xt promise-next
  "schedules a thunk on the next tick"
  {:added "4.1"}
  [thunk]
  (return (-/promise-delay 0 thunk)))

(defn.xt async-fn
  "adapts a handler function to the event-view async pipeline contract"
  {:added "4.1"}
  [handler-fn context #{success error}]
  (return (. (-/promise-run
              (fn []
                (return (handler-fn context))))
             (then success)
             (catch error))))
