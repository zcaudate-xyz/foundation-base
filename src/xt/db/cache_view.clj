(ns xt.db.cache-view
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.db.base-scope :as base-scope]]})

(defn.xt tree-base
  "creates a tree base"
  {:added "4.0"}
  [schema table-name sel-query returning custom-query]
  (var cquery (:? (xt/x:not-nil? custom-query) custom-query []))
  (var tarr (base-scope/merge-queries sel-query cquery))
  (var tree (xt/x:arr-assign [table-name] (:? (xt/x:not-nil? tarr) tarr [])))
  (when (xtd/not-empty? returning)
    (xt/x:arr-push tree returning))
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
  (var sel-query   (xt/x:get-path sel-entry ["view" "query"]))
  (var ret-query   (xt/x:get-path ret-entry ["view" "query"]))
  (when (xt/x:nil? sel-query)
    (:= sel-query {}))
  (when (xt/x:nil? ret-query)
    (:= ret-query {}))
  (return (-/tree-base schema
                       sel-table
                       sel-query
                       ret-query
                       (:? (xtd/not-empty? ret-omit)
                           [{:id {:not-in [ret-omit]}}]
                           []))))

(defn.xt query-fill-input
  "fills the input for args"
  {:added "4.0"}
  [tree args input-spec drop-first]
  (var arg-map {})
  (when drop-first
    (xt/x:arr-pop-first input-spec))
  (when (== 0 (xt/x:len input-spec))
    (return tree))
  (xt/for:array [[i e] input-spec]
    (:= (. arg-map [(xt/x:cat "{{" (. e ["symbol"]) "}}")])
        (. args [i])))
  (var out (xtd/tree-walk tree
                   (fn [x] (return x))
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
                                  (xt/x:arr-assign sel-args))
                              (-> (xt/x:arr-clone  ret-input)
                                  (xt/x:arr-assign sel-input))
                              true)))
