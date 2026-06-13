(ns xt.net.conn-sql
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :refer [defprotocol.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-protocol :as proto]]})

(defprotocol.xt ISqlClient
  (connect [client opts])
  (disconnect [client])
  (query [client input])
  (query-async [client input]))


