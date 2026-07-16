(ns xt.db.system.impl-supabase-ws
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :refer [defprotocol.xt]]))

(l/script :xtalk
  {:require [[xt.lang.common-protocol :as proto]]})

(defprotocol.xt ISupabaseWebsocketFactory
  (create-ws-client [impl defaults]))
