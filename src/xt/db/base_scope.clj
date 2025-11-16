(ns xt.db.base-scope
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.lib.schema.base :as base]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]]})

(def +scope+ (h/map-entries
              (fn [[k v]]
                [(h/strn k)
                 (zipmap (map h/strn v)
                         (repeat true))])
              base/+scope+))

(def.xt Scopes (@! +scope+))

(defn.xt merge-queries
  "merges query with clause"
  {:added "4.0"}
  [q-0 q-1]
  (var arr-0 (k/arr-filter (k/arrayify q-0) k/not-empty?))
  (var arr-1 (k/arr-filter (k/arrayify q-1) k/not-empty?))
  (when (k/is-empty? arr-0)
    (return arr-1))
  (when (k/is-empty? arr-1)
    (return arr-0))
  (var out [])
  (k/for:array [e-0 arr-0]
    (k/for:array [e-1 arr-1]
      (x:arr-push out (k/obj-assign-nested
                       (k/walk e-0 k/identity k/identity)
                       (k/walk e-1 k/identity k/identity)))))
  (return out))

(defn.xt filter-scope
  "filter scopes from keys"
  {:added "4.0"}
  [ks]
  (var mscopes (k/arr-filter ks (fn:> [s] (== "-" (k/sym-ns s)))))
  (var ascopes (k/arr-filter ks (fn:> [s] (== "*" (k/sym-ns s)))))
  (return
   (k/arr-foldl
    (k/arr-map ascopes
               (fn:> [s] (k/get-key -/Scopes s)))
    k/obj-assign 
    (k/arr-lookup mscopes))))

(defn.xt filter-plain-key
  "converts _id tags to standard keys"
  {:added "4.0"}
  [s]
  (when (== nil (k/sym-ns s))
    (return (:? (k/ends-with? s "_id")
                (k/substring s 0 (- (k/len s) 3))
                s))))

(defn.xt filter-plain
  "filter ids keys from scope keys"
  {:added "4.0"}
  [ks]
  (return
   (k/arr-lookup (k/arr-keep ks -/filter-plain-key))))

(defn.xt get-data-columns
  "get columns for given keys"
  {:added "4.0"}
  [schema table-key ks]
  (var str-ks (k/arr-filter ks k/is-string?))
  (var scopes (-/filter-scope str-ks))
  (var plains (-/filter-plain str-ks))
  (var cols   (k/get-key schema table-key))
  (when (k/nil? cols)
    (k/err (k/cat "ERR - Table not in Schema - " table-key)))
  (var scoped (k/arr-filter (k/obj-vals cols)
                            (fn:> [e]
                                  (or (and (k/has-key? e "scope")
                                           (k/get-key scopes (k/cat "-/" (k/get-key e "scope"))))
                                      (k/get-key plains (k/get-key e "ident"))))))
  (return
   (k/arr-sort scoped
               (fn:> [e] (k/get-key e "order"))
               (fn:> [a b] (< a b)))))

(defn.xt get-link-standard
  "classifies the link"
  {:added "4.0"}
  [link]
  (var ltag (k/first link))
  (var llen (k/len link))
  (when (== 1 llen)
    (return [ltag [{} ["*/data"]]]))
  (var lmap (k/arr-filter link k/obj?))
  (var larr (k/arr-filter link k/arr?))
  (when (== 0 (k/len larr))
    (:= larr [["*/data"]]))
  (when (== 0 (k/len lmap))
    (:= lmap [{}]))
  (var lout [])
  (k/arr-append lout lmap)
  (k/arr-append lout larr)
  (return [ltag lout]))

;;
;;

(defn.xt get-query-tables
  "get columns for given query"
  {:added "4.0"}
  [schema table-key query acc]
  (:= acc (or acc {}))
  (var table (k/get-key schema table-key))
  (when table
    (:= (. acc [table-key]) true)
    (k/for:object [[k v] query]
      (var e (k/get-key table k))
      (when (==  "ref" (k/get-key e "type"))
        (var link-key (k/get-path e ["ref" "ns"]))
        (cond (k/obj? v)
              (-/get-query-tables schema link-key v acc)
              
              :else
              (:= (. acc [link-key]) true)))))
  (return acc))

(defn.xt get-link-columns
  "get columns for given keys"
  {:added "4.0"}
  [schema table-key ks]
  (var link-arr (k/arr-filter ks k/arr?))
  (var linked (-> (k/arr-map link-arr -/get-link-standard)
                  (k/obj-from-pairs)))
  (var cols   (k/get-key schema table-key))
  (return
   (k/arr-keepf (k/obj-vals cols)
                (fn:> [col] (k/has-key? linked (k/get-key col "ident")))
                (fn:> [col] [col (k/get-key linked (k/get-key col "ident"))]))))

(defn.xt get-linked-tables
  "calculated linked tables given query"
  {:added "4.0"}
  [schema table-key returning]
  (var link-loop
       (fn [table-key returning acc]
         (var linked := (-/get-link-columns schema table-key (or returning [])))
         (var inner-loop
              (fn [arr]
                (var [attr link-query] := arr)
                (var [link-where link-returning] := link-query)
                (link-loop (k/get-path attr ["ref" "ns"])
                           link-returning
                           acc)))

         (do (k/set-key acc table-key true)
             (k/arr-each linked inner-loop))
         (return acc)))
  (return (link-loop table-key returning {})))

(defn.xt as-where-input
  "when empty, returns an empty array"
  {:added "4.0"}
  [input]
  (cond (k/is-empty? input)
        (return [])

        (k/arr? input)
        (return input)

        :else
        (return [input])))

(defn.xt get-tree
  "calculated linked tree given query"
  {:added "4.0"}
  [schema table-name where returning opts]
  (var table-fn   (k/get-key opts "table_fn" k/identity))
  (var column-fn  (k/get-key opts "column_fn" k/identity))
  (:= where (-/as-where-input where))
  (:= returning (or returning ["*/data"]))
  (var where-pred  (fn:> [e] (and (k/obj? e) (k/nil? (k/get-key  e "::")))))
  (var custom-pred (fn:> [e] (and (k/obj? e) (k/is-string? (k/get-key  e "::")))))
  (var custom (k/arr-filter returning custom-pred))
  (var data   (-/get-data-columns schema table-name returning))
  (var links  (-/get-link-columns schema table-name returning))
  (var get-child-tree
       (fn [link]
         (var [attr link-query] link)
         (var link-where-query (k/arr-filter link-query where-pred))
         (var link-returning  (k/last link-query))
         (var link-where-returning  (k/arr-filter link-returning where-pred))
         (var link-where  (-/merge-queries link-where-query link-where-returning))
         (var link-table  (k/get-path attr ["ref" "ns"]))
         (var link-type   (k/get-path attr ["ref" "type"]))
         (var link-extra (:? (== "reverse" link-type)
                             {(k/get-path attr ["ref" "rkey"])
                              ["eq" [(k/cat (table-fn table-name)
                                            "."
                                            (column-fn "id"))]]}
                             
                             {"id"
                              ["eq" [(k/cat (table-fn table-name)
                                            "."
                                            (column-fn (k/cat (k/get-path attr ["ref" "key"])
                                                              "_id")))]]}))
         (return [(k/get-key attr "ident")
                  link-type
                  (-/get-tree schema
                              link-table
                              (-/merge-queries link-where link-extra)
                              link-returning
                              opts)])))
  (return [table-name 
           {:where where
            :data  (k/arr-map data (fn:> [e] (:? (== "ref" (k/get-key e "type"))
                                                 (k/cat (k/get-key e "ident") "_id")
                                                 (k/get-key e "ident"))))
            :links (k/arr-map links get-child-tree)
            :custom custom}]))

