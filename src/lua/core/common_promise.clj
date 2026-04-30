(ns lua.core.common-promise
  (:require [std.lang :as l])
  (:refer-clojure :exclude [promise]))

(l/script :lua
  {:require [[xt.lang.spec-base :as xt]]})

(defn.lua promise-native?
  "checks whether the value is a native runtime promise wrapper"
  {:added "4.1"}
  [value]
  (return (and (== "table" (type value))
               (== "xt.promise" (. value ["__type__"])))))

(defn.lua promise-resolve
  "wraps plain values as resolved promises"
  {:added "4.1"}
  [value]
  (if (-/promise-native? value)
    (return value)
    (return {"__type__" "xt.promise"
             "status" "resolved"
             "value" value})))

(defn.lua promise-reject
  "wraps thrown values as rejected promises"
  {:added "4.1"}
  [err]
  (return {"__type__" "xt.promise"
           "status" "rejected"
           "error" err}))

(defn.lua async-run
  "executes a thunk and captures either its value or error as a promise"
  {:added "4.1"}
  [thunk]
  (var out [(pcall thunk)])
  (if (. out [1])
    (return (-/promise-resolve (. out [2])))
    (return (-/promise-reject (. out [2])))))

(defn.lua async-bind
  "binds success and error continuations onto a promise-like value"
  {:added "4.1"}
  [promise on-resolve on-reject]
  (var current (-/promise-resolve promise))
  (if (== "rejected" (. current ["status"]))
    (if (== nil on-reject)
      (return current)
      (do (var out [(pcall on-reject (. current ["error"]))])
          (if (. out [1])
            (return (-/promise-resolve (. out [2])))
            (return (-/promise-reject (. out [2]))))))
    (if (== nil on-resolve)
      (return current)
      (do (var out [(pcall on-resolve (. current ["value"]))])
          (if (. out [1])
            (return (-/promise-resolve (. out [2])))
            (return (-/promise-reject (. out [2]))))))))

(defn.lua promise
  "executes a thunk and captures either its value or error as a promise"
  {:added "4.1"}
  [thunk]
  (return (-/async-run thunk)))

(defn.lua promise-all
  "waits for all values in an array and short-circuits on rejection"
  {:added "4.1"}
  [promises]
  (:= promises (:? (== nil promises) [] promises))
  (var out [])
  (xt/for:array [value promises]
    (var current (-/promise-resolve value))
    (if (== "rejected" (. current ["status"]))
      (return current))
    (table.insert out (. current ["value"])))
  (return (-/promise-resolve out)))

(defn.lua promise-then
  "applies a continuation to resolved promises"
  {:added "4.1"}
  [promise thunk]
  (return (-/async-bind promise thunk nil)))

(defn.lua promise-catch
  "applies a continuation to rejected promises"
  {:added "4.1"}
  [promise thunk]
  (return (-/async-bind promise nil thunk)))

(defn.lua promise-finally
  "runs a finalizer and preserves the original promise unless the finalizer fails"
  {:added "4.1"}
  [promise thunk]
  (var current (-/promise-resolve promise))
  (var out [(pcall thunk)])
  (if (. out [1])
    (return current)
    (return (-/promise-reject (. out [2])))))

(defn.lua with-delay
  "sleeps before invoking a thunk, returning a promise and accepting either (ms thunk) or (thunk ms)"
  {:added "4.1"}
  [a b]
  (var thunk (:? (== "function" (type a)) a b))
  (var ms (:? (== "function" (type a)) b a))
  (return (-/promise
           (fn []
             (var socket (require "socket"))
             (socket.sleep (/ ms 1000.0))
             (return (thunk))))))
