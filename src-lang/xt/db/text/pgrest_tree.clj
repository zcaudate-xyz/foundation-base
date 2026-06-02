(ns xt.db.text.pgrest-tree
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.text.pgrest-graph :as pgrest-graph]
             [xt.db.text.base-tree :as base-tree]]})

(defn.xt pgrest-query-select
  "provides a view select query"
  {:added "4.0"}
  [schema entry args opts]
  (var qtree (base-tree/plan-select schema entry args opts))
  (return (pgrest-graph/select-return schema qtree 0 opts)))

(defn.xt pgrest-query-count
  "provides the count statement"
  {:added "4.0"}
  [schema entry args opts]
  (var qtree (base-tree/plan-count schema entry args opts))
  (return (pgrest-graph/select-return schema qtree 0 opts)))

(defn.xt pgrest-query-return
  "provides a view return query"
  {:added "4.0"}
  [schema entry id args opts]
  (var qtree (base-tree/plan-return schema entry id args opts))
  (return (pgrest-graph/select-return schema qtree 0 opts)))

(defn.xt pgrest-query-return-bulk
  "creates a bulk return statement"
  {:added "4.0"}
  [schema entry ids args opts]
  (var qtree (base-tree/plan-return-bulk schema entry ids args opts))
  (return (pgrest-graph/select-return schema qtree 0 opts)))

(defn.xt pgrest-query-combined
  "provides a view combine query"
  {:added "4.0"}
  [schema sel-entry sel-args ret-entry ret-args ret-omit opts]
  (var qtree (base-tree/plan-combined schema sel-entry sel-args ret-entry ret-args ret-omit opts))
  (return (pgrest-graph/select-return schema qtree 0 opts)))
