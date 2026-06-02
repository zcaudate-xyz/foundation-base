(ns xt.db.system.memory-store
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.text.base-flatten :as f]
             [xt.db.text.base-schema :as base-schema]
             [xt.db.runtime.cache-util :as cache-util]
             [xt.db.system.memory-graph :as memory-graph]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(defn.xt create-store
  "creates an empty in-memory store"
  {:added "4.1"}
  [opts]
  (return {"rows" {}}))

(defn.xt get-rows
  "returns the rows map for a store"
  {:added "4.1"}
  [store]
  (var rows (xt/x:get-key store "rows"))
  (when (not (xt/x:is-object? rows))
    (:= rows {})
    (xt/x:set-key store "rows" rows))
  (return rows))

(defn.xt set-input
  "prepares nested input data for in-memory storage"
  {:added "4.1"}
  [schema data]
  (return (f/flatten-bulk schema data)))

(defn.xt set-sync
  "merges nested data into store rows"
  {:added "4.1"}
  [store schema data opts]
  (var rows (-/get-rows store))
  (var flat (-/set-input schema data))
  (cache-util/merge-bulk rows flat nil)
  (cache-util/add-bulk-links rows schema flat)
  (return (xt/x:obj-keys flat)))

(defn.xt remove-input
  "prepares ordered delete ids from nested input data"
  {:added "4.1"}
  [schema lookup data]
  (var flat (f/flatten-bulk schema data))
  (return
   (xtd/arr-keep (base-schema/table-order lookup)
                 (fn [table-name]
                   (return (:? (xt/x:has-key? flat table-name)
                               [table-name (xt/x:obj-keys (xt/x:get-key flat table-name))]
                               nil))))))

(defn.xt remove-sync
  "removes nested input data from store rows"
  {:added "4.1"}
  [store schema lookup data opts]
  (var rows (-/get-rows store))
  (var ordered (-/remove-input schema lookup data))
  (xt/for:array [entry ordered]
    (var [table-name ids] entry)
    (cache-util/remove-bulk rows schema table-name ids))
  (return (xt/x:arr-map ordered xt/x:first)))

(defn.xt fetch-sync
  "fetches data from store rows using tree ir"
  {:added "4.1"}
  [store schema tree opts]
  (return (memory-graph/fetch (-/get-rows store)
                              schema
                              tree
                              opts)))

(defn.xt delete-sync
  "deletes rows directly from the store"
  {:added "4.1"}
  [store schema table-name ids opts]
  (return (cache-util/remove-bulk (-/get-rows store)
                                  schema
                                  table-name
                                  ids)))

(defn.xt clear
  "clears the in-memory store"
  {:added "4.1"}
  [store]
  (xt/x:set-key store "rows" {})
  (return true))
