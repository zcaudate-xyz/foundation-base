(ns xt.db.cache-view
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.db.base-scope :as base-scope]]})

(defn.xt tree-base
  "creates a tree base"
  {:added "4.0"}
  [schema table-name sel-query returning custom-query]
  (var tarr (base-scope/merge-queries sel-query (or custom-query
                                                    [])))
  (var tree (k/arr-append [table-name] (or tarr [])))
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
  (var sel-table   (k/get-path sel-entry ["view" "table"]))
  (var ret-table   (k/get-path ret-entry ["view" "table"]))
  (var sel-query   (or (k/get-path sel-entry ["view" "query"]) {}))
  (var ret-query   (or (k/get-path ret-entry ["view" "query"]) {}))
  (return (-/tree-base schema
                       sel-table
                       sel-query
                       ret-query
                       (:? (k/not-empty? ret-omit)
                           [{:id {:not-in [ret-omit]}}]
                           []))))

(defn.xt query-fill-input
  "fills the input for args"
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

(defn.xt query-select
  "tree for the query-select"
  {:added "4.0"}
  [schema entry args]
  (var #{input} entry)
  (var itree  (-/tree-select schema entry))
  (return (-/query-fill-input itree args (k/arr-clone input) false)))

(defn.xt query-return
  "tree for the query-return"
  {:added "4.0"}
  [schema entry id args]
  (var #{input} entry)
  (var itree (-/tree-return schema entry {:id id}))
  (return (-/query-fill-input itree args (k/arr-clone input) true)))

(defn.xt query-return-bulk
  "tree for query-return"
  {:added "4.0"}
  [schema entry ids args]
  (var #{input} entry)
  (var itree  (-/tree-return schema
                             entry
                             {:id ["in" [ids]]}))
  (return (-/query-fill-input itree args (k/arr-clone input) true)))

(defn.xt query-combined
  "tree for query combined"
  {:added "4.0"}
  [schema sel-entry sel-args ret-entry ret-args ret-omit]
  (var sel-input  (k/get-key sel-entry "input"))
  (var ret-input  (k/get-key ret-entry "input"))
  (var itree   (-/tree-combined schema
                                sel-entry
                                ret-entry
                                ret-omit))
  (return (-/query-fill-input itree
                              (-> (k/arr-clone  ret-args)
                                  (k/arr-append sel-args))
                              (-> (k/arr-clone  ret-input)
                                  (k/arr-append sel-input))
                              true)))

