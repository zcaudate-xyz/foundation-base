(ns xt.db.runtime.cache
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.text.base-flatten :as f]
             [xt.db.text.base-schema :as base-schema]
             [xt.db.text.base-scope :as scope]
             [xt.db.runtime.cache-pull :as cache-pull]
             [xt.db.runtime.cache-util :as cache-util]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.event.util-throttle :as th]]})

(defn.xt cache-process-event-sync
  "processes event sync data from database"
  {:added "4.0"}
  [cache tag data schema lookup opts]
  (var #{rows} cache)
  (var flat (f/flatten-bulk schema data))
  (cond (== tag "input")
        (return flat)

        :else
        (do (cache-util/merge-bulk rows flat nil)
            (cache-util/add-bulk-links rows schema flat)
            (return (xt/x:obj-keys flat)))))

(defn.xt cache-process-event-remove
  "removes data from database"
  {:added "4.0"}
  [cache tag data schema lookup opts]
  (var #{rows} cache)
  (var flat (f/flatten-bulk schema data))
  (var ordered (xtd/arr-keep (base-schema/table-order lookup)
                             (fn:> [col]
                               (:? (xt/x:has-key? flat col)
                                   [col (xt/x:obj-keys (xt/x:get-key flat col))]
                                   nil))))
  (cond (== tag "input")
        (return ordered)

        :else
        (do (xt/for:array [e ordered]
              (var [table-name ids] e)
              (cache-util/remove-bulk rows schema table-name ids))
            (return (xt/x:obj-keys flat)))))

(defn.xt cache-pull-sync
  "runs a pull statement"
  {:added "4.0"}
  [cache schema tree opts]
  (var input (scope/get-link-standard tree))
  (var [table-name linked] input)
  (var return-params (xt/x:last linked))
  (var where-params  (xt/x:arr-filter linked (fn:> [x]
                                               (and (xt/x:is-object? x)
                                                    (xtd/not-empty? x)))))
  (var where where-params)
  (cond (== 0 (xt/x:len where-params))
        (:= where nil)

        (== 1 (xt/x:len where-params))
        (:= where (xt/x:first where-params)))
  (var #{rows} cache)
  (var output (cache-pull/pull
               rows schema table-name
               {:where where
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
  (xt/x:set-key cache "rows" {})
  (return true))
