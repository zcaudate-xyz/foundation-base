(ns xt.db.impl-sql
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.base-flatten :as f]
             [xt.db.base-schema :as base-schema]
             [xt.db.base-scope :as scope]
             [xt.sys.conn-dbsql :as conn-dbsql]
             [xt.db.sql-graph :as sql-graph]
             [xt.db.sql-table :as sql-table]
             [xt.db.sql-raw :as raw]
             [xt.lang.common-spec :as xt]
             [xt.lang.common-data :as xtd]]})

(defn.xt sql-gen-delete
  "generates the delete statements"
  {:added "4.0"}
  [table-name ids opts]
  (return
   (xt/x:arr-map ids (fn:> [id] (raw/raw-delete table-name {:id id} opts)))))

(defn.xt sql-process-event-sync
  "processes event sync data from database"
  {:added "4.0"}
  [instance tag data schema lookup opts]
  (var flat (f/flatten-bulk schema data))
  (var statements (sql-table/table-emit-flat
                   sql-table/table-emit-upsert
                   schema lookup flat opts))
  (cond (== tag "input")
        (return (xt/x:str-join "\n\n" statements))

        :else
        (do (conn-dbsql/query-sync instance 
                                   (xt/x:str-join "\n\n" statements))
            (return (xt/x:obj-keys flat)))))

(defn.xt sql-process-event-remove
  "removes data from database"
  {:added "4.0"}
  [instance tag data schema lookup opts]
  (var flat (f/flatten-bulk schema data))
  (var ordered (xtd/arr-keep (base-schema/table-order lookup)
                           (fn [col]
                             (return (:? (xt/x:has-key? flat col) [col (xt/x:obj-keys (xt/x:get-key flat col))] nil)))))
  
  (var emit-fn
       (fn [e]
         (var [table-name ids] e)
         (return (-/sql-gen-delete table-name ids opts))))
  (var statements (xtd/arr-mapcat ordered emit-fn))
  (cond (== tag "input")
        (return (xt/x:str-join "\n\n" statements))

        :else
        (do (conn-dbsql/query-sync instance 
                                   (xt/x:str-join "\n\n" statements))
            (return (xt/x:obj-keys flat)))))


(defn.xt sql-pull-sync
  "runs a pull statement"
  {:added "4.0"}
  [instance schema tree opts]
  (var output (conn-dbsql/query-sync
               instance
               (sql-graph/select schema tree opts)))
  (return (xt/x:json-decode (:? (xt/x:is-string? output)
                                output
                                "null"))))

(defn.xt sql-delete-sync
  "deletes sync data from sql db"
  {:added "4.0"}
  [instance schema table-name ids opts]
  (return (conn-dbsql/query-sync
           instance
           (xt/x:str-join "\n\n" (-/sql-gen-delete table-name ids opts)))))

(defn.xt sql-clear
  "clears the sql db"
  {:added "4.0"}
  [instance]
  (return true))
