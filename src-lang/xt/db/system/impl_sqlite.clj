(ns xt.db.system.impl-sqlite
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.system.impl-common :as impl-common]
             [xt.db.text.base-flatten :as f]
             [xt.db.text.sql-graph :as sql-graph]
             [xt.db.text.sql-table :as sql-table]
             [xt.db.text.sql-util :as sql-util]
             [xt.db.text.sql-manage :as manage]
             [xt.db.text.sql-raw :as raw]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.conn-sql :as conn-sql]]})

(defn.xt pull
  "runs a tree ir pull against a sqlite client"
  {:added "4.1"}
  [client tree]
  (var #{instance
         schema
         opts} client)
  (var output
       (conn-sql/query instance
                       (sql-graph/select schema tree opts)))
  (when (xt/x:is-string? output)
    (xt/x:err "SQL pull expected decoded structured data"))
  (return output))

(defn.xt pull-async
  "runs a tree ir pull with async sqlite semantics"
  {:added "4.1"}
  [client tree]
  (var #{instance
         schema
         opts} client)
  (return
   (conn-sql/query-async instance
                         (sql-graph/select schema tree opts))))

(defn.xt record-add
  "adds rows directly through stored sqlite context"
  {:added "4.1"}
  [client table-name records]
  (var #{instance
         schema
         lookup
         opts} client)
  (var input (sql-table/prepare-add-input {table-name records}
                                          schema
                                          lookup
                                          opts))
  (when (== "" input)
    (return nil))
  (return
   (conn-sql/query instance input)))

(defn.xt record-add-async
  "adds rows directly with async sqlite semantics"
  {:added "4.1"}
  [client table-name records]
  (var #{instance
         schema
         lookup
         opts} client)
  (var input (sql-table/prepare-add-input {table-name records}
                                          schema
                                          lookup
                                          opts))
  (when (== "" input)
    (return (promise/x:promise-run nil)))
  (return
   (conn-sql/query-async instance input)))

(defn.xt record-delete
  "deletes ids directly through stored sqlite context"
  {:added "4.1"}
  [client table-name ids]
  (var #{instance
         opts} client)
  (var statements
       (xt/x:arr-map ids
                     (fn [id]
                       (return (raw/raw-delete table-name {"id" id} opts)))))
  (return
   (conn-sql/query instance (xt/x:str-join "\n\n" statements))))

(defn.xt record-delete-async
  "deletes ids directly with async sqlite semantics"
  {:added "4.1"}
  [client table-name ids]
  (var #{instance
         opts} client)
  (var statements
       (xt/x:arr-map ids
                     (fn [id]
                       (return (raw/raw-delete table-name {"id" id} opts)))))
  (return
   (conn-sql/query-async instance
                         (xt/x:str-join "\n\n" statements))))

(defn.xt process-add-event
  "processes nested data into sqlite upserts"
  {:added "4.1"}
  [client data]
  (var #{instance
         schema
         lookup
         opts} client)
  (var flat (f/flatten-bulk schema data))
  (conn-sql/query instance
                  (sql-table/prepare-add-input data schema lookup opts))
  (return (xt/x:obj-keys flat)))


;;
;; PROCESS
;;

(defn.xt process-remove-event
  "processes nested removals into sqlite delete statements"
  {:added "4.1"}
  [client data]
  (var #{instance
         schema
         lookup
         opts} client)
  (var ordered (f/flatten-bulk-ids schema lookup data))
  (conn-sql/query instance
                  (sql-table/prepare-remove-input data schema lookup opts))
  (return (xt/x:arr-map ordered xt/x:first)))

(defn.xt exec-sync
  "executes raw sql synchronously through the sqlite client"
  {:added "4.1"}
  [client raw-input]
  (var #{instance} client)
  (return (conn-sql/query instance raw-input)))

(defn.xt client-sqlite
  "creates the thin sqlite client record with stored context"
  {:added "4.1"}
  [schema lookup settings]
  (return
   (xt/x:obj-assign
    (impl-common/client-base "db.client.sqlite"
                             schema
                             lookup
                             (sql-util/sqlite-opts lookup))
    {"settings" (or settings {"filename" ":memory:"})})))

(defn.xt client-sqlite-init
  [client driver]
  (var #{schema
         lookup
         settings
         opts} client)
  (return
   (-> (conn-sql/connect driver settings)
       (promise/x:promise-then
        (fn [instance]
          (xt/x:set-key client "instance" instance)
          (conn-sql/query instance
                          (xt/x:str-join
                           "\n\n"
                           (manage/table-create-all schema lookup opts)))
          (return client))))))
