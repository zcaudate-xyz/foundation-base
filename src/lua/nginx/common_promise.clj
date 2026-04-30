(ns lua.nginx.common-promise
  (:require [std.lang :as l])
  (:refer-clojure :exclude [promise]))

(l/script :lua.nginx
  {:require [[xt.lang.spec-base :as xt]]})

(defn.lua promise-wrapper?
  "checks whether the value is a native runtime promise wrapper"
  {:added "4.1"}
  [value]
  (return (and (== "table" (type value))
               (== "xt.promise" (. value ["__type__"])))))

(defn.lua promise-native?
  "checks whether the value is a native nginx promise wrapper"
  {:added "4.1"}
  [value]
  (return (-/promise-wrapper? value)))

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

(defn.lua promise-pending
  "creates a pending promise backed by an nginx light thread"
  {:added "4.1"}
  [thread]
  (return {"__type__" "xt.promise"
           "status" "pending"
           "thread" thread}))

(defn.lua promise-settle
  "updates a pending promise with its settled status"
  {:added "4.1"}
  [wrapper status value]
  (:= (. wrapper ["status"]) status)
  (:= (. wrapper ["thread"]) nil)
  (if (== "resolved" status)
    (do (:= (. wrapper ["value"]) value)
        (:= (. wrapper ["error"]) nil))
    (do (:= (. wrapper ["error"]) value)
        (:= (. wrapper ["value"]) nil)))
  (return wrapper))

(defn.lua promise-await
  "waits for pending nginx promises and adopts nested promise results"
  {:added "4.1"}
  [value]
  (var current (-/promise-resolve value))
  (if (not= "pending" (. current ["status"]))
    (return current))
  (var thread (. current ["thread"]))
  (var '[ok out] (ngx.thread.wait thread))
  (if ok
    (if (-/promise-native? out)
      (do (var settled (-/promise-await out))
          (if (== "rejected" (. settled ["status"]))
            (return (-/promise-settle current "rejected" (. settled ["error"])))
            (return (-/promise-settle current "resolved" (. settled ["value"])))))
      (return (-/promise-settle current "resolved" out)))
    (return (-/promise-settle current "rejected" out))))

(defn.lua async-run
  "executes a thunk in an nginx light thread and returns a pending promise"
  {:added "4.1"}
  [thunk]
  (return (-/promise-pending
           (ngx.thread.spawn thunk))))

(defn.lua async-bind
  "binds success and error continuations onto a promise-like value"
  {:added "4.1"}
  [promise on-resolve on-reject]
  (return (-/promise-pending
           (ngx.thread.spawn
            (fn []
              (var current (-/promise-await promise))
              (if (== "rejected" (. current ["status"]))
                (if (== nil on-reject)
                  (return current)
                  (return (on-reject (. current ["error"]))))
                (if (== nil on-resolve)
                  (return current)
                  (return (on-resolve (. current ["value"]))))))))))

(defn.lua promise
  "executes a thunk in an nginx light thread and returns a pending promise"
  {:added "4.1"}
  [thunk]
  (return (-/async-run thunk)))

(defn.lua promise-all
  "waits for all values in an array and preserves nginx async chaining"
  {:added "4.1"}
  [promises]
  (return (-/promise-pending
           (ngx.thread.spawn
            (fn []
              (:= promises (:? (== nil promises) [] promises))
              (var out [])
              (xt/for:array [value promises]
                (var current (-/promise-await value))
                (if (== "rejected" (. current ["status"]))
                  (return current))
                (table.insert out (. current ["value"])))
              (return out))))))

(defn.lua promise-then
  "applies a continuation to resolved promises while preserving async chaining"
  {:added "4.1"}
  [promise thunk]
  (return (-/async-bind promise thunk nil)))

(defn.lua promise-catch
  "applies a continuation to rejected promises while preserving async chaining"
  {:added "4.1"}
  [promise thunk]
  (return (-/async-bind promise nil thunk)))

(defn.lua promise-finally
  "runs a finalizer and preserves the original promise unless cleanup rejects"
  {:added "4.1"}
  [promise thunk]
  (return (-/promise-pending
           (ngx.thread.spawn
            (fn []
              (var current (-/promise-await promise))
              (var cleanup (-/promise-await (thunk)))
              (if (== "rejected" (. cleanup ["status"]))
                (return cleanup)
                (return current)))))))

(defn.lua with-delay
  "sleeps before invoking a thunk, returning a promise and accepting either (ms thunk) or (thunk ms)"
  {:added "4.1"}
  [a b]
  (var thunk (:? (== "function" (type a)) a b))
  (var ms (:? (== "function" (type a)) b a))
  (return (-/promise
           (fn []
             (ngx.sleep (/ ms 1000.0))
             (return (thunk))))))
