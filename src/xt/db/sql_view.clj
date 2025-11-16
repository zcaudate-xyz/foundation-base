(ns xt.db.sql-view
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.db.sql-graph :as sql-graph]
             [xt.db.sql-util :as sql-util]
             [xt.db.base-scope :as base-scope]]})

(defn.xt tree-control-array
  "creates a control array"
  {:added "4.0"}
  [control]
  (when (k/is-empty? control)
    (return []))
  
  (var out [])
  (var #{order-by
         order-sort
         limit
         offset} control)
  (when (k/arr? order-by)
    (x:arr-push out (sql-util/ORDER-BY order-by)))
  (when order-sort
    (x:arr-push out (sql-util/ORDER-SORT order-sort)))
  (when (k/is-number? limit)
    (x:arr-push out (sql-util/LIMIT limit)))
  (when (k/is-number? offset)
    (x:arr-push out (sql-util/OFFSET offset)))
  (return out))

(defn.xt tree-base
  "creates a tree base"
  {:added "4.0"}
  [schema table-name sel-query clause returning opts]
  (var tarr (base-scope/merge-queries sel-query clause))
  (var tree (k/arr-append [table-name] tarr))
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
                                                  (k/unpack (-/tree-control-array control))]
                       opts)))

(defn.xt tree-select
  "provides a view select query"
  {:added "4.0"}
  [schema entry clause opts]
  (var #{view control} entry)
  (var #{table query} view)
  (return (-/tree-base schema table query clause ["id"
                                                  (k/unpack (-/tree-control-array control))] opts)))

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
  (var sel-table   (k/get-path sel-entry ["view" "table"]))
  (var ret-table   (k/get-path ret-entry ["view" "table"]))
  (var sel-query   (or (k/get-path sel-entry ["view" "query"]) {}))
  (var ret-query   (or (k/get-path ret-entry ["view" "query"]) {}))
  
  (return (-/tree-base schema sel-table sel-query clause
                       (k/arr-append (k/arr-clone ret-query)
                                     (-/tree-control-array control))
                       opts)))

;;
;; QUERY
;;

(defn.xt query-fill-clause
  "fills the clause with access-id"
  {:added "4.0"}
  [entry access-id]
  (if (k/nil? access-id)
    (return {}))
  (var  clause (or (k/get-in entry ["view" "access" "query" "clause"])
                   {}))
  (return (k/walk clause
                  (fn [x]
                    (return (:? (== x "{{<%>}}")
                                access-id
                                x)))
                  k/identity)))

(defn.xt query-fill-input
  "fills out the tree for a given input"
  {:added "4.0"}
  [tree args input-spec drop-first]
  (var arg-map {})
  (when drop-first
    (x:arr-pop-first input-spec))
  (when (== 0 (k/len input-spec))
    (return tree))
  (k/for:array [[i e] input-spec]
    (:= (. arg-map [(k/cat "{{" (. e ["symbol"]) "}}")])
        (. args [i])))
  (var out (k/walk tree
                   k/identity
                   (fn [x]
                     (return (:? (and (k/is-string? x)
                                      (k/has-key? arg-map x))
                                 (k/get-key arg-map x)
                                 x)))))
  (return out))

(defn.xt query-access-check
  "constructs the access check"
  {:added "4.0"}
  [sel-access ret-access]
  (cond (k/nil? (k/get-key sel-access "symbol"))
        (return false)

        (== (k/get-key sel-access "symbol")
            (k/get-key ret-access "symbol"))
        (return true)
        
        (< 0 (k/len (k/obj-intersection
                     (k/get-key sel-access "roles")
                     (k/get-key ret-access "roles"))))
        (return true)
        
        :else
        (return false)))

(defn.xt query-select
  "provides a view select query"
  {:added "4.0"}
  [schema entry args opts as-tree]
  
  (var #{input} entry)
  (var #{access-id} opts)
  (var clause (-/query-fill-clause entry access-id))
  (var itree  (-/tree-select schema entry clause opts))
  (var qtree  (-/query-fill-input itree args (k/arr-clone input) false))
  (if as-tree
    (return qtree)
    (return (sql-graph/select-return schema qtree 0 opts))))

(defn.xt query-count
  "provides the count statement"
  {:added "4.0"}
  [schema entry args opts as-tree]
  (var #{input} entry)
  (var #{access-id} opts)
  (var clause (-/query-fill-clause entry access-id))
  (var itree  (-/tree-count schema entry clause opts))
  (var qtree  (-/query-fill-input itree args (k/arr-clone input) false))
  (if as-tree
    (return qtree)
    (return (sql-graph/select-return schema qtree 0 opts))))

(defn.xt query-return
  "provides a view return query"
  {:added "4.0"}
  [schema entry id args opts as-tree]
  (var #{input} entry)
  (var #{access-id} opts)
  (var clause (-/query-fill-clause entry access-id))
  (var itree (-/tree-return schema entry {:id id} clause opts))
  (var qtree (-/query-fill-input itree args (k/arr-clone input) true))
  (if as-tree
    (return qtree)
    (return (sql-graph/select-return schema qtree 0 opts))))

(defn.xt query-return-bulk
  "creates a bulk return statement"
  {:added "4.0"}
  [schema entry ids args opts as-tree]
  (var #{input} entry)
  (var #{access-id} opts)
  (var clause (-/query-fill-clause entry access-id))
  (var itree  (-/tree-return schema
                             entry
                             {:id ["in" [ids]]}
                             clause
                             opts))
  (var qtree (-/query-fill-input itree args (k/arr-clone input) true))
  (if as-tree
    (return qtree)
    (return (sql-graph/select-return schema qtree 0 opts))))

(defn.xt query-combined
  "provides a view combine query"
  {:added "4.0"}
  [schema sel-entry sel-args ret-entry ret-args ret-omit opts as-tree]
  (var #{access-id} opts)
  (var sel-input  (k/get-key sel-entry "input"))
  (var ret-input  (k/get-key ret-entry "input"))
  (var sel-access (k/get-path sel-entry ["view" "access"]))
  (var ret-access (k/get-path ret-entry ["view" "access"]))
  (var sel-clause [])
  (var ret-clause [])
  (when (not= nil (k/get-key sel-access "symbol"))
    (:= sel-clause (-/query-fill-clause sel-entry access-id)))
  
  (when (and (not= nil (k/get-key ret-access "symbol"))
             (not (-/query-access-check sel-access ret-access)))
    (:= ret-clause (-/query-fill-clause ret-entry access-id)))
  (var all-clause (base-scope/merge-queries sel-clause ret-clause))
  (var itree   (-/tree-combined schema
                                sel-entry
                                ret-entry
                                ret-omit
                                all-clause
                                opts))
  (var qtree (-/query-fill-input itree
                                 (-> (k/arr-clone  ret-args)
                                     (k/arr-append sel-args))
                                 (-> (k/arr-clone  ret-input)
                                     (k/arr-append sel-input))
                                 true))
  (if as-tree
    (return qtree)
    (return (sql-graph/select-return schema qtree 0 opts))))

