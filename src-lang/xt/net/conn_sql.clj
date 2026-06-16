(ns xt.net.conn-sql
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :as proto :refer [defprotocol.xt defimpl.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-protocol :as proto]]})

(defprotocol.xt ISqlClient
  (connect [client opts])
  (disconnect [client])
  (query [client input])
  (query-async [client input]))

(defn.xt connection-disconnect
  [client]
  (var impl (xt/x:get-key client "impl"))
  (var raw  (xt/x:get-key client "raw"))
  (var disconnect-fn (xt/x:get-key impl "disconnect"))
  (return (disconnect-fn raw)))

(defn.xt connection-query
  [client input]
  (var impl (xt/x:get-key client "impl"))
  (var raw  (xt/x:get-key client "raw"))
  (var query-fn (or (xt/x:get-key impl "query")
                    (xt/x:get-key impl "query_sync")))
  (return (query-fn raw input)))

(defn.xt connection-query-async
  [client input]
  (var impl (xt/x:get-key client "impl"))
  (var raw  (xt/x:get-key client "raw"))
  (var query-async-fn (or (xt/x:get-key impl "query_async")
                          (xt/x:get-key impl "query")))
  (return (query-async-fn raw input)))

(defn.xt connection-connect
  [client opts]
  (var impl (xt/x:get-key client "impl"))
  (var connect-fn (xt/x:get-key impl "connect"))
  (if (xt/x:nil? connect-fn)
    (return client)
    (return (connect-fn (xt/x:get-key client "raw") opts))))

(defimpl.xt SqlConnection
  [raw impl]
  -/ISqlClient
  {connect     -/connection-connect
   disconnect  -/connection-disconnect
   query       -/connection-query
   query-async -/connection-query-async})

(defn.xt connection-create
  "creates a generic sql connection from a raw value and method map"
  {:added "4.1"}
  [raw impl]
  (return (-/SqlConnection raw impl)))
