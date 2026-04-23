(ns xt.db.base-scope
  (:require [std.lang :as l]
            [std.lib.collection :as collection]
            [std.lib.foundation :as f]
            [std.lib.schema.base :as base]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-string :as xts]]})

(def +scope+ (collection/map-entries
              (fn [[k v]]
                [(f/strn k)
                 (zipmap (map f/strn v)
                         (repeat true))])
              base/+scope+))

(def.xt Scopes (@! +scope+))

(defn.xt merge-queries
  "merges query with clause"
  {:added "4.0"}
  [q-0 q-1]
  (var arr-0 (xt/x:arr-filter (xtd/arrayify q-0) xtd/not-empty?))
  (var arr-1 (xt/x:arr-filter (xtd/arrayify q-1) xtd/not-empty?))
  (when (xtd/arr-empty? arr-0)
    (return arr-1))
  (when (xtd/arr-empty? arr-1)
    (return arr-0))
  (var out [])
  (xt/for:array [e-0 arr-0]
    (xt/for:array [e-1 arr-1]
      (xt/x:arr-push out (xtd/obj-assign-nested
                          (xtd/tree-walk e-0 (fn [x] (return x)) (fn [x] (return x)))
                          (xtd/tree-walk e-1 (fn [x] (return x)) (fn [x] (return x)))))))
  (return out))

(defn.xt filter-scope
  "filter scopes from keys"
  {:added "4.0"}
  [ks]
  (var mscopes (xt/x:arr-filter ks (fn:> [s] (== "-" (xts/sym-ns s)))))
  (var ascopes (xt/x:arr-filter ks (fn:> [s] (== "*" (xts/sym-ns s)))))
  (return
   (xt/x:arr-foldl
    (xt/x:arr-map ascopes
               (fn:> [s] (xt/x:get-key -/Scopes s)))
    xt/x:obj-assign 
    (xtd/arr-lookup mscopes))))

(defn.xt filter-plain-key
  "converts _id tags to standard keys"
  {:added "4.0"}
  [s]
  (when (== nil (xts/sym-ns s))
    (return (:? (xt/x:str-ends-with s "_id")
                (xt/x:str-substring s 0 (- (xt/x:len s) 3))
                s))))

(defn.xt filter-plain
  "filter ids keys from scope keys"
  {:added "4.0"}
  [ks]
  (return
   (xtd/arr-lookup (xtd/arr-keep ks -/filter-plain-key))))

(defn.xt get-data-columns
  "get columns for given keys"
  {:added "4.0"}
  [schema table-key ks]
  (var str-ks (xt/x:arr-filter ks xt/x:is-string?))
  (var scopes (-/filter-scope str-ks))
  (var plains (-/filter-plain str-ks))
  (var cols   (xt/x:get-key schema table-key))
  (when (xt/x:nil? cols)
    (xt/x:err (xt/x:cat "ERR - Table not in Schema - " table-key)))
  (var scoped (xt/x:arr-filter (xt/x:obj-vals cols)
                             (fn:> [e]
                                   (or (and (xt/x:has-key? e "scope")
                                            (== true
                                                (xt/x:get-key scopes
                                                              (xt/x:cat "-/" (xt/x:get-key e "scope")))))
                                       (xt/x:has-key? plains
                                                      (xt/x:get-key e "ident"))))))
  (return
   (xtd/arr-sort scoped
                 (fn:> [e] (xt/x:get-key e "order"))
                 (fn:> [a b] (< a b)))))

(defn.xt get-link-standard
  "classifies the link"
  {:added "4.0"}
  [link]
  (var ltag (xt/x:first link))
  (var llen (xt/x:len link))
  (when (== 1 llen)
    (return [ltag [{} ["*/data"]]]))
  (var lmap (xt/x:arr-filter link xt/x:is-object?))
  (var larr (xt/x:arr-filter link xt/x:is-array?))
  (when (== 0 (xt/x:len larr))
    (:= larr [["*/data"]]))
  (when (== 0 (xt/x:len lmap))
    (:= lmap [{}]))
  (var lout [])
  (xt/x:arr-assign lout lmap)
  (xt/x:arr-assign lout larr)
  (return [ltag lout]))

;;
;;

(defn.xt get-query-tables
  "get columns for given query"
  {:added "4.0"}
[schema table-key query acc]
  (:= acc (:? (xt/x:is-object? acc) acc {}))
  (var table (xt/x:get-key schema table-key))
  (when table
    (:= (. acc [table-key]) true)
    (xt/for:object [[k v] query]
      (var e (xt/x:get-key table k))
      (when (==  "ref" (xt/x:get-key e "type"))
        (var link-key (xt/x:get-path e ["ref" "ns"]))
        (cond (xt/x:is-object? v)
              (-/get-query-tables schema link-key v acc)
              
              :else
              (:= (. acc [link-key]) true)))))
  (return acc))

(defn.xt get-link-columns
  "get columns for given keys"
  {:added "4.0"}
  [schema table-key ks]
  (var link-arr (xt/x:arr-filter ks xt/x:is-array?))
  (var linked (-> (xt/x:arr-map link-arr -/get-link-standard)
                  (xt/x:obj-from-pairs)))
  (var cols   (xt/x:get-key schema table-key))
  (return
   (xtd/arr-keepf (xt/x:obj-vals cols)
                (fn:> [col] (xt/x:has-key? linked (xt/x:get-key col "ident")))
                (fn:> [col] [col (xt/x:get-key linked (xt/x:get-key col "ident"))]))))

(defn.xt get-linked-tables
  "calculated linked tables given query"
  {:added "4.0"}
  [schema table-key returning]
  (var link-loop
        (fn [table-key returning acc]
          (var linked := (-/get-link-columns schema table-key
                                             (:? (xt/x:is-array? returning)
                                                 returning
                                                 [])))
          (var inner-loop
               (fn [arr]
                 (var attr := (xt/x:first arr))
                 (var link-query := (xtd/second arr))
                 (var link-where := (xt/x:first link-query))
                 (var link-returning := (xtd/second link-query))
                 (link-loop (xt/x:get-path attr ["ref" "ns"])
                            link-returning
                            acc)))

         (do (xt/x:set-key acc table-key true)
             (xt/x:arr-each linked inner-loop))
         (return acc)))
  (return (link-loop table-key returning {})))

(defn.xt as-where-input
  "when empty, returns an empty array"
  {:added "4.0"}
  [input]
  (cond (xtd/is-empty? input)
        (return [])

        (xt/x:is-array? input)
        (return input)

        :else
        (return [input])))

(defn.xt get-tree
  "calculated linked tree given query"
  {:added "4.0"}
  [schema table-name where returning opts]
  (var table-fn   (xt/x:get-key opts "table_fn" (fn [x] (return x))))
  (var column-fn  (xt/x:get-key opts "column_fn" (fn [x] (return x))))
  (:= where (-/as-where-input where))
  (:= returning (:? (xt/x:is-array? returning)
                    returning
                    ["*/data"]))
  (var where-pred  (fn:> [e] (and (xt/x:is-object? e) (xt/x:nil? (xt/x:get-key  e "::")))))
  (var custom-pred (fn:> [e] (and (xt/x:is-object? e) (xt/x:is-string? (xt/x:get-key  e "::")))))
  (var custom (xt/x:arr-filter returning custom-pred))
  (var data   (-/get-data-columns schema table-name returning))
  (var links  (-/get-link-columns schema table-name returning))
  (var get-child-tree
       (fn [link]
         (var attr := (xt/x:first link))
         (var link-query := (xtd/second link))
         (var link-where-query (xt/x:arr-filter link-query where-pred))
         (var link-returning  (xt/x:last link-query))
          (var link-where-returning  (xt/x:arr-filter link-returning where-pred))
          (var link-where  (-/merge-queries link-where-query link-where-returning))
          (var link-table  (xt/x:get-path attr ["ref" "ns"]))
          (var link-type   (xt/x:get-path attr ["ref" "type"]))
          (var link-extra {})
          (if (== "reverse" link-type)
            (xt/x:set-key link-extra
                          (xt/x:get-path attr ["ref" "rkey"])
                          ["eq" [(xt/x:cat (table-fn table-name)
                                           "."
                                           (column-fn "id"))]])
            (xt/x:set-key link-extra
                          "id"
                          ["eq" [(xt/x:cat (table-fn table-name)
                                           "."
                                           (column-fn (xt/x:cat (xt/x:get-path attr ["ref" "key"])
                                                                "_id")))]]))
          (return [(xt/x:get-key attr "ident")
                   link-type
                   (-/get-tree schema
                              link-table
                              (-/merge-queries link-where link-extra)
                              link-returning
                              opts)])))
  (return [table-name 
           {:where where
            :data  (xt/x:arr-map data (fn:> [e] (:? (== "ref" (xt/x:get-key e "type"))
                                                 (xt/x:cat (xt/x:get-key e "ident") "_id")
                                                 (xt/x:get-key e "ident"))))
            :links (xt/x:arr-map links get-child-tree)
            :custom custom}]))
