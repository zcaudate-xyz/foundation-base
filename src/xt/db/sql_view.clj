(ns xt.db.sql-view
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.db.sql-graph :as sql-graph]
             [xt.db.sql-util :as sql-util]
             [xt.db.base-scope :as base-scope]]})

(defn.xt tree-control-array
  "creates a control array"
  {:added "4.0"}
  [control]
  (when (xt/x:is-empty? control)
    (return []))
  
  (var out [])
  (var #{order-by
         order-sort
         limit
         offset} control)
  (when (xt/is-array? order-by)
    (x:arr-push out (sql-util/ORDER-BY order-by)))
  (when order-sort
    (x:arr-push out (sql-util/ORDER-SORT order-sort)))
  (when (xt/x:is-number? limit)
    (x:arr-push out (sql-util/LIMIT limit)))
  (when (xt/x:is-number? offset)
    (x:arr-push out (sql-util/OFFSET offset)))
  (return out))

(defn.xt tree-base
  "creates a tree base"
  {:added "4.0"}
  [schema table-name sel-query clause returning opts]
  (var tarr (base-scope/merge-queries sel-query clause))
  (var tree (xt/x:arr-append [table-name] tarr))
  (when returning
    (x:arr-push tree returning))
  (return (sql-graph/select-tree schema tree opts)))

(defn.xt tree-count
  "provides a view count query"
  {:added "4.0"}
  [schema entry clause opts]
  (var #{view control} entry)
  (var #{table query} view)
  (return (-/tree-base schema table query clause [{"::" "sql/count"}
                                                  (xt/x:unpack (-/tree-control-array control))]
                       opts)))

(defn.xt tree-select
  "provides a view select query"
  {:added "4.0"}
  [schema entry clause opts]
  (var #{view control} entry)
  (var #{table query} view)
  (return (-/tree-base schema table query clause ["id"
                                                  (xt/x:unpack (-/tree-control-array control))] opts)))

(defn.xt tree-return
  "provides a view return query"
  {:added "4.0"}
  [schema entry sel-query clause opts]
  (var #{view} entry)
  (var #{table query} view)
  (return (-/tree-base schema table sel-query clause query opts)))

(defn.xt tree-combined
  "provides a view return query"
  {:added "4.0"}
  [schema sel-entry ret-entry ret-omit clause opts]
  (var #{control} sel-entry)
  (var sel-table   (xt/x:get-path sel-entry ["view" "table"]))
  (var ret-table   (xt/x:get-path ret-entry ["view" "table"]))
  (var sel-query   (or (xt/x:get-path sel-entry ["view" "query"]) {}))
  (var ret-query   (or (xt/x:get-path ret-entry ["view" "query"]) {}))
  (var ret-clause  (:? (xt/x:not-empty? ret-omit)
                       [{:id {:not-in [ret-omit]}}]
                       []))
  (var combined-clause (base-scope/merge-queries clause ret-clause))
  
  (return (-/tree-base schema sel-table sel-query combined-clause
                       (xt/x:arr-append (xt/x:arr-clone ret-query)
                                     (-/tree-control-array control))
                       opts)))

;;
;; QUERY
;;

(defn.xt query-fill-input
  "fills out the tree for a given input"
  {:added "4.0"}
  [tree args input-spec drop-first]
  (var arg-map {})
  (when drop-first
    (x:arr-pop-first input-spec))
  (when (== 0 (xt/x:len input-spec))
    (return tree))
  (xt/for:array [[i e] input-spec]
    (:= (. arg-map [(xt/x:cat "{{" (. e ["symbol"]) "}}")])
        (. args [i])))
  (var out (xt/x:walk tree
                   k/identity
                   (fn [x]
                     (return (:? (and (xt/x:is-string? x)
                                      (xt/x:has-key? arg-map x))
                                 (xt/x:get-key arg-map x)
                                 x)))))
  (return out))

(defn.xt query-select
  "provides a view select query"
  {:added "4.0"}
  [schema entry args opts as-tree]
  
  (var #{input} entry)
  (var itree  (-/tree-select schema entry {} opts))
  (var qtree  (-/query-fill-input itree args (xt/x:arr-clone input) false))
  (if as-tree
    (return qtree)
    (return (sql-graph/select-return schema qtree 0 opts))))

(defn.xt query-count
  "provides the count statement"
  {:added "4.0"}
  [schema entry args opts as-tree]
  (var #{input} entry)
  (var itree  (-/tree-count schema entry {} opts))
  (var qtree  (-/query-fill-input itree args (xt/x:arr-clone input) false))
  (if as-tree
    (return qtree)
    (return (sql-graph/select-return schema qtree 0 opts))))

(defn.xt query-return
  "provides a view return query"
  {:added "4.0"}
  [schema entry id args opts as-tree]
  (var #{input} entry)
  (var itree (-/tree-return schema entry {:id id} {} opts))
  (var qtree (-/query-fill-input itree args (xt/x:arr-clone input) true))
  (if as-tree
    (return qtree)
    (return (sql-graph/select-return schema qtree 0 opts))))

(defn.xt query-return-bulk
  "creates a bulk return statement"
  {:added "4.0"}
  [schema entry ids args opts as-tree]
  (var #{input} entry)
  (var itree  (-/tree-return schema
                             entry
                             {:id ["in" [ids]]}
                             {}
                             opts))
  (var qtree (-/query-fill-input itree args (xt/x:arr-clone input) true))
  (if as-tree
    (return qtree)
    (return (sql-graph/select-return schema qtree 0 opts))))

(defn.xt query-combined
  "provides a view combine query"
  {:added "4.0"}
  [schema sel-entry sel-args ret-entry ret-args ret-omit opts as-tree]
  (var sel-input  (xt/x:get-key sel-entry "input"))
  (var ret-input  (xt/x:get-key ret-entry "input"))
  (var itree   (-/tree-combined schema
                                sel-entry
                                ret-entry
                                ret-omit
                                []
                                opts))
  (var qtree (-/query-fill-input itree
                                 (-> (xt/x:arr-clone  ret-args)
                                     (xt/x:arr-append sel-args))
                                 (-> (xt/x:arr-clone  ret-input)
                                     (xt/x:arr-append sel-input))
                                 true))
  (if as-tree
    (return qtree)
    (return (sql-graph/select-return schema qtree 0 opts))))
