(ns xt.db.system.client-sqlite
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.system.base-sql :as sql-base]
             [xt.lang.spec-base :as xt]
             [xt.protocol.impl.graphdb :as graphdb]]})

(defn.xt client?
  "checks if a value is a sqlite graphdb client"
  {:added "4.1"}
  [obj]
  (return (and (xt/x:is-object? obj)
               (== "db.client.sqlite"
                   (xt/x:get-key obj "::")))))

(defn.xt client
  "creates a tagged sqlite graphdb client"
  {:added "4.1"}
  [m]
  (when (-/client? m)
    (return m))
  (return (sql-base/attach-graphdb-tag m "db.client.sqlite")))

(defn.xt process-event-sync
  "processes nested data into sqlite upserts"
  {:added "4.1"}
  [client tag data schema lookup opts]
  (return (sql-base/process-event-sync client tag data schema lookup opts)))

(defn.xt process-event-remove
  "processes nested removals into sqlite delete statements"
  {:added "4.1"}
  [client tag data schema lookup opts]
  (return (sql-base/process-event-remove client tag data schema lookup opts)))

(defn.xt record-delete-sync
  "deletes rows synchronously through the sqlite client"
  {:added "4.1"}
  [client schema table-name ids opts]
  (return (sql-base/record-delete-sync client schema table-name ids opts)))

(defn.xt exec-sync
  "executes raw sql synchronously through the sqlite client"
  {:added "4.1"}
  [client raw-input]
  (return (sql-base/exec-sync client raw-input)))

(def.xt DRIVER
  (graphdb/driver-create
   {"create"      (fn [m]
                    (return (-/client m)))
    "add"         (fn [instance tag data schema lookup opts]
                    (return (-/process-event-sync instance tag data schema lookup opts)))
    "remove"      (fn [instance tag data schema lookup opts]
                    (return (-/process-event-remove instance tag data schema lookup opts)))
    "delete_sync" (fn [instance schema table-name ids opts]
                    (return (-/record-delete-sync instance schema table-name ids opts)))
    "exec_sync"   (fn [instance raw-input]
                    (return (-/exec-sync instance raw-input)))}))
