(ns xt.db.cache-view
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.db.base-scope :as base-scope]]})

(defn.xt tree-base
  "creates a tree base"
  {:added "4.0"}
  [schema table-name sel-query returning custom-query]
  (var tarr (base-scope/merge-queries sel-query (or custom-query
                                                    [])))
  (var tree (xt/x:arr-append [table-name] (or tarr [])))
  (when returning
    (x:arr-push tree returning))
  (return tree))

(defn.xt tree-select
  "creates a select tree"
  {:added "4.0"}
  [schema entry]
  (var #{view control} entry)
  (var #{table query} view)
  (return (-/tree-base schema table query ["id"] [])))

(defn.xt tree-return
  "creates a return tree"
  {:added "4.0"}
  [schema entry sel-query]
  (var #{view} entry)
  (var #{table query} view)
  (return (-/tree-base schema table sel-query query [])))

(defn.xt tree-combined
  "creates a combined tree"
  {:added "4.0"}
  [schema sel-entry ret-entry ret-omit]
  (var sel-table   (xt/x:get-path sel-entry ["view" "table"]))
  (var ret-table   (xt/x:get-path ret-entry ["view" "table"]))
  (var sel-query   (or (xt/x:get-path sel-entry ["view" "query"]) {}))
  (var ret-query   (or (xt/x:get-path ret-entry ["view" "query"]) {}))
  (return (-/tree-base schema
                       sel-table
                       sel-query
                       ret-query
                       (:? (xt/x:not-empty? ret-omit)
                           [{:id {:not-in [ret-omit]}}]
                           []))))

(defn.xt query-fill-input
  "fills the input for args"
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
  "tree for the query-select"
  {:added "4.0"}
  [schema entry args]
  (var #{input} entry)
  (var itree  (-/tree-select schema entry))
  (return (-/query-fill-input itree args (xt/x:arr-clone input) false)))

(defn.xt query-return
  "tree for the query-return"
  {:added "4.0"}
  [schema entry id args]
  (var #{input} entry)
  (var itree (-/tree-return schema entry {:id id}))
  (return (-/query-fill-input itree args (xt/x:arr-clone input) true)))

(defn.xt query-return-bulk
  "tree for query-return"
  {:added "4.0"}
  [schema entry ids args]
  (var #{input} entry)
  (var itree  (-/tree-return schema
                             entry
                             {:id ["in" [ids]]}))
  (return (-/query-fill-input itree args (xt/x:arr-clone input) true)))

(defn.xt query-combined
  "tree for query combined"
  {:added "4.0"}
  [schema sel-entry sel-args ret-entry ret-args ret-omit]
  (var sel-input  (xt/x:get-key sel-entry "input"))
  (var ret-input  (xt/x:get-key ret-entry "input"))
  (var itree   (-/tree-combined schema
                                sel-entry
                                ret-entry
                                ret-omit))
  (return (-/query-fill-input itree
                              (-> (xt/x:arr-clone  ret-args)
                                  (xt/x:arr-append sel-args))
                              (-> (xt/x:arr-clone  ret-input)
                                  (xt/x:arr-append sel-input))
                              true)))

