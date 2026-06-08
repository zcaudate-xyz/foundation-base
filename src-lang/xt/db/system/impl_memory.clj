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
  "fetches tree ir data from the memory impl"
  {:added "4.1"}
  [impl tree]
  (var #{client schema opts} impl)
  (return (graph/pull client
                      schema
                      tree
                      opts)))

(defn.xt pull-sync
  "fetches tree ir data synchronously from the memory impl"
  {:added "4.1"}
  [impl tree]
  (return (-/pull impl tree)))

(defn.xt pull-async
  "fetches tree ir data with async semantics"
  {:added "4.1"}
  [impl tree]
  (return
   (promise/x:promise-run
    (-/pull impl tree))))

(defn.xt record-add
  "adds records directly to a single table in the memory impl"
  {:added "4.1"}
  [impl table-name records]
  (var #{client schema opts} impl)
  (return
   (util/add-bulk client schema {table-name records})))

(defn.xt record-add-async
  "adds records directly with async semantics"
  {:added "4.1"}
  [impl table-name records]
  (return
   (promise/x:promise-run
    (-/record-add impl table-name records))))

(defn.xt record-delete
  "deletes ids directly from a single table in the memory impl"
  {:added "4.1"}
  [impl table-name ids]
  (var #{client schema opts} impl)
  (return (util/remove-bulk client
                            schema
                            table-name
                            ids)))

(defn.xt record-delete-async
  "deletes ids directly with async semantics"
  {:added "4.1"}
  [impl table-name ids]
  (return
   (promise/x:promise-run
    (-/record-delete impl table-name ids))))


;;
;; PROCESS
;;

(defn.xt process-add-event
  [impl data]
  (var #{client schema} impl)
  (return
   (util/add-bulk client schema data)))

(defn.xt process-remove-event
  [impl data]
  (var #{client schema lookup} impl)
  (var ordered (f/flatten-bulk-ids schema lookup data))
  (xt/for:array [entry ordered]
    (var [table-name ids] entry)
    (util/remove-bulk client schema table-name ids))
  (return (xt/x:arr-map ordered xt/x:first)))

;;
;; IMPL
;;

(defn.xt impl-memory
  [schema lookup]
  (return
   (impl-common/impl-base "db.impl.memory" {} schema lookup)))
