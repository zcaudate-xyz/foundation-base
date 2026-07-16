(ns xt.db.text.base-tree
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-tree :as xtt]
             [xt.db.text.base-check :as check]
             [xt.db.text.sql-graph :as sql-graph]
             [xt.db.text.sql-util :as sql-util]
             [xt.db.text.base-scope :as base-scope]]})

(defn.xt tree-control-array
  "creates a control array"
  {:added "4.0"}
  [control]
  (when (xtd/is-empty? control)
    (return []))
  
  (var out [])
  (var #{order-by
         order-sort
         limit
         offset} control)
  (when (xt/x:is-array? order-by)
    (xt/x:arr-push out (sql-util/ORDER-BY order-by)))
  (when (xt/x:not-nil? order-sort)
    (xt/x:arr-push out (sql-util/ORDER-SORT order-sort)))
  (when (xt/x:is-number? limit)
    (xt/x:arr-push out (sql-util/LIMIT limit)))
  (when (xt/x:is-number? offset)
    (xt/x:arr-push out (sql-util/OFFSET offset)))
  (return out))

(defn.xt tree-base
  "creates a tree base"
  {:added "4.0"}
  [schema table-name sel-query clause returning opts]
  (var tarr (base-scope/merge-queries sel-query clause))
  (var tree (xt/x:arr-assign [table-name] tarr))
  (when (xtd/not-empty? returning)
    (xt/x:arr-push tree returning))
  (return (sql-graph/select-tree schema tree opts)))

(defn.xt tree-count
  "provides a view count query"
  {:added "4.0"}
  [schema entry clause opts]
  (var #{view control} entry)
  (var #{table query} view)
  (return (-/tree-base schema table query clause (xt/x:arr-assign [{"::" "sql/count"}]
                                                                  (-/tree-control-array control))
                       opts)))

(defn.xt tree-select
  "provides a view select query"
  {:added "4.0"}
  [schema entry clause opts]
  (var #{view control} entry)
  (var #{table query} view)
  (return (-/tree-base schema table query clause (xt/x:arr-assign ["id"]
                                                                  (-/tree-control-array control))
                       opts)))

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
  (var sel-query   (xt/x:get-path sel-entry ["view" "query"]))
  (var ret-query   (xt/x:get-path ret-entry ["view" "query"]))
  (when (xt/x:nil? sel-query)
    (:= sel-query {}))
  (when (xt/x:nil? ret-query)
    (:= ret-query {}))
  (var ret-clause  (:? (xtd/not-empty? ret-omit)
                       [{:id {:not-in [ret-omit]}}]
                       []))
  (var combined-clause (base-scope/merge-queries clause ret-clause))
  
  (return (-/tree-base schema sel-table sel-query combined-clause
                       (xt/x:arr-assign (xt/x:arr-clone ret-query)
                                        (-/tree-control-array control))
                       opts)))

(defn.xt tree-fill-input
  "fills out the tree for a given input"
  {:added "4.0"}
  [tree args input-spec drop-first]
  (var arg-map {})
  (when drop-first
    (xt/x:arr-pop-first input-spec))
  (when (== 0 (xt/x:len input-spec))
    (return tree))
  (xt/for:array [[i e] input-spec]
    (xt/x:set-key arg-map
                  (xt/x:cat "{{" (xt/x:get-key e "symbol") "}}")
                  (xt/x:get-idx args i)))
  (var out (xtt/tree-walk tree
                          (fn [x] (return x))
                          (fn [x]
                            (when (and (xt/x:is-string? x)
                                       (xt/x:has-key? arg-map x))
                              (return (xt/x:get-key arg-map x)))
                            (return x))))
  (return out))


;;
;; Plans for submission
;;

(defn.xt plan-select
  "provides a view select query"
  {:added "4.0"}
  [schema entry args opts]
  (var #{input} entry)
  (var itree  (-/tree-select schema entry {} opts))
  (var qtree  (-/tree-fill-input itree args (xt/x:arr-clone input) false))
  (return qtree))

(defn.xt plan-count
  "provides the count statement"
  {:added "4.0"}
  [schema entry args opts]
  (var #{input} entry)
  (var itree  (-/tree-count schema entry {} opts))
  (var qtree  (-/tree-fill-input itree args (xt/x:arr-clone input) false))
  (return qtree))

(defn.xt plan-return
  "provides a view return query"
  {:added "4.0"}
  [schema entry id args opts]
  (var #{input} entry)
  (var itree (-/tree-return schema entry {:id id} {} opts))
  (var qtree (-/tree-fill-input itree args (xt/x:arr-clone input) true))
  (return qtree))

(defn.xt plan-return-bulk
  "creates a bulk return statement"
  {:added "4.0"}
  [schema entry ids args opts]
  (var #{input} entry)
  (var itree  (-/tree-return schema
                             entry
                             {:id ["in" [ids]]}
                             {}
                             opts))
  (var qtree (-/tree-fill-input itree args (xt/x:arr-clone input) true))
  (return qtree))

(defn.xt plan-combined
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
  (var qtree (-/tree-fill-input itree
                                 (-> (xt/x:arr-clone  ret-args)
                                     (xt/x:arr-assign sel-args))
                                 (-> (xt/x:arr-clone  ret-input)
                                     (xt/x:arr-assign sel-input))
                                 (:? (> (xt/x:len ret-input) 0) true false)))
  (return qtree))


;;
;; PLAN VIEW
;;

(defn.xt plan-view-check
  "checks query arguments against the entry input"
  {:added "4.0"}
  [entry args drop-first]
  (var targs (xt/x:get-key entry "input"))
  (when drop-first
    (:= targs [(xt/x:unpack targs)])
    (x:arr-pop-first targs))
  (var [l-ok l-err] (check/check-args-length args targs))
  (when (not l-ok)
    (return [l-ok l-err]))
  (var [t-ok t-err] (check/check-args-type args targs))
  (when (not t-ok)
    (return [t-ok t-err]))
  (return [true nil]))

(defn.xt plan-view
  "prepares a db query from entries against a system impl"
  {:added "4.0"}
  [schema query-spec]
  (var #{table
         select-entry
         select-args
         return-entry
         return-args
         return-omit
         return-count
         return-id
         return-bulk} query-spec)
  (cond (and (xt/x:not-nil? select-entry)
             (xt/x:not-nil? return-entry))
        (do (var [s-ok s-err] (-/plan-view-check select-entry select-args false))
            (when (not s-ok)
              (return [s-ok s-err]))
            (var [r-ok r-err] (-/plan-view-check return-entry return-args true))
            (when (not r-ok)
              (return [r-ok r-err]))
            (return [true (-/plan-combined
                           schema
                           select-entry
                           select-args
                           return-entry
                           return-args
                           return-omit
                           {}
                           false)]))
        
        (xt/x:not-nil? select-entry)
        (do (var [s-ok s-err] (-/plan-view-check select-entry select-args false))
            (when (not s-ok)
              (return [s-ok s-err]))
            (return [true (:? return-count
                              (-/plan-count
                               schema
                               select-entry
                               select-args
                               {})
                              (-/plan-select
                               schema
                               select-entry
                               select-args
                               {}))]))

        (xt/x:not-nil? return-id)
        (do (var rargs [return-id (xt/x:unpack return-args)])
            (var [r-ok r-err] (-/plan-view-check return-entry rargs false))
            (when (not r-ok)
              (return [r-ok r-err]))
            (return [true (-/plan-return
                           schema
                           return-entry
                           return-id
                           return-args
                           {})]))

        (xt/x:not-nil? return-bulk)
        (do (var [r-ok r-err] (-/plan-view-check return-entry return-args true))
            (when (not r-ok)
              (return [r-ok r-err]))
            (return [true (-/plan-return-bulk
                           schema
                           return-entry
                           return-bulk
                           return-args
                           {})]))

        :else
        (return [true nil])))
