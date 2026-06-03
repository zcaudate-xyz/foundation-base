(ns xt.db.system.client-memory
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.system.memory-store :as store]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(defn.xt client?
  "checks if a value is a memory client"
  {:added "4.1"}
  [obj]
  (return (and (xt/x:is-object? obj)
               (== "db.client.memory"
                   (xt/x:get-key obj "::")))))

(defn.xt client
  "creates a tagged in-memory db client"
  {:added "4.1"}
  [m]
  (when (-/client? m)
    (return m))
  (var out (xt/x:obj-clone (or m {})))
  (xt/x:set-key out "::" "db.client.memory")
  (when (not (xt/x:is-object? (xt/x:get-key out "rows")))
    (xt/x:set-key out "rows" {}))
  (return out))

(defn.xt process-event-sync
  "processes nested data into the memory client"
  {:added "4.1"}
  [client tag data schema lookup opts]
  (:= client (-/client client))
  (cond (== tag "input")
        (return (store/set-input schema data))

        :else
        (return (store/set-sync client schema data opts))))

(defn.xt process-event-remove
  "removes nested data from the memory client"
  {:added "4.1"}
  [client tag data schema lookup opts]
  (:= client (-/client client))
  (cond (== tag "input")
        (return (store/remove-input schema lookup data))

        :else
        (return (store/remove-sync client schema lookup data opts))))

(defn.xt pull-sync
  "fetches tree ir data from the memory client"
  {:added "4.1"}
  [client schema tree opts]
  (:= client (-/client client))
  (return (store/fetch-sync client schema tree opts)))

(defn.xt pull
  "fetches tree ir data with async semantics"
  {:added "4.1"}
  [client schema tree opts]
  (return
   (promise/x:promise-run
    (-/pull-sync client schema tree opts))))

(defn.xt record-add-sync
  "adds records directly to a single table in the memory client"
  {:added "4.1"}
  [client schema table-name records opts]
  (:= client (-/client client))
  (return (store/set-sync client schema {table-name records} opts)))

(defn.xt record-add
  "adds records directly with async semantics"
  {:added "4.1"}
  [client schema table-name records opts]
  (return
   (promise/x:promise-run
    (-/record-add-sync client schema table-name records opts))))

(defn.xt record-delete-sync
  "deletes ids directly from a single table in the memory client"
  {:added "4.1"}
  [client schema table-name ids opts]
  (:= client (-/client client))
  (return (store/delete-sync client schema table-name ids opts)))

(defn.xt record-delete
  "deletes ids directly with async semantics"
  {:added "4.1"}
  [client schema table-name ids opts]
  (return
   (promise/x:promise-run
    (-/record-delete-sync client schema table-name ids opts))))

(defn.xt clear
  "clears the memory client"
  {:added "4.1"}
  [client]
  (:= client (-/client client))
  (return (store/clear client)))
