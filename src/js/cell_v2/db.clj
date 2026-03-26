(ns js.cell-v2.db
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.core :as j]
             [xt.lang.base-lib :as k]
             [xt.db :as xdb]]})

(defn.js make-registry
  "creates a db registry"
  {:added "4.0"}
  []
  (return {"::" "cell-v2.db-registry"
           :entries {}}))

(defn.js register-db
  "registers an xt.db-backed database entry"
  {:added "4.0"}
  [registry key entry]
  (var curr (j/assign {:key key}
                      (or entry {})))
  (:= (. registry ["entries"] [key]) curr)
  (return curr))

(defn.js create-db
  "creates and registers an xt.db-backed database"
  {:added "4.0"}
  [registry key m schema lookup opts]
  (var db (xdb/db-create m schema lookup opts))
  (return (-/register-db registry
                         key
                         {:xdb db
                          :schema schema
                          :lookup lookup
                          :opts opts})))

(defn.js get-db
  "gets a db entry"
  {:added "4.0"}
  [registry key]
  (return (. registry ["entries"] [key])))

(defn.js list-dbs
  "lists db keys"
  {:added "4.0"}
  [registry]
  (return (k/obj-keys (. registry ["entries"]))))

(defn.js ensure-db
  "ensures a db entry exists"
  {:added "4.0"}
  [registry key]
  (var entry (-/get-db registry key))
  (when (k/nil? entry)
    (k/err (k/cat "ERR - DB not found - " key)))
  (return entry))

(defn.js db-sync
  "syncs rows into xt.db"
  {:added "4.0"}
  [registry key data]
  (var entry (-/ensure-db registry key))
  (return (xdb/sync-event (k/get-key entry "xdb")
                          ["add" data])))

(defn.js db-remove
  "removes rows from xt.db"
  {:added "4.0"}
  [registry key data]
  (var entry (-/ensure-db registry key))
  (return (xdb/sync-event (k/get-key entry "xdb")
                          ["remove" data])))

(defn.js db-query
  "queries xt.db with a pull tree"
  {:added "4.0"}
  [registry key tree]
  (var entry (-/ensure-db registry key))
  (return (xdb/db-pull-sync (k/get-key entry "xdb")
                            (k/get-key entry "schema")
                            tree)))

(defn.js db-delete
  "deletes rows from xt.db"
  {:added "4.0"}
  [registry key table-name ids]
  (var entry (-/ensure-db registry key))
  (return (xdb/db-delete-sync (k/get-key entry "xdb")
                              (k/get-key entry "schema")
                              table-name
                              ids)))

(defn.js db-clear
  "clears xt.db contents"
  {:added "4.0"}
  [registry key]
  (var entry (-/ensure-db registry key))
  (return (xdb/db-clear (k/get-key entry "xdb"))))

(defn.js add-trigger
  "adds an xt.db trigger"
  {:added "4.0"}
  [registry key id trigger]
  (var entry (-/ensure-db registry key))
  (return (xdb/add-trigger (k/get-key entry "xdb")
                           id
                           trigger)))

(defn.js remove-trigger
  "removes an xt.db trigger"
  {:added "4.0"}
  [registry key id]
  (var entry (-/ensure-db registry key))
  (return (xdb/remove-trigger (k/get-key entry "xdb")
                              id)))

(defn.js add-view-trigger
  "adds an xt.db view trigger"
  {:added "4.0"}
  [registry key id view view-fn]
  (var entry (-/ensure-db registry key))
  (return (xdb/add-view-trigger (k/get-key entry "xdb")
                                id
                                (k/get-key entry "schema")
                                view
                                view-fn)))
