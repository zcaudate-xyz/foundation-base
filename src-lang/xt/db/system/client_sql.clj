(ns xt.db.system.client-sql
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.text.base-flatten :as f]
             [xt.db.text.base-schema :as base-schema]
             [xt.db.text.sql-graph :as sql-graph]
             [xt.db.text.sql-table :as sql-table]
             [xt.db.text.sql-raw :as raw]
             [xt.protocol.impl.connection-sql :as sql]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]]})

(defn.xt client?
  "checks if a value is a sql client"
  {:added "4.1"}
  [obj]
  (return (and (xt/x:is-object? obj)
               (== "db.client.sql"
                   (xt/x:get-key obj "::")))))

(defn.xt client
  "creates a tagged sql client"
  {:added "4.1"}
  [m]
  (when (-/client? m)
    (return m))
  (var out (xt/x:obj-clone (or m {})))
  (xt/x:set-key out "::" "db.client.sql")
  (return out))

(defn.xt sql-gen-delete
  "generates delete statements"
  {:added "4.1"}
  [table-name ids opts]
  (return
   (xt/x:arr-map ids
                 (fn [id]
                   (return (raw/raw-delete table-name {"id" id} opts))))))

(defn.xt prepare-sync-input
  "prepares nested data into sql upsert statements"
  {:added "4.1"}
  [data schema lookup opts]
  (var flat (f/flatten-bulk schema data))
  (var statements (sql-table/table-emit-flat
                  sql-table/table-emit-upsert
                  schema
                  lookup
                  flat
                  opts))
  (return (xt/x:str-join "\n\n" statements)))

(defn.xt prepare-event-input
  "prepares nested removals into sql delete statements"
  {:added "4.1"}
  [data schema lookup opts]
  (var flat (f/flatten-bulk schema data))
  (var ordered (xtd/arr-keep (base-schema/table-order lookup)
                            (fn [table-name]
                              (return (:? (xt/x:has-key? flat table-name)
                                          [table-name (xt/x:obj-keys (xt/x:get-key flat table-name))]
                                          nil)))))
  (var statements (xtd/arr-mapcat ordered
                                 (fn [entry]
                                   (var [table-name ids] entry)
                                   (return (-/sql-gen-delete table-name ids opts)))))
  (return (xt/x:str-join "\n\n" statements)))

(defn.xt process-event-sync
  "processes nested data into sql upserts"
  {:added "4.1"}
  [client tag data schema lookup opts]
  (:= client (-/client client))
  (var instance (xt/x:get-key client "instance"))
  (var flat (f/flatten-bulk schema data))
  (sql/query-sync instance
                 (-/prepare-sync-input data schema lookup opts))
  (return (xt/x:obj-keys flat)))

(defn.xt process-event-remove
  "processes nested removals into delete statements"
  {:added "4.1"}
  [client tag data schema lookup opts]
  (:= client (-/client client))
  (var instance (xt/x:get-key client "instance"))
  (var flat (f/flatten-bulk schema data))
  (sql/query-sync instance
                 (-/prepare-event-input data schema lookup opts))
  (return (xt/x:obj-keys flat)))

(defn.xt exec-sync
  "executes raw sql through query-sync"
  {:added "4.1"}
  [client raw-input]
  (:= client (-/client client))
  (return (sql/query-sync (xt/x:get-key client "instance")
                          raw-input)))

(defn.xt pull-sync
  "runs a tree ir pull against a sql client"
  {:added "4.1"}
  [client schema tree opts]
  (:= client (-/client client))
  (var output (sql/query-sync
               (xt/x:get-key client "instance")
               (sql-graph/select schema tree opts)))
  (when (xt/x:is-string? output)
    (xt/x:err "SQL pull expected decoded structured data"))
  (return output))

(defn.xt pull
  "runs a tree ir pull with async sql semantics"
  {:added "4.1"}
  [client schema tree opts]
  (:= client (-/client client))
  (return
   (promise/x:promise-then
    (sql/ensure-promise
     (sql/query
      (xt/x:get-key client "instance")
      (sql-graph/select schema tree opts)))
    (fn [output]
      (when (xt/x:is-string? output)
        (xt/x:err "SQL pull expected decoded structured data"))
      (return output)))))

(defn.xt record-add-sync
  "adds rows directly through query-sync"
  {:added "4.1"}
  [client schema table-name records opts]
  (:= client (-/client client))
  (var lookup {table-name {"position" 0}})
  (return
   (sql/query-sync
    (xt/x:get-key client "instance")
    (-/prepare-sync-input {table-name records} schema lookup opts))))

(defn.xt record-add
  "adds rows with async sql semantics"
  {:added "4.1"}
  [client schema table-name records opts]
  (:= client (-/client client))
  (var lookup {table-name {"position" 0}})
  (return
   (sql/ensure-promise
    (sql/query
     (xt/x:get-key client "instance")
     (-/prepare-sync-input {table-name records} schema lookup opts)))))

(defn.xt record-delete-sync
  "deletes rows through query-sync"
  {:added "4.1"}
  [client schema table-name ids opts]
  (:= client (-/client client))
  (return
   (sql/query-sync
    (xt/x:get-key client "instance")
    (xt/x:str-join "\n\n" (-/sql-gen-delete table-name ids opts)))))

(defn.xt record-delete
  "deletes rows with async sql semantics"
  {:added "4.1"}
  [client schema table-name ids opts]
  (:= client (-/client client))
  (return
   (sql/ensure-promise
    (sql/query
     (xt/x:get-key client "instance")
     (xt/x:str-join "\n\n" (-/sql-gen-delete table-name ids opts))))))
