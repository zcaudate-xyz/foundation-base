(ns xt.db.system.impl-common
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :as proto :refer [defprotocol.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-protocol :as proto]]})

(defprotocol.xt ISourceListener
  (add-db-listener    [impl listener-id handle])
  (remove-db-listener [impl listener-id])
  (get-db-listener    [impl listener-id]))

(defprotocol.xt ISourceRemote
  (pull-async [impl tree])
  (rpc-call-async [impl rpc-spec args]))

(defprotocol.xt ISourceRealtime
  (subscribe-db [impl conn-id topics])
  (unsubscribe-db [impl conn-id  topics]))

(defprotocol.xt ISourceLocal
  (clear-db      [impl])
  (pull          [impl tree])
  (record-add    [impl table-name records])
  (record-delete [impl table-name ids])
  (process-add-event [impl data])
  (process-remove-event [impl data]))

(defprotocol.xt ISourceLifecycle
  (stop-db [impl]))

;;
;; 
;;

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

(defn.xt sync-get-tables
  "Extracts table names present in db/sync or db/remove."
  {:added "4.1"}
  [payload]
  (var out {})
  (var db-sync   (xt/x:get-key payload "db/sync"))
  (var db-remove (xt/x:get-key payload "db/remove"))
  (when (xt/x:is-object? db-sync)
    (xt/for:object [[table _] db-sync]
      (xt/x:set-key out table true)))
  (when (xt/x:is-object? db-remove)
    (xt/for:object [[table _] db-remove]
      (xt/x:set-key out table true)))
  (return (xt/x:obj-keys out)))

(defn.xt sync-notify-listeners
  "Notifies db listeners whose table guard matches any changed table.

   A listener's \"guard\" may be:
   - a function (legacy): (fn [table] bool)
   - a map/object:        {<table> true|false|(fn [table-payload] bool)}

   For map guards the function receives the table's payload object:
   {\"db/sync\" [...] \"db/remove\" [...]}."
  {:added "4.1"}
  [impl tables event]
  (var #{listeners} impl)
  (xt/for:object [[listener-id handle] listeners]
    (var guard    (xt/x:get-key handle "guard"))
    (var callback (xt/x:get-key handle "callback"))
    (var matched false)
    (when (xt/x:is-function? guard)
      (xt/for:array [table tables]
        (when (guard table)
          (:= matched true))))
    (when (xt/x:is-object? guard)
      (xt/for:array [table tables]
        (var table-guard (xt/x:get-key guard table))
        (when (xt/x:is-function? table-guard)
          (var table-payload {"db/sync"   (xtd/get-in event ["db/sync" table])
                              "db/remove" (xtd/get-in event ["db/remove" table])})
          (when (table-guard table-payload)
            (:= matched true)))
        (when (and table-guard (not (xt/x:is-function? table-guard)))
          (:= matched true))))
    (when matched
      (callback event)))
  (return true))

(defn.xt sync-process-payload
  "Applies a sync payload to a db implementation and notifies listeners.

   Steps:
   1. Apply db/sync records via process-add-event.
   2. Apply db/remove ids via process-remove-event.
   3. Extract the affected table names with sync-get-tables.
   4. Notify any db listeners whose guard matches those tables."
  {:added "4.1.4"}
  [impl payload]
  (var db-sync   (xt/x:get-key payload "db/sync"))
  (var db-remove (xt/x:get-key payload "db/remove"))
  (when (and (xt/x:is-object? db-sync)
             (xtd/not-empty? db-sync))
    (-/process-add-event impl db-sync))
  (when (and (xt/x:is-object? db-remove)
             (xtd/not-empty? db-remove))
    (xt/for:object [[table-name ids] db-remove]
      (-/record-delete impl table-name ids)))
  (var tables (-/sync-get-tables payload))
  (when (> (xt/x:len tables) 0)
    (-/sync-notify-listeners impl tables payload))
  (return true))
