(ns python.core.common-promise
  (:require [std.lang :as l])
  (:refer-clojure :exclude [promise]))

(l/script :python
  {:import [["asyncio" :as asyncio]
            ["inspect" :as inspect]]})

(defn.py promise-wrapper?
  "checks whether the value is a native runtime promise wrapper"
  {:added "4.1"}
  [value]
  (return (and (isinstance value dict)
               (== "xt.promise" (. value ["__type__"])))))

(defn.py promise-native?
  "checks whether the value can be treated as a promise-like value"
  {:added "4.1"}
  [value]
  (return (or (-/promise-wrapper? value)
              (. asyncio (isfuture value))
              (. inspect (isawaitable value)))))

(defn.py promise-reject
  "wraps thrown values as rejected promises"
  {:added "4.1"}
  [err]
  (return {"__type__" "xt.promise"
           "status" "rejected"
           "error" err}))

(defn.py promise-awaitable
  "synchronously settles awaitables and futures into the runtime promise model"
  {:added "4.1"}
  [value]
  (try
    (. asyncio (get-running-loop))
    (return (-/promise-reject
             (Exception "cannot synchronously settle awaitable while an event loop is already running")))
    (catch [RuntimeError :as _]
      (if (. asyncio (isfuture value))
        (let [loop (. value (get-loop))]
          (try
            (return {"__type__" "xt.promise"
                     "status" "resolved"
                     "value" (. loop (run-until-complete value))})
            (catch [Exception :as e]
              (return (-/promise-reject e)))))
        (try
          (return {"__type__" "xt.promise"
                   "status" "resolved"
                   "value" (. asyncio (run value))})
          (catch [Exception :as e]
            (return (-/promise-reject e))))))))

(defn.py promise-wrap
  "normalises plain values, wrappers, and awaitables into the runtime promise model"
  {:added "4.1"}
  [value]
  (if (-/promise-wrapper? value)
    (return value)
    (if (or (. asyncio (isfuture value))
            (. inspect (isawaitable value)))
      (return (-/promise-awaitable value))
      (return {"__type__" "xt.promise"
               "status" "resolved"
               "value" value}))))

(defn.py promise
  "executes a thunk and captures either its value or error as a promise"
  {:added "4.1"}
  [thunk]
  (try
    (return (-/promise-wrap (thunk)))
    (catch [Exception :as e]
      (return (-/promise-reject e)))))

(defn.py promise-then
  "applies a continuation to resolved promises, adopting awaitables when needed"
  {:added "4.1"}
  [promise thunk]
  (var current (-/promise-wrap promise))
  (if (== "rejected" (. current ["status"]))
    (return current)
    (try
      (return (-/promise-wrap (thunk (. current ["value"]))))
      (catch [Exception :as e]
        (return (-/promise-reject e))))))

(defn.py promise-catch
  "applies a continuation to rejected promises, adopting awaitables when needed"
  {:added "4.1"}
  [promise thunk]
  (var current (-/promise-wrap promise))
  (if (not= "rejected" (. current ["status"]))
    (return current)
    (try
      (return (-/promise-wrap (thunk (. current ["error"]))))
      (catch [Exception :as e]
        (return (-/promise-reject e))))))

(defn.py promise-finally
  "runs a finalizer and preserves the original promise unless the finalizer fails"
  {:added "4.1"}
  [promise thunk]
  (var current (-/promise-wrap promise))
  (try
    (var cleanup-value (thunk))
    (var cleanup (-/promise-wrap cleanup-value))
    (if (== "rejected" (. cleanup ["status"]))
      (return cleanup)
      (return current))
    (catch [Exception :as e]
      (return (-/promise-reject e)))))
