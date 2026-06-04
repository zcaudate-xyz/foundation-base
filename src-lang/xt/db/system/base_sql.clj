(ns xt.db.system.base-sql
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.system.client-common :as client-common]
             [xt.db.text.base-flatten :as f]
             [xt.db.text.base-schema :as base-schema]
             [xt.db.text.sql-graph :as sql-graph]
             [xt.db.text.sql-table :as sql-table]
             [xt.db.text.sql-raw :as raw]
             [xt.protocol.impl.connection-sql :as sql]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]
             [xt.protocol.impl.graphdb :as graphdb]]})

(def.xt CLIENT_TAGS
  {"db.client.sql" true
   "db.client.postgres" true
   "db.client.sqlite" true})

(def.xt SQL_SETTING_KEYS
  ["host"
   "port"
   "database"
   "username"
   "password"
   "ssl"
   "sslmode"
   "filename"
   "filepath"
   "path"
   "dialect"])

(defn.xt sql-client?
  "checks if a value is any sql family client"
  {:added "4.1"}
  [obj]
  (return (and (xt/x:is-object? obj)
               (xt/x:has-key? -/CLIENT_TAGS
                              (xt/x:get-key obj "::")))))

(defn.xt client?
  "checks if a value is a sql client"
  {:added "4.1"}
  [obj]
  (return (-/sql-client? obj)))

(defn.xt normalize-client
  "normalises a raw value into a tagged sql-family client shape"
  {:added "4.1"}
  [m]
  (when (-/sql-client? m)
    (return m))
  (return (client-common/create-client m "db.client.sql" -/SQL_SETTING_KEYS)))

(defn.xt normalize-client-tag
  "normalises a raw value into a tagged sql-family client shape"
  {:added "4.1"}
  [m tag]
  (when (and (-/sql-client? m)
             (== tag (xt/x:get-key m "::")))
    (return m))
  (return (client-common/create-client m tag -/SQL_SETTING_KEYS)))

(defn.xt resolve-schema
  "resolves schema from explicit args or stored client context"
  {:added "4.1"}
  [client schema]
  (return (or schema
              (xt/x:get-key client "schema")
              {})))

(defn.xt resolve-lookup
  "resolves lookup from explicit args or stored client context"
  {:added "4.1"}
  [client lookup]
  (return (or lookup
              (xt/x:get-key client "lookup")
              {})))

(defn.xt resolve-opts
  "resolves opts from explicit args or stored client context"
  {:added "4.1"}
  [client opts]
  (return (or opts
              (xt/x:get-key client "opts")
              {})))

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
  (:= client (-/normalize-client client))
  (:= schema (-/resolve-schema client schema))
  (:= lookup (-/resolve-lookup client lookup))
  (:= opts (-/resolve-opts client opts))
  (var instance (xt/x:get-key client "instance"))
  (var flat (f/flatten-bulk schema data))
  (sql/query instance
                 (-/prepare-sync-input data schema lookup opts))
  (return (xt/x:obj-keys flat)))

(defn.xt process-event-remove
  "processes nested removals into delete statements"
  {:added "4.1"}
  [client tag data schema lookup opts]
  (:= client (-/normalize-client client))
  (:= schema (-/resolve-schema client schema))
  (:= lookup (-/resolve-lookup client lookup))
  (:= opts (-/resolve-opts client opts))
  (var instance (xt/x:get-key client "instance"))
  (var flat (f/flatten-bulk schema data))
  (sql/query instance
                 (-/prepare-event-input data schema lookup opts))
  (return (xt/x:obj-keys flat)))

(defn.xt exec-sync
  "executes raw sql through query"
  {:added "4.1"}
  [client raw-input]
  (:= client (-/normalize-client client))
  (return (sql/query (xt/x:get-key client "instance")
                          raw-input)))

(defn.xt pull-sync
  "runs a tree ir pull against a sql client"
  {:added "4.1"}
  [client schema tree opts]
  (:= client (-/normalize-client client))
  (:= schema (-/resolve-schema client schema))
  (:= opts (-/resolve-opts client opts))
  (var output (sql/query
               (xt/x:get-key client "instance")
               (sql-graph/select schema tree opts)))
  (when (xt/x:is-string? output)
    (xt/x:err "SQL pull expected decoded structured data"))
  (return output))

(defn.xt pull
  "runs a tree ir pull with async sql semantics"
  {:added "4.1"}
  [client schema tree opts]
  (:= client (-/normalize-client client))
  (:= schema (-/resolve-schema client schema))
  (:= opts (-/resolve-opts client opts))
  (return
   (promise/x:promise-then
    (sql/ensure-promise
     (sql/query-async
      (xt/x:get-key client "instance")
      (sql-graph/select schema tree opts)))
    (fn [output]
      (when (xt/x:is-string? output)
        (xt/x:err "SQL pull expected decoded structured data"))
      (return output)))))

(defn.xt record-add-sync
  "adds rows directly through query"
  {:added "4.1"}
  [client schema table-name records opts]
  (:= client (-/normalize-client client))
  (:= schema (-/resolve-schema client schema))
  (:= opts (-/resolve-opts client opts))
  (var lookup {table-name {"position" 0}})
  (return
   (sql/query
    (xt/x:get-key client "instance")
    (-/prepare-sync-input {table-name records} schema lookup opts))))

(defn.xt record-add
  "adds rows with async sql semantics"
  {:added "4.1"}
  [client schema table-name records opts]
  (:= client (-/normalize-client client))
  (:= schema (-/resolve-schema client schema))
  (:= opts (-/resolve-opts client opts))
  (var lookup {table-name {"position" 0}})
  (return
   (sql/ensure-promise
    (sql/query-async
     (xt/x:get-key client "instance")
     (-/prepare-sync-input {table-name records} schema lookup opts)))))

(defn.xt record-delete-sync
  "deletes rows through query"
  {:added "4.1"}
  [client schema table-name ids opts]
  (:= client (-/normalize-client client))
  (:= schema (-/resolve-schema client schema))
  (:= opts (-/resolve-opts client opts))
  (return
   (sql/query
    (xt/x:get-key client "instance")
    (xt/x:str-join "\n\n" (-/sql-gen-delete table-name ids opts)))))

(defn.xt record-delete
  "deletes rows with async sql semantics"
  {:added "4.1"}
  [client schema table-name ids opts]
  (:= client (-/normalize-client client))
  (:= schema (-/resolve-schema client schema))
  (:= opts (-/resolve-opts client opts))
  (return
   (sql/ensure-promise
    (sql/query-async
     (xt/x:get-key client "instance")
     (xt/x:str-join "\n\n" (-/sql-gen-delete table-name ids opts))))))

(defn.xt attach-graphdb
  "attaches graphdb methods to a sql-family client"
  {:added "4.1"}
  [client]
  (return
   (graphdb/db-create
   client
   {"pull"          (fn [client schema tree opts]
                    (return (-/pull client schema tree opts)))
    "pull_sync"     (fn [client schema tree opts]
                    (return (-/pull-sync client schema tree opts)))
    "record_add"    (fn [client schema table-name records opts]
                      (return (-/record-add client schema table-name records opts)))
    "record_delete" (fn [client schema table-name ids opts]
                      (return (-/record-delete client schema table-name ids opts)))})))

(defn.xt attach-graphdb-tag
  "tags and attaches graphdb methods to a sql-family client"
  {:added "4.1"}
  [client tag]
  (var out (-/normalize-client-tag client tag))
  (return (-/attach-graphdb out)))

(defn.xt client
  "creates a tagged sql client"
  {:added "4.1"}
  [m]
  (return (-/attach-graphdb (-/normalize-client m))))

(def.xt DRIVER
  (graphdb/driver-create
   {"create" (fn [m]
               (return (-/client m)))}))
