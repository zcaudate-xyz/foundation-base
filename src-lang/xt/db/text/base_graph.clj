(ns xt.db.text.base-graph
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.db.text.base-scope :as scope]]})

(defn.xt tree-params?
  "checks if params are already in canonical tree format"
  {:added "4.1"}
  [params]
  (return
   (and (xt/x:is-object? params)
        (not (xt/x:is-array? params))
        (or (xt/x:has-key? params "where")
            (xt/x:has-key? params "data")
            (xt/x:has-key? params "links")
            (xt/x:has-key? params "custom")))))

(defn.xt tree?
  "checks if a query is already a canonical tree"
  {:added "4.1"}
  [query]
  (return
   (and (xt/x:is-array? query)
        (>= (xt/x:len query) 2)
        (-/tree-params? (xtd/second query)))))

(defn.xt normalise-tree-params
  "fills missing tree buckets with defaults"
  {:added "4.1"}
  [params]
  (var out (xt/x:obj-clone (or params {})))
  (when (xt/x:nil? (xt/x:get-key out "where"))
    (xt/x:set-key out "where" []))
  (when (xt/x:nil? (xt/x:get-key out "data"))
    (xt/x:set-key out "data" []))
  (when (xt/x:nil? (xt/x:get-key out "links"))
    (xt/x:set-key out "links" []))
  (when (xt/x:nil? (xt/x:get-key out "custom"))
    (xt/x:set-key out "custom" []))
  (return out))

(defn.xt normalise-tree
  "normalises a canonical tree into the full explicit params shape"
  {:added "4.1"}
  [query]
  (if (not (-/tree? query))
    (return query)
    (return [(xt/x:first query)
             (-/normalise-tree-params (xtd/second query))])))

(defn.xt base-query-inputs
  "formats query input into table clause and return"
  {:added "4.1"}
  [query]
  (var table-name (xt/x:first query))
  (var cnt (xt/x:len query))
  (cond (== cnt 1)
        (return [table-name {} nil])

        (== cnt 3)
        (return [table-name (xt/x:second query) (xtd/nth query 2)])

        (xt/x:is-array? (xt/x:second query))
        (return [table-name {} (xt/x:second query)])

        :else
        (return [table-name (xt/x:second query) nil])))

(defn.xt select-tree
  "normalises a query into canonical tree ir"
  {:added "4.1"}
  [schema query opts]
  (:= opts (or opts {}))
  (if (-/tree? query)
    (return (-/normalise-tree query))
    (do (var input (scope/get-link-standard query))
        (var table-name := (xt/x:first input))
        (var linked := (xtd/second input))
        (var return-params (xt/x:last linked))
        (var where-params  (xt/x:arr-filter linked
                                           (fn [x]
                                             (return (and (xt/x:is-object? x)
                                                          (xtd/not-empty? x))))))
        (return (scope/get-tree schema table-name where-params return-params opts)))))
