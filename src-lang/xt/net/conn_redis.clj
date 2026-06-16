(ns xt.net.conn-redis
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :refer [defprotocol.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-protocol :as proto]]})

(defprotocol.xt IRedisClient
  (connect [client opts])
  (disconnect [client])
  (exec [client command args]))
