(ns xt.db.sql-graph
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-data :as xtd]
             [xt.db.sql-util :as ut]
             [xt.db.base-scope :as scope]]})

(defn.xt base-query-inputs
  "formats the query inputs"
  {:added "4.0"}
  [query]
  (var table-name (xt/x:first query))
  (var cnt (xt/x:len query))
  (cond (== cnt 1)
        (return [table-name {} nil])

        (== cnt 3)
        (return [table-name (xt/x:second query) (xtd/nth query 2)])

        (xt/x:is-array? (xt/x:second query))
        (return [table-name {} (xt/x:second query)])
        
        :else
        (return [table-name (xt/x:second query) nil])))

(defn.xt base-format-return
  "formats the query return"
  {:added "4.0"}
  [input nest-fn column-fn]
  (cond (xt/x:is-object? input)
        (return (xt/x:cat (xt/x:get-key input "expr")
                       (:? (xt/x:has-key? input "as")
                           (xt/x:cat " AS " (xt/x:get-key input "as"))
                           "")))

        (xt/x:is-array? input)
        (return (nest-fn input))

        (xt/x:is-string? input)
        (return (column-fn input))

        :else
        (xt/x:err (xt/x:cat "Invalid input - " (xt/x:to-string input)))))

(defn.xt select-where-pair
  "formats the query return"
  {:added "4.0"}
  [schema table-name key clause indent opts where-fn]
  (var column-fn  (xt/x:get-key opts "column_fn" (fn [x] (return x))))
  (var attr (xt/x:get-path schema [table-name key]))
  (when (xt/x:nil? attr)
    (xt/x:err (xt/x:cat "Attribute not found - " table-name " - " key)))
  (var arr-fn (fn [clause-fn clause-arr]
                (return (xt/x:cat "("
                                  (xt/x:str-join " OR "
                                                 (-> clause-arr
                                                     (xt/x:arr-map (fn [clause-obj]
                                                                     (return (xt/x:cat "(" (clause-fn clause-obj) ")"))))))
                               ")"))))
  (var forward-fn
       (fn [clause-obj]
         (return (xt/x:cat key "_id" " IN (\n"
                        (xt/x:str-pad-left "" indent " ")
                        (where-fn schema
                                  (xt/x:get-path attr ["ref" "ns"])
                                  (column-fn "id")
                                  clause-obj
                                  indent
                                  opts)
                        "\n"
                        (xt/x:str-pad-left "" (- indent 2) " ")
                        ")"))))
  (var reverse-fn
       (fn [clause-obj]
         (return (xt/x:cat "id IN (\n"
                        (xt/x:str-pad-left "" indent " ")
                        (where-fn schema
                                  (xt/x:get-path attr ["ref" "ns"])
                                  (column-fn (xt/x:cat (xt/x:get-path attr ["ref" "rkey"]) "_id"))
                                  clause-obj
                                  indent
                                  opts)
                        "\n"
                        (xt/x:str-pad-left "" (- indent 2) " ")
                        ")"))))
  
  (cond (==  "ref" (xt/x:get-key attr "type"))
        (cond (==  "forward" (xt/x:get-path attr ["ref" "type"]))
                   (cond (xt/x:is-object? clause)
                         (return (forward-fn clause))
                         
                         (and (xt/x:is-array? clause)
                              (xt/x:is-object? (xt/x:first clause)))
                         (return (arr-fn forward-fn clause))
                         
                         :else
                         (return (ut/encode-query-segment (xt/x:cat key "_id")
                                                          clause
                                                          column-fn
                                                          opts)))
                   
                   (== "reverse" (xt/x:get-path attr ["ref" "type"]))
                   (do (cond (xt/x:is-string? clause)
                             (:= clause {:id clause})
                             
                             (and (xt/x:is-array? clause)
                                  (xt/x:is-string? (xt/x:first clause)))
                             (:= clause {:id clause}))
                       
                       (cond (xt/x:is-object? clause)
                             (return (reverse-fn clause))
                             
                             (and (xt/x:is-array? clause)
                                  (xt/x:is-object? (xt/x:first clause)))
                             (return (arr-fn reverse-fn clause)))
                       #_(xt/x:TRACE! [table-name k clause (xt/x:get-path attr ["ref" "type"])])))
        
        :else
        (return (ut/encode-query-segment key clause column-fn opts))))

(defn.xt select-where
  "formats the query return"
  {:added "4.0"}
  [schema table-name return-str where-params indent opts]
  (var table-fn   (xt/x:get-key opts "table_fn" (fn [x] (return x))))
  (var column-fn  (xt/x:get-key opts "column_fn" (fn [x] (return x))))
  (when (not (xt/x:is-array? where-params))
    (:= where-params [where-params]))
  (var clause-fn
       (fn [clause]
         (var pair-fn
              (fn [pair]
                (return (-/select-where-pair
                         schema
                         table-name
                         (xt/x:first pair)
                         (xt/x:second pair)
                         (+ indent 2)
                         opts
                         -/select-where))))
         (var clause-arr (xt/x:arr-map (xt/x:obj-pairs clause)
                                    pair-fn))
         (return (xt/x:str-join " AND " clause-arr))))
  (var where-arr  (-> (xt/x:arr-map where-params clause-fn)
                      (xt/x:arr-filter xtd/not-empty?)))
  (var where-str  (:? (== 0 (xt/x:len where-arr)) ""
                      (== 1 (xt/x:len where-arr)) (xt/x:first where-arr)
                      :else (xt/x:str-join " OR "
                                           (xt/x:arr-map where-arr (fn:> [s] (xt/x:cat "(" s ")"))))))
  (var out-arr    [(xt/x:cat "SELECT " return-str)
                   (xt/x:cat " FROM "  (table-fn table-name))])
  (if (< 0 (xt/x:len where-str))
    (xt/x:arr-push out-arr (xt/x:cat "\n" (xt/x:str-pad-left "" indent " ")
                               "WHERE " where-str)))
  (return (xt/x:str-join "" out-arr)))

(defn.xt select-return-str
  "select return string loop"
  {:added "4.0"}
  [schema
   params
   return-fn
   indent
   opts]
  (var column-fn   (xt/x:get-key opts "column_fn" (fn [x] (return x))))
  (var return-count-fn   (xt/x:get-key opts "return_count_fn" (fn []
                                                             (return "count(*)"))))
  (var return-format-fn  (xt/x:get-key opts "return_format_fn" ut/default-return-format-fn))
  (var return-join-fn    (xt/x:get-key opts "return_join_fn" (fn [arr] (return (xt/x:str-join ", " arr)))))
  (var return-link-fn    (xt/x:get-key opts "return_link_fn" (fn [s link-name]
                                                            (return (xt/x:cat "(" s ") AS " link-name)))))
  (var nest-fn
       (fn [link]
         (var link-name (xt/x:first link))
         (var link-tree (xt/x:last link))
         (var link-ret  (return-fn schema link-tree 2 opts))
         (return (return-link-fn link-ret
                                 link-name))))
  (var format-fn
       (fn [v]
         (return (return-format-fn v nest-fn column-fn opts))))
  
  (var data-params   (xt/x:get-key params "data"))
  (var link-params   (xt/x:get-key params "links"))
  (var custom-params (xt/x:get-key params "custom"))

  (when (and (== 1 (xt/x:len custom-params))
             (== "sql/count"
                 (. (xt/x:first custom-params)
                    ["::"])))
    (return (return-count-fn)))  
  
  (var return-data   (xt/x:arr-map data-params format-fn))
  (var return-links  (xt/x:arr-map link-params format-fn))
  (return  (return-join-fn
            (xtd/arr-mapcat [return-data
                              return-links]
                             (fn [x] (return x))))))

(defn.xt select-return
  "select return call"
  {:added "4.0"}
  [schema tree indent opts]
  (var column-fn   (xt/x:get-key opts "column_fn" (fn [x] (return x))))
  (var wrapper-fn  (xt/x:get-key opts "wrapper_fn" (fn [s indent] (return s))))
  (var format-fn   (fn:> [input] (ut/encode-sql input column-fn opts ut/encode-loop-fn)))
  (var [table-name params] tree)
  (var where-params  (xt/x:get-key params "where"))
  (var custom-params (xt/x:arr-filter (or (xt/x:get-key params "custom")
                                       [])
                                   (fn:> [e] (== (. e ["::"])
                                                 "sql/keyword"))))
  (var return-str   (-/select-return-str schema
                                         params
                                         -/select-return
                                         indent
                                         opts))
  (var return-base  (-/select-where schema table-name return-str where-params 2 opts))
  (return (wrapper-fn (xt/x:str-join " "
                                     [return-base
                                      (xt/x:unpack (xt/x:arr-map custom-params format-fn))])
                      (:? (> indent 0)
                          2
                          0))))

(defn.xt select-tree
  "gets the selection tree structure"
  {:added "4.0"}
  [schema query opts]
  (var input (scope/get-link-standard query))
  (var [table-name linked] input)
  (var return-params (xt/x:last linked))
  (var where-params  (xt/x:arr-filter linked (fn [x]
                                            (return (and (xt/x:is-object? x)
                                                         (xtd/not-empty? x))))))
  (var tree (scope/get-tree schema table-name where-params return-params opts))
  (return tree))

(defn.xt select
  "encodes a select state given schema and graph"
  {:added "4.0"}
  [schema query opts]
  (var tree (-/select-tree schema query opts))
  (return (-/select-return schema tree 0 opts)))
