(ns xt.db.system.impl-common
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :as proto :refer [defprotocol.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-protocol :as proto]]})

(defprotocol.xt ISourceRemote
  (pull-async [impl tree])
  (rpc-call-async [impl rpc-spec args]))

(defprotocol.xt ISourceLocal
  (pull [impl tree])
  (record-add    [impl table-name records])
  (record-delete [impl table-name ids])
  (process-add-event [impl data])
  (process-remove-event [impl data]))

