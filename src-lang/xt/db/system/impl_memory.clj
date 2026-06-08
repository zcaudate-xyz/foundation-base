(ns xt.db.system.impl-memory
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.system.impl-common :as impl-common]
             [xt.db.system.memory-util :as util]
             [xt.db.system.memory-graph :as graph]
             [xt.db.text.base-flatten :as f]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(defn.xt pull
  "fetches tree ir data from the memory client"
  {:added "4.1"}
  [client tree]
  (var #{rows schema opts} client)
  (return (graph/pull rows
                      schema
                      tree
                      opts)))

(defn.xt pull-sync
  "fetches tree ir data synchronously from the memory client"
  {:added "4.1"}
  [client tree]
  (return (-/pull client tree)))

(defn.xt pull-async
  "fetches tree ir data with async semantics"
  {:added "4.1"}
  [client tree]
  (return
   (promise/x:promise-run
    (-/pull client tree))))

(defn.xt record-add
  "adds records directly to a single table in the memory client"
  {:added "4.1"}
  [client table-name records]
  (var #{rows schema opts} client)
  (return
   (util/add-bulk rows schema {table-name records})))

(defn.xt record-add-async
  "adds records directly with async semantics"
  {:added "4.1"}
  [client table-name records]
  (return
   (promise/x:promise-run
    (-/record-add client table-name records))))

(defn.xt record-delete
  "deletes ids directly from a single table in the memory client"
  {:added "4.1"}
  [client table-name ids]
  (var #{rows schema opts} client)
  (return (util/remove-bulk rows
                            schema
                            table-name
                            ids)))

(defn.xt record-delete-async
  "deletes ids directly with async semantics"
  {:added "4.1"}
  [client table-name ids]
  (return
   (promise/x:promise-run
    (-/record-delete client table-name ids))))


;;
;;
;;

(defn.xt process-add-event
  [client data]
  (var #{rows schema} client)
  (return
   (util/add-bulk rows schema data)))

(defn.xt process-remove-event
  [client data]
  (var #{rows schema lookup} client)
  (var ordered (f/flatten-bulk-ids schema lookup data))
  (xt/for:array [entry ordered]
    (var [table-name ids] entry)
    (util/remove-bulk rows schema table-name ids))
  (return (xt/x:arr-map ordered xt/x:first)))

(defn.xt client-memory
  [schema lookup]
  (return
   (xt/x:obj-assign
    (impl-common/impl-base "db.impl.memory" schema lookup nil)
    {"rows" {}})))
