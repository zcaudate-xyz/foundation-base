(ns xt.db.system.impl-sqlite
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :as proto :refer [defimpl.xt]]))

(l/script :xtalk
  {:require [[xt.db.system.impl-common :as impl-common]
             [xt.db.text.base-flatten :as f]
             [xt.db.text.sql-graph :as sql-graph]
             [xt.db.text.sql-table :as sql-table]
             [xt.db.text.sql-util :as sql-util]
             [xt.db.text.sql-manage :as manage]
             [xt.db.text.sql-raw :as raw]
             [xt.lang.common-protocol :as proto]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.conn-sql :as conn-sql]]})

(defn.xt pull
  "runs a tree ir pull against a sqlite impl"
  {:added "4.1"}
  [impl tree]
  (var #{client
         schema
         opts} impl)
  (return
   (conn-sql/query client (sql-graph/select schema tree opts))))

(defn.xt pull-async
  "runs a tree ir pull with async sqlite semantics"
  {:added "4.1"}
  [impl tree]
  (return
   (promise/x:promise-run (-/pull impl tree))))

(defn.xt record-add
  "adds rows directly through stored sqlite context"
  {:added "4.1"}
  [impl table-name records]
  (var #{client
         schema
         lookup
         opts} impl)
  (var input (sql-table/prepare-add-input {table-name records}
                                          schema
                                          lookup
                                          opts))
  (when (== "" input)
    (return nil))
  (return
   (conn-sql/query client input)))

(defn.xt record-delete
  "deletes ids directly through stored sqlite context"
  {:added "4.1"}
  [impl table-name ids]
  (var #{client
         opts} impl)
  (var statements
       (xt/x:arr-map ids
                     (fn [id]
                       (return (raw/raw-delete table-name {"id" id} opts)))))
  (return
   (conn-sql/query client (xt/x:str-join "\n\n" statements))))



;;
;; PROCESS
;;

(defn.xt process-add-event
  "processes nested data into sqlite upserts"
  {:added "4.1"}
  [impl data]
  (var #{client
         schema
         lookup
         opts} impl)
  (var flat (f/flatten-bulk schema data))
  (conn-sql/query client
                  (sql-table/prepare-add-input data schema lookup opts))
  (return (xt/x:obj-keys flat)))

(defn.xt process-remove-event
  "processes nested removals into sqlite delete statements"
  {:added "4.1"}
  [impl data]
  (var #{client
         schema
         lookup
         opts} impl)
  (var ordered (f/flatten-bulk-ids schema lookup data))
  (conn-sql/query client
                  (sql-table/prepare-remove-input data schema lookup opts))
  (return (xt/x:arr-map ordered xt/x:first)))

(defn.xt clear-db
  "clears all tables from the sqlite impl and recreates the schema"
  {:added "4.1"}
  [impl]
  (var #{client
         schema
         lookup
         opts} impl)
  (conn-sql/query client
                  (xt/x:str-join "\n\n"
                                 (manage/table-drop-all schema lookup opts)))
  (conn-sql/query client
                  (xt/x:str-join "\n\n"
                                 (manage/table-create-all schema lookup opts)))
  (return nil))

(defn.xt rpc-call-async
  "sqlite impl does not support remote rpc calls"
  {:added "4.1"}
  [_impl _rpc-spec _args]
  (xt/x:err "ImplSqlite does not support rpc_call_async"))


;;
;; IMPL
;;


(defimpl.xt ImplSqlite
  [client schema lookup listeners opts]

  impl-common/ISourceLocal
  {impl-common/clear-db             -/clear-db
   impl-common/pull                 -/pull
   impl-common/record-add           -/record-add
   impl-common/record-delete        -/record-delete
   impl-common/process-add-event    -/process-add-event
   impl-common/process-remove-event -/process-remove-event}
  
  impl-common/ISourceRemote
  {impl-common/pull-async     -/pull-async
   impl-common/rpc-call-async -/rpc-call-async}

  impl-common/ISourceListener
  {impl-common/add-db-listener     impl-common/add-db-listener-default
   impl-common/remove-db-listener  impl-common/remove-db-listener-default
   impl-common/get-db-listener     impl-common/get-db-listener-default})

(defn.xt impl-sqlite
  [client schema lookup]
  (return
   (-/ImplSqlite client schema lookup  listeners (sql-util/sqlite-opts lookup))))

(defn.xt impl-sqlite-init
  [impl]
  (var #{client
         schema
         lookup
         opts} impl)
  (return
   (-> (conn-sql/connect client)
       (promise/x:promise-then
        (fn [client]
          (conn-sql/query client (xt/x:str-join
                                  "\n\n"
                                  (manage/table-create-all schema lookup opts)))
          (return impl))))))
