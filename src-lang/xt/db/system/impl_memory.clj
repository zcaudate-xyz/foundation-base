(ns xt.db.system.impl-memory
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :as proto :refer [defimpl.xt]]))

(l/script :xtalk
  {:require [[xt.db.system.impl-common :as impl-common]
             [xt.db.system.memory-util :as util]
             [xt.db.system.memory-graph :as graph]
             [xt.db.text.base-flatten :as f]
             [xt.lang.common-protocol :as proto]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(defn.xt pull
  "fetches tree ir data from the memory impl"
  {:added "4.1"}
  [impl tree]
  (var #{rows schema opts} impl)
  (return (graph/pull rows
                      schema
                      tree
                      opts)))

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
  (var #{rows schema opts} impl)
  (return
   (util/add-bulk rows schema {table-name records})))


(defn.xt record-delete
  "deletes ids directly from a single table in the memory impl"
  {:added "4.1"}
  [impl table-name ids]
  (var #{rows schema opts} impl)
  (return (util/remove-bulk rows
                            schema
                            table-name
                            ids)))


;;
;; PROCESS
;;

(defn.xt process-add-event
  [impl data]
  (var #{rows schema} impl)
  (return
   (util/add-bulk rows schema data)))

(defn.xt process-remove-event
  [impl data]
  (var #{rows schema lookup} impl)
  (var ordered (f/flatten-bulk-ids schema lookup data))
  (xt/for:array [entry ordered]
    (var [table-name ids] entry)
    (util/remove-bulk rows schema table-name ids))
  (return (xt/x:arr-map ordered xt/x:first)))

(defn.xt clear-db
  "clears all rows from the memory impl"
  {:added "4.1"}
  [impl]
  (var #{rows} impl)
  (xt/for:array [table-key (xt/x:obj-keys rows)]
    (xt/x:del-key rows table-key))
  (return nil))

(defn.xt rpc-call-async
  "memory impl does not support remote rpc calls"
  {:added "4.1"}
  [_impl _rpc-spec _args]
  (xt/x:err "db.impl.memory does not support rpc_call_async"))

;;
;; IMPL
;;

(defimpl.xt ImplMemory
  [rows schema lookup listeners]

  impl-common/ISourceLocal
  {impl-common/clear-db             -/clear-db
   impl-common/pull                 -/pull
   impl-common/record-add           -/record-add
   impl-common/record-delete        -/record-delete
   impl-common/process-add-event    -/process-add-event
   impl-common/process-remove-event -/process-remove-event}

  impl-common/ISourceRemote
  {impl-common/pull-async      -/pull-async
   impl-common/rpc-call-async  -/rpc-call-async}

  impl-common/ISourceListener
  {impl-common/add-db-listener     impl-common/add-db-listener-default
   impl-common/remove-db-listener  impl-common/remove-db-listener-default
   impl-common/get-db-listener     impl-common/get-db-listener-default})

(defn.xt impl-memory
  [schema lookup]
  (return
   (-/ImplMemory {} schema lookup {})))
