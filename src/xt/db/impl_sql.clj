(ns xt.db.impl-sql
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :xtalk
  {:require [[xt.db.base-flatten :as f]
             [xt.db.base-schema :as base-schema]
             [xt.db.base-scope :as scope]
             [xt.sys.conn-dbsql :as conn-dbsql]
             [xt.db.sql-graph :as sql-graph]
             [xt.db.sql-table :as sql-table]
             [xt.db.sql-raw :as raw]
             [xt.lang.base-lib :as k]]})

(defn.xt sql-gen-delete
  "generates the delete statements"
  {:added "4.0"}
  [table-name ids opts]
  (return
   (k/arr-map ids (fn:> [id] (raw/raw-delete table-name {:id id} opts)))))

(defn.xt sql-process-event-sync
  "processes event sync data from database"
  {:added "4.0"}
  [instance tag data schema lookup opts]
  (var flat (f/flatten-bulk schema data))
  (var statements (sql-table/table-emit-flat
                   sql-table/table-emit-upsert
                   schema lookup flat opts))
  (cond (== tag "input")
        (return (k/join "\n\n" statements))

        :else
        (do (conn-dbsql/query-sync instance 
                                   (k/join "\n\n" statements))
            (return (k/obj-keys flat)))))

(defn.xt sql-process-event-remove
  "removes data from database"
  {:added "4.0"}
  [instance tag data schema lookup opts]
  (var flat (f/flatten-bulk schema data))
  (var ordered (k/arr-keep (base-schema/table-order lookup)
                           (fn [col]
                             (return (:? (k/has-key? flat col) [col (k/obj-keys (k/get-key flat col))] nil)))))
  
  (var emit-fn
       (fn [e]
         (var [table-name ids] e)
         (return (-/sql-gen-delete table-name ids opts))))
  (var statements (k/arr-mapcat ordered emit-fn))
  (cond (== tag "input")
        (return (k/join "\n\n" statements))

        :else
        (do (conn-dbsql/query-sync instance 
                                   (k/join "\n\n" statements))
            (return (k/obj-keys flat)))))


(defn.xt sql-pull-sync
  "runs a pull statement"
  {:added "4.0"}
  [instance schema tree opts]
  (return (k/json-decode (or (conn-dbsql/query-sync
                            instance
                            (sql-graph/select schema tree opts))
                           "null"))))

(defn.xt sql-delete-sync
  "deletes sync data from sql db"
  {:added "4.0"}
  [instance schema table-name ids opts]
  (return (conn-dbsql/query-sync
           instance
           (k/join "\n\n" (-/sql-gen-delete table-name ids opts)))))

(defn.xt sql-clear
  "clears the sql db"
  {:added "4.0"}
  [instance]
  (return true))

