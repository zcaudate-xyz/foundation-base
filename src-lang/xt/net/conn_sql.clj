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
  (var #{impl raw} client)
  (var disconnect-fn (. impl ["disconnect"]))
  (return (disconnect-fn raw)))

(defn.xt connection-query
  [client input]
  (var #{impl raw} client)
  (var query-fn (or (. impl ["query"])
                    (. impl ["query_sync"])))
  (return (query-fn raw input)))

(defn.xt connection-query-async
  [client input]
  (var #{impl raw} client)
  (var query-async-fn (or (. impl ["query_async"])
                          (. impl ["query"])))
  (return (query-async-fn raw input)))

(defn.xt connection-connect
  [client opts]
  (var #{impl} client)
  (var connect-fn (. impl ["connect"]))
  (if (xt/x:nil? connect-fn)
    (return client)
    (return (connect-fn (. client ["raw"]) opts))))

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
