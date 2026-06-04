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
             [xt.protocol.impl.connection-sql :as dbsql]]})

(defn.xt pull
  "runs a tree ir pull against a sqlite client"
  {:added "4.1"}
  [client tree]
  (var #{instance
         schema
         opts} client)
  (var output
       (dbsql/query instance
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
   (dbsql/ensure-promise
    (dbsql/query-async instance
                     (sql-graph/select schema tree opts)))))

(defn.xt record-add
  "adds rows directly through stored sqlite context"
  {:added "4.1"}
  [client table-name records]
  (var #{instance
         schema
         lookup
         opts} client)
  (return
   (dbsql/query instance
              (sql-table/prepare-add-input {table-name records}
                                           schema
                                           lookup
                                           opts))))

(defn.xt record-add-async
  "adds rows directly with async sqlite semantics"
  {:added "4.1"}
  [client table-name records]
  (var #{instance
         schema
         lookup
         opts} client)
  (return
   (dbsql/ensure-promise
    (dbsql/query-async instance
                     (sql-table/prepare-add-input {table-name records}
                                                  schema
                                                  lookup
                                                  opts)))))

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
   (dbsql/query instance
              (xt/x:str-join "\n\n" statements))))

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
   (dbsql/ensure-promise
    (dbsql/query-async instance
                     (xt/x:str-join "\n\n" statements)))))

(defn.xt process-add-event
  "processes nested data into sqlite upserts"
  {:added "4.1"}
  [client data]
  (var #{instance
         schema
         lookup
         opts} client)
  (var flat (f/flatten-bulk schema data))
  (dbsql/query instance
             (sql-table/prepare-add-input data schema lookup opts))
  (return (xt/x:obj-keys flat)))

(defn.xt process-remove-event
  "processes nested removals into sqlite delete statements"
  {:added "4.1"}
  [client data]
  (var #{instance
         schema
         lookup
         opts} client)
  (var ordered (f/flatten-bulk-ids schema lookup data))
  (dbsql/query instance
             (sql-table/prepare-remove-input data schema lookup opts))
  (return (xt/x:arr-map ordered xt/x:first)))

(defn.xt exec-sync
  "executes raw sql synchronously through the sqlite client"
  {:added "4.1"}
  [client raw-input]
  (var #{instance} client)
  (return (dbsql/query instance raw-input)))

(defn.xt sqlite-client
  "creates the thin sqlite client record with stored context"
  {:added "4.1"}
  [schema lookup opts settings]
  (return
   (xt/x:obj-assign
    (impl-common/client-base "db.client.sqlite"
                             schema
                             lookup
                             (xt/x:obj-assign
                              (sql-util/sqlite-opts lookup)
                              (or opts {})))
    {"settings" (or settings {"filename" ":memory:"})})))

(defn.xt sqlite-client-init
  [client driver-fn]
  (var #{schema
         lookup
         settings
         opts} client)
  (return
   (-> (dbsql/connect (driver-fn) settings)
       (promise/x:promise-then
        (fn [instance]
          (xt/x:set-key client "instance" instance)
          (dbsql/query
           instance
           (xt/x:str-join
            "\n\n"
            (manage/table-create-all schema lookup opts)))
          (return client))))))
