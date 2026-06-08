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
  "runs a tree ir pull against a sqlite impl"
  {:added "4.1"}
  [impl tree]
  (var #{client
         schema
         opts} impl)
  (var output
       (conn-sql/query client
                       (sql-graph/select schema tree opts)))
  (when (xt/x:is-string? output)
    (xt/x:err "SQL pull expected decoded structured data"))
  (return output))

(defn.xt pull-async
  "runs a tree ir pull with async sqlite semantics"
  {:added "4.1"}
  [impl tree]
  (var #{client
         schema
         opts} impl)
  (return
   (conn-sql/query-async client (sql-graph/select schema tree opts))))

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

(defn.xt record-add-async
  "adds rows directly with async sqlite semantics"
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
    (return (promise/x:promise-run nil)))
  (return
   (conn-sql/query-async client input)))

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

(defn.xt record-delete-async
  "deletes ids directly with async sqlite semantics"
  {:added "4.1"}
  [impl table-name ids]
  (var #{client
         opts} impl)
  (var statements
       (xt/x:arr-map ids
                     (fn [id]
                       (return (raw/raw-delete table-name {"id" id} opts)))))
  (return
   (conn-sql/query-async client
                         (xt/x:str-join "\n\n" statements))))

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


;;
;; PROCESS
;;

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

(defn.xt exec-sync
  "executes raw sql synchronously through the sqlite impl"
  {:added "4.1"}
  [impl raw-input]
  (var #{client} impl)
  (return (conn-sql/query client raw-input)))

;;
;; 

(defn.xt impl-sqlite
  "creates the thin sqlite impl record with stored context"
  {:added "4.1"}
  [schema lookup settings]
  (return
   (xt/x:obj-assign
    (conn-sql/create-base)
    (impl-common/client-base "db.client.sqlite"
                             schema
                             lookup
                             (sql-util/sqlite-opts lookup))
    {"settings" (or settings {"filename" ":memory:"})})))

(defn.xt impl-sqlite-init
  [impl driver]
  (var #{schema
         lookup
         settings
         opts} impl)
  (return
   (-> (conn-sql/connect driver settings)
       (promise/x:promise-then
        (fn [client]
          (xt/x:set-key impl "client" client)
          (conn-sql/query client (xt/x:str-join
                                  "\n\n"
                                  (manage/table-create-all schema lookup opts)))
          (return impl))))))
