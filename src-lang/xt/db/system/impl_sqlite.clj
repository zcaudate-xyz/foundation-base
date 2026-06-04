(ns xt.db.system.impl-sqlite
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.system.impl-common :as impl-common]
             [xt.db.text.base-flatten :as f]
             [xt.db.text.base-schema :as base-schema]
             [xt.db.text.sql-graph :as sql-graph]
             [xt.db.text.sql-table :as sql-table]
             [xt.db.text.sql-util :as sql-util]
             [xt.db.text.sql-raw :as raw]
             [xt.protocol.impl.connection-sql :as sql]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(defn.xt process-remove-event
  "processes nested removals into sqlite delete statements"
  {:added "4.1"}
  [client data]
  (var #{instance
         schema
         lookup
         opts
         } client)
  (var flat (f/flatten-bulk schema data))
  (sql/query instance
                  (sql-table/prepare-remove-input data schema lookup opts))
  (return (xt/x:obj-keys flat)))

(defn.xt process-add-event
  "processes nested data into sqlite upserts"
  {:added "4.1"}
  [client data]
  (var #{instance
         schema
         lookup
         opts} client)
  (var flat (f/flatten-bulk schema data))
  (sql/query instance
                  (-/prepare-sync-input data schema lookup opts))
  (return (xt/x:obj-keys flat)))



(defn.xt pull-sync
  "runs a tree ir pull against a sqlite client"
  {:added "4.1"}
  [client tree]
  (var #{instance
         schema
         opts} client)
  (var output (sql/query instance
                              (sql-graph/select schema tree opts)))
  (when (xt/x:is-string? output)
    (xt/x:err "SQL pull expected decoded structured data"))
  (return output))

(defn.xt record-add-sync
  "adds rows directly through sqlite query"
  {:added "4.1"}
  [client table-name records]
  (var #{schema
         opts} client)
  (var lookup {table-name {"position" 0}})
  (return
   (sql/query
    instance
    (-/prepare-sync-input {table-name records} schema lookup opts))))

(defn.xt record-add
  "adds rows with async sqlite semantics"
  {:added "4.1"}
  [client table-name records]
  (var #{schema
         opts} client)
  (var lookup {table-name {"position" 0}})
  (return
   (sql/ensure-promise
    (sql/query-async
     instance
     (-/prepare-sync-input {table-name records} schema lookup opts)))))

(defn.xt record-delete-sync
  "deletes rows through sqlite query"
  {:added "4.1"}
  [client table-name ids]
  (var #{instance} client)
  (return
   (sql/query
    instance
    (xt/x:str-join "\n\n" (-/sql-gen-delete table-name ids opts)))))

(defn.xt record-delete
  "deletes rows with async sqlite semantics"
  {:added "4.1"}
  [client table-name ids]
  (var #{instance} client)
  (return
   (sql/ensure-promise
    (sql/query-async
     instance
     (xt/x:str-join "\n\n" (-/sql-gen-delete table-name ids (xt/x:get-key client "opts")))))))

(defn.xt exec-sync
  "executes raw sql synchronously through the sqlite client"
  {:added "4.1"}
  [client raw-input]
  (var #{instance
         schema
         opts} client)
  (return (sql/query instance
                          raw-input)))

(defn.xt sqlite-client
  "creates the thin sqlite client record with stored schema context"
  {:added "4.1"}
  [instance schema lookup opts]
  (return
   (xt/x:obj-assign
    (impl-common/client-base "db.client.sqlite"
                             schema
                             lookup
                             (xt/x:obj-assign
                              (sql-util/sqlite-opts lookup)
                              opts))
    {"instance" instance})))


(comment

  
  (defn.xt prepare-event-input
    "prepares nested removals into sqlite delete statements"
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
    "processes nested data into sqlite upserts"
    {:added "4.1"}
    [client data]
    (var #{instance
           schema
           lookup
           opts} client)
    (var flat (f/flatten-bulk schema data))
    (sql/query instance
                    (-/prepare-sync-input data schema lookup opts))
    (return (xt/x:obj-keys flat)))

  )
