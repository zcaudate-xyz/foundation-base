(ns xt.sys.conn-dbsql
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-lib :as xtl]]})

(defn.xt connect
  "connects to a database"
  {:added "4.0"}
  [m cb]
  (var #{constructor} m)
  (var success-fn (xtl/wrap-callback cb "success"))
  (var error-fn   (xtl/wrap-callback cb "error"))
  (xt/for:return [[conn err] (constructor m (xt/x:callback))]
    {:success (return (success-fn conn))
     :error   (return (error-fn err))
     :final   true}))

(defn.xt disconnect
  "disconnects form database"
  {:added "4.0"}
  [conn cb]
  (var disconnect-fn (xt/x:get-key conn "::disconnect"))
  (var success-fn (xtl/wrap-callback cb "success"))
  (var error-fn   (xtl/wrap-callback cb "error"))
  (xt/for:return [[res err] (disconnect-fn (xt/x:callback))]
    {:success (return (success-fn res))
     :error   (return (error-fn err))
     :final   true}))

(defn.xt query-base
  "calls query without the wrapper"
  {:added "4.0"}
  [conn raw]
  (var query-fn (xt/x:get-key conn "::query"))
  (return (query-fn raw)))

(defn.xt query
  "sends a query"
  {:added "4.0"}
  [conn raw cb]
  (var query-fn (xt/x:get-key conn "::query"))
  (var success-fn (xtl/wrap-callback cb "success"))
  (var error-fn   (xtl/wrap-callback cb "error"))
  (xt/for:return [[res err] (query-fn raw (xt/x:callback))]
    {:success (return (success-fn res))
     :error   (return (error-fn err))
     :final   true}))

(defn.xt query-sync
  "sends a synchronous query"
  {:added "4.0"}
  [conn raw]
  (var query-fn (or (xt/x:get-key conn "::query_sync")
                    (xt/x:get-key conn "::query")))
  (return (query-fn raw)))

