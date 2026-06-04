(ns xt.db.system.impl-memory
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.system.impl-common :as impl-common]
             [xt.db.system.memory-store :as store]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(defn.xt pull-sync
  "fetches tree ir data from the memory client"
  {:added "4.1"}
  [client tree]
  (var #{schema opts} client)
  (return (store/fetch-sync client schema tree opts)))

(defn.xt pull
  "fetches tree ir data with async semantics"
  {:added "4.1"}
  [client tree]
  (return
   (promise/x:promise-run
    (-/pull-sync client tree))))

(defn.xt record-add-sync
  "adds records directly to a single table in the memory client"
  {:added "4.1"}
  [client table-name records]
  (var #{schema opts} client)
  (return (store/set-sync client schema {table-name records} opts)))

(defn.xt record-add
  "adds records directly with async semantics"
  {:added "4.1"}
  [client table-name records]
  (var #{schema opts} client)
  (return
   (promise/x:promise-run
    (-/record-add-sync client table-name records))))

(defn.xt record-delete-sync
  "deletes ids directly from a single table in the memory client"
  {:added "4.1"}
  [client table-name ids]
  (var #{schema opts} client)
  (return (store/delete-sync client schema table-name ids opts)))

(defn.xt record-delete
  "deletes ids directly with async semantics"
  {:added "4.1"}
  [client table-name ids]
  (return
   (promise/x:promise-run
    (-/record-delete-sync client table-name ids))))

(defn.xt memory-client
  [schema lookup opts]
  (return
   (xt/x:obj-assign
    (impl-common/client-base "db.client.memory" schema lookup opts)
    {"rows" {}})))





