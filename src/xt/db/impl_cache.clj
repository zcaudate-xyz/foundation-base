(ns xt.db.impl-cache
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :xtalk
  {:require [[xt.db.base-flatten :as f]
             [xt.db.base-schema :as base-schema]
             [xt.db.base-scope :as scope]
             [xt.db.cache-pull :as cache-pull]
             [xt.db.cache-util :as cache-util]
             [xt.lang.base-lib :as k]
             [xt.lang.util-throttle :as th]]})

(defn.xt cache-process-event-sync
  "processes event sync data from database"
  {:added "4.0"}
  [cache tag data schema lookup opts]
  (var #{rows} cache)
  (var flat (f/flatten-bulk schema data))
  (cond (== tag "input")
        (return flat)

        :else
        (do (cache-util/merge-bulk rows flat)
            (cache-util/add-bulk-links rows schema flat)
            (return (k/obj-keys flat)))))

(defn.xt cache-process-event-remove
  "removes data from database"
  {:added "4.0"}
  [cache tag data schema lookup opts]
  (var #{rows} cache)
  (var flat (f/flatten-bulk schema data))
  (var ordered (k/arr-keep (base-schema/table-order lookup)
                           (fn:> [col]
                             (:? (k/has-key? flat col)
                                 [col (k/obj-keys (k/get-key flat col))]
                                 nil))))
  (cond (== tag "input")
        (return ordered)

        :else
        (do (k/for:array [e ordered]
              (var [table-name ids] e)
              (cache-util/remove-bulk rows schema table-name ids))
            (return (k/obj-keys flat)))))

(defn.xt cache-pull-sync
  "runs a pull statement"
  {:added "4.0"}
  [cache schema tree opts]
  (var input (scope/get-link-standard tree))
  (var [table-name linked] input)
  (var return-params (k/last linked))
  (var where-params  (k/arr-filter linked (fn:> [x]
                                            (and (k/obj? x)
                                                 (k/not-empty? x)))))
  (var #{rows} cache)
  (var output (cache-pull/pull
               rows schema table-name
               {:where where-params
                :returning return-params}))
  (return output))

(defn.xt cache-delete-sync
  "deletes sync data from cache db"
  {:added "4.0"}
  [cache schema table-name ids opts]
  (var #{rows} cache)
  (return (cache-util/remove-bulk rows schema table-name ids)))

(defn.xt cache-clear
  "clears the cache"
  {:added "4.0"}
  [cache]
  (k/set-key cache "rows" {})
  (return true))

