(ns xt.db.system.impl-common
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :as proto :refer [defprotocol.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-protocol :as proto]]})

(defprotocol.xt ISourceListener
  (add-db-listener    [impl listener-id handle])
  (remove-db-listener [impl listener-id])
  (get-db-listener    [impl listener-id]))

(defprotocol.xt ISourceRemote
  (pull-async [impl tree])
  (rpc-call-async [impl rpc-spec args]))

(defprotocol.xt ISourceLocal
  (clear-db      [impl])
  (pull          [impl tree])
  (record-add    [impl table-name records])
  (record-delete [impl table-name ids])
  (process-add-event [impl data])
  (process-remove-event [impl data]))

(defn.xt add-db-listener-default
  [impl listener-id handle]
  (var #{listeners} impl)
  (xt/x:set-key listeners listener-id handle)
  (return listener-id))

(defn.xt remove-db-listener-default
  [impl listener-id]
  (var #{listeners} impl)
  (xt/x:del-key listeners listener-id)
  (return listener-id))

(defn.xt get-db-listener-default
  [impl listener-id handle]
  (var #{listeners} impl)
  (return
   (xt/x:get-key listeners listener-id)))
