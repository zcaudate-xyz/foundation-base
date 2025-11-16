(ns xt.db.sql-graph
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.db.sql-util :as ut]
             [xt.db.base-scope :as scope]]})

(defn.xt base-query-inputs
  "formats the query inputs"
  {:added "4.0"}
  [query]
  (var table-name (k/first query))
  (var cnt (k/len query))
  (cond (== cnt 1)
        (return [table-name {} nil])

        (== cnt 3)
        (return [table-name (k/second query) (k/nth query 2)])

        (k/arr? (k/second query))
        (return [table-name {} (k/second query)])
        
        :else
        (return [table-name (k/second query) nil])))

(defn.xt base-format-return
  "formats the query return"
  {:added "4.0"}
  [input nest-fn column-fn]
  (cond (k/obj? input)
        (return (k/cat (k/get-key input "expr")
                       (:? (k/has-key? input "as")
                           (k/cat " AS " (k/get-key input "as"))
                           "")))

        (k/arr? input)
        (return (nest-fn input))

        (k/is-string? input)
        (return (column-fn input))

        :else
        (k/err (k/cat "Invalid input - " (k/to-string input)))))

(defn.xt select-where-pair
  "formats the query return"
  {:added "4.0"}
  [schema table-name key clause indent opts where-fn]
  (var column-fn  (k/get-key opts "column_fn" k/identity))
  (var attr (k/get-path schema [table-name key]))
  (when (k/nil? attr)
    (k/err (k/cat "Attribute not found - " table-name " - " key)))
  (var arr-fn (fn [clause-fn clause-arr]
                (return (k/cat "("
                               (-> clause-arr
                                   (k/arr-map (fn [clause-obj]
                                                (return (k/cat "(" (clause-fn clause-obj) ")"))))
                                   (k/arr-join " OR "))
                               ")"))))
  (var forward-fn
       (fn [clause-obj]
         (return (k/cat key "_id" " IN (\n"
                        (k/pad-left "" indent " ")
                        (where-fn schema
                                  (k/get-path attr ["ref" "ns"])
                                  (column-fn "id")
                                  clause-obj
                                  indent
                                  opts)
                        "\n"
                        (k/pad-left "" (- indent 2) " ")
                        ")"))))
  (var reverse-fn
       (fn [clause-obj]
         (return (k/cat "id IN (\n"
                        (k/pad-left "" indent " ")
                        (where-fn schema
                                  (k/get-path attr ["ref" "ns"])
                                  (column-fn (k/cat (k/get-path attr ["ref" "rkey"]) "_id"))
                                  clause
                                  indent
                                  opts)
                        "\n"
                        (k/pad-left "" (- indent 2) " ")
                        ")"))))
  
  (cond (==  "ref" (k/get-key attr "type"))
        (cond (==  "forward" (k/get-path attr ["ref" "type"]))
                   (cond (k/obj? clause)
                         (return (forward-fn clause))
                         
                         (and (k/arr? clause)
                              (k/obj? (k/first clause)))
                         (return (arr-fn forward-fn clause))
                         
                         :else
                         (return (ut/encode-query-segment (k/cat key "_id")
                                                          clause
                                                          column-fn
                                                          opts)))
                   
                   (== "reverse" (k/get-path attr ["ref" "type"]))
                   (do (cond (k/is-string? clause)
                             (:= clause {:id clause})
                             
                             (and (k/arr? clause)
                                  (k/is-string? (k/first clause)))
                             (:= clause {:id clause}))
                       
                       (cond (k/obj? clause)
                             (return (reverse-fn clause))
                             
                             (and (k/arr? clause)
                                  (k/obj? (k/first clause)))
                             (return (arr-fn reverse-fn clause)))
                       #_(k/TRACE! [table-name k clause (k/get-path attr ["ref" "type"])])))
        
        :else
        (return (ut/encode-query-segment key clause column-fn opts))))

(defn.xt select-where
  "formats the query return"
  {:added "4.0"}
  [schema table-name return-str where-params indent opts]
  (var table-fn   (k/get-key opts "table_fn" k/identity))
  (var column-fn  (k/get-key opts "column_fn" k/identity))
  (when (not (k/arr? where-params))
    (:= where-params [where-params]))
  (var clause-fn
       (fn [clause]
         (var pair-fn
              (fn [pair]
                (return (-/select-where-pair
                         schema
                         table-name
                         (k/first pair)
                         (k/second pair)
                         (+ indent 2)
                         opts
                         -/select-where))))
         (var clause-arr (k/arr-map (k/obj-pairs clause)
                                    pair-fn))
         (return (k/join " AND " clause-arr))))
  (var where-arr  (-> (k/arr-map where-params clause-fn)
                      (k/arr-filter k/not-empty?)))
  (var where-str  (:? (== 0 (k/len where-arr)) ""
                      (== 1 (k/len where-arr)) (k/first where-arr)
                      :else (-> where-arr
                                (k/arr-map (fn:> [s] (k/cat "(" s ")")))
                                (k/arr-join " OR "))))
  (var out-arr    [(k/cat "SELECT " return-str)
                   (k/cat " FROM "  (table-fn table-name))])
  (if (< 0 (k/len where-str))
    (x:arr-push out-arr (k/cat "\n" (k/pad-left "" indent " ")
                               "WHERE " where-str)))
  (return (k/join "" out-arr)))

(defn.xt select-return-str
  "select return string loop"
  {:added "4.0"}
  [schema
   params
   return-fn
   indent
   opts]
  (var column-fn   (k/get-key opts "column_fn" k/identity))
  (var return-count-fn   (k/get-key opts "return_count_fn" (fn []
                                                             (return (k/cat "count(*)")))))
  (var return-format-fn  (k/get-key opts "return_format_fn" ut/default-return-format-fn))
  (var return-join-fn    (k/get-key opts "return_join_fn" (fn [arr] (return (k/join ", " arr)))))
  (var return-link-fn    (k/get-key opts "return_link_fn" (fn [s link-name]
                                                            (return (k/cat "(" s ") AS " link-name)))))
  (var nest-fn
       (fn [link]
         (var link-name (k/first link))
         (var link-tree (k/last link))
         (var link-ret  (return-fn schema link-tree 2 opts))
         (return (return-link-fn link-ret
                                 link-name))))
  (var format-fn
       (fn [v]
         (return (return-format-fn v nest-fn column-fn opts))))
  
  (var data-params   (k/get-key params "data"))
  (var link-params   (k/get-key params "links"))
  (var custom-params (k/get-key params "custom"))

  (when (and (== 1 (k/len custom-params))
             (== "sql/count"
                 (. (k/first custom-params)
                    ["::"])))
    (return (return-count-fn)))  
  
  (var return-data   (k/arr-map data-params format-fn))
  (var return-links  (k/arr-map link-params format-fn))
  (return  (return-join-fn
            (k/arr-mapcat [return-data
                           return-links]
                          k/identity))))

(defn.xt select-return
  "select return call"
  {:added "4.0"}
  [schema tree indent opts]
  (var column-fn   (k/get-key opts "column_fn" k/identity))
  (var wrapper-fn  (k/get-key opts "wrapper_fn" (fn [s indent] (return s))))
  (var format-fn   (fn:> [input] (ut/encode-sql input column-fn opts ut/encode-loop-fn)))
  (var [table-name params] tree)
  (var where-params  (k/get-key params "where"))
  (var custom-params (k/arr-filter (or (k/get-key params "custom")
                                       [])
                                   (fn:> [e] (== (. e ["::"])
                                                 "sql/keyword"))))
  (var return-str   (-/select-return-str schema
                                         params
                                         -/select-return
                                         indent
                                         opts))
  (var return-base  (-/select-where schema table-name return-str where-params 2 opts))
  (return (wrapper-fn (k/arr-join [return-base
                                   (k/unpack (k/arr-map custom-params format-fn))]
                                  " ")
                      (:? (> indent 0)
                          2
                          0))))

(defn.xt select-tree
  "gets the selection tree structure"
  {:added "4.0"}
  [schema query opts]
  (var input (scope/get-link-standard query))
  (var [table-name linked] input)
  (var return-params (k/last linked))
  (var where-params  (k/arr-filter linked (fn [x]
                                            (return (and (k/obj? x)
                                                         (k/not-empty? x))))))
  (var tree (scope/get-tree schema table-name where-params return-params opts))
  (return tree))

(defn.xt select
  "encodes a select state given schema and graph"
  {:added "4.0"}
  [schema query opts]
  (var tree (-/select-tree schema query opts))
  (return (-/select-return schema tree 0 opts)))

