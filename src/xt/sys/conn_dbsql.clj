(ns xt.sys.conn-dbsql
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]]})

(defn.xt connect
  "connects to a database"
  {:added "4.0"}
  [m cb]
  (var #{constructor} m)
  (var success-fn (k/wrap-callback cb "success"))
  (var error-fn   (k/wrap-callback cb "error"))
  (k/for:return [[conn err] (constructor m (x:callback))]
    {:success (return (success-fn conn))
     :error   (return (error-fn err))
     :final   true}))

(defn.xt disconnect
  "disconnects form database"
  {:added "4.0"}
  [conn cb]
  (var disconnect-fn (k/get-key conn "::disconnect"))
  (var success-fn (k/wrap-callback cb "success"))
  (var error-fn   (k/wrap-callback cb "error"))
  (k/for:return [[res err] (disconnect-fn (x:callback))]
    {:success (return (success-fn res))
     :error   (return (error-fn err))
     :final   true}))

(defn.xt query-base
  "calls query without the wrapper"
  {:added "4.0"}
  [conn raw]
  (var query-fn (k/get-key conn "::query"))
  (return (query-fn raw)))

(defn.xt query
  "sends a query"
  {:added "4.0"}
  [conn raw cb]
  (var query-fn (k/get-key conn "::query"))
  (var success-fn (k/wrap-callback cb "success"))
  (var error-fn   (k/wrap-callback cb "error"))
  (k/for:return [[res err] (query-fn raw (x:callback))]
    {:success (return (success-fn res))
     :error   (return (error-fn err))
     :final   true}))

(defn.xt query-sync
  "sends a synchronous query"
  {:added "4.0"}
  [conn raw]
  (var query-fn (or (k/get-key conn "::query_sync")
                    (k/get-key conn "::query")))
  (return (query-fn raw)))

