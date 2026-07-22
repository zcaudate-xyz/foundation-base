(ns xt.db.text.pgrest-graph
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.db.text.base-graph :as base-graph]]})

(defn.xt tree-count?
  "checks whether custom tree params request a count result"
  {:added "4.1"}
  [custom]
  (return
   (xt/x:arr-some (or custom [])
                  (fn [entry]
                    (return (== (. entry ["::"])
                                "sql/count"))))))

(defn.xt pgrest-resolve-value
  "resolves sql tag objects into scalar values for PostgREST"
  {:added "4.1"}
  [value]
  (cond (and (xt/x:is-object? value)
             (xt/x:has-key? value "::"))
        (do (var tcls (. value ["::"]))
            (cond (== tcls "sql/arg")
                  (return (. value ["name"]))

                  (== tcls "sql/cast")
                  (return (-/pgrest-resolve-value
                           (xt/x:first (. value ["args"]))))

                  (== tcls "sql/defenum")
                  (return (. value ["name"]))

                  :else
                  (return value)))

        :else
        (return value)))

(defn.xt value->query-text
  "formats a value for PostgREST query-string output"
  {:added "4.1"}
  [value]
  (:= value (-/pgrest-resolve-value value))
  (cond (xt/x:nil? value)
        (return "null")

        (xt/x:is-string? value)
        (return value)

        (xt/x:is-boolean? value)
        (return (:? value "true" "false"))

        (xt/x:is-number? value)
        (return (xt/x:to-string value))

        :else
        (return (xt/x:to-string value))))

(defn.xt normalise-in-values
  "normalises `in` operands into a flat array"
  {:added "4.1"}
  [value]
  (cond (and (xt/x:is-array? value)
             (== 1 (xt/x:len value))
             (xt/x:is-array? (xt/x:first value)))
        (return (xt/x:first value))

        (xt/x:is-array? value)
        (return value)

        :else
        (return [value])))

(defn.xt filter-operator?
  "checks whether an operator is supported in filter lowering"
  {:added "4.1"}
  [op]
  (return (or (== op "eq")
              (== op "neq")
              (== op "gt")
              (== op "gte")
              (== op "lt")
              (== op "lte")
              (== op "like")
              (== op "ilike")
              (== op "is")
              (== op "in"))))

(defn.xt compile-filter-value
  "compiles the value portion of a PostgREST filter"
  {:added "4.1"}
  [op value]
  (cond (== op "in")
        (return (xt/x:cat op
                          ".("
                          (xt/x:str-join ","
                                         (xt/x:arr-map (-/normalise-in-values value)
                                                       -/value->query-text))
                          ")"))

        :else
        (return (xt/x:cat op
                          "."
                          (-/value->query-text value)))))

(defn.xt compile-filter-fragment
  "compiles a filter descriptor into a PostgREST filter fragment"
  {:added "4.1"}
  [filter]
  (return (xt/x:cat (. filter ["path"])
                    "."
                    (-/compile-filter-value (. filter ["op"])
                                            (. filter ["value"])))))

(defn.xt compile-clause-into
  "compiles nested tree where clauses into filter descriptors"
  {:added "4.1"}
  [prefix clause out]
  (xt/for:array [key (xtd/arr-sort (xt/x:obj-keys (or clause {}))
                                   xt/x:to-string
                                   xt/x:str-comp)]
    (var value (xt/x:get-key clause key))
    (var path (:? (xtd/not-empty? prefix)
                  (xt/x:cat prefix "." key)
                  key))
    (cond (and (xt/x:is-object? value)
               (not (xt/x:is-array? value))
               (xt/x:nil? (. value ["::"])))
          (-/compile-clause-into path value out)

          (and (xt/x:is-array? value)
               (xt/x:is-string? (xt/x:first value))
               (-/filter-operator? (xt/x:first value)))
          (xt/x:arr-push out {"path" path
                              "op" (xt/x:first value)
                              "value" (xt/x:second value)})

          (and (xt/x:is-array? value)
               (xt/x:is-string? (xt/x:first value)))
          (xt/x:err (xt/x:cat "Unsupported filter operator - " (xt/x:first value)))

          :else
          (xt/x:arr-push out {"path" path
                              "op" "eq"
                              "value" value})))
  (return out))

(defn.xt compile-or-clause
  "compiles one OR branch of tree where clauses"
  {:added "4.1"}
  [clause]
  (var fragments (xt/x:arr-map (-/compile-clause-into "" clause [])
                               -/compile-filter-fragment))
  (cond (== 0 (xt/x:len fragments))
        (return "")

        (== 1 (xt/x:len fragments))
        (return (xt/x:first fragments))

        :else
        (return (xt/x:cat "and("
                          (xt/x:str-join "," fragments)
                          ")"))))

(defn.xt forward-ref-column?
  "checks whether a key in the given table is a forward reference column"
  {:added "4.1"}
  [schema table-name key]
  (var table (xt/x:get-key schema table-name))
  (var col (and table (xt/x:get-key table key)))
  (return (and (xt/x:is-object? col)
               (== "ref" (. col ["type"]))
               (not (== "reverse" (xt/x:get-path col ["ref" "type"]))))))

(defn.xt flatten-forward-ref-clause
  "flattens forward reference filters from {ref {id ...}} to {ref_id ...}"
  {:added "4.1"}
  [schema table-name clause]
  (var out {})
  (xt/for:object [[k v] clause]
    (cond (and (-/forward-ref-column? schema table-name k)
               (xt/x:is-object? v)
               (xt/x:has-key? v "id"))
          (xt/x:set-key out (xt/x:cat k "_id") (. v ["id"]))

          (xt/x:is-object? v)
          (xt/x:set-key out k (-/flatten-forward-ref-clause schema table-name v))

          :else
          (xt/x:set-key out k v)))
  (return out))

(defn.xt flatten-forward-ref-filters
  "flattens forward reference filters in a where clause list"
  {:added "4.1"}
  [schema table-name where]
  (:= where (:? (xt/x:is-array? where) where (:? (xt/x:is-object? where) [where] [])))
  (return (xt/x:arr-map where
                        (fn [clause]
                          (return (-/flatten-forward-ref-clause schema table-name clause))))))

(defn.xt compile-where-params
  "compiles tree where clauses into PostgREST query params"
  {:added "4.1"}
  [where]
  (:= where (:? (xt/x:is-array? where) where (:? (xt/x:is-object? where) [where] [])))
  (when (== 0 (xt/x:len where))
    (return []))
  (when (== 1 (xt/x:len where))
    (return
     (xt/x:arr-map (-/compile-clause-into "" (xt/x:first where) [])
                   (fn [filter]
                     (return (xt/x:cat (. filter ["path"])
                                       "="
                                       (-/compile-filter-value (. filter ["op"])
                                                               (. filter ["value"]))))))))
  (var clauses (xt/x:arr-filter (xt/x:arr-map where -/compile-or-clause)
                                xtd/not-empty?))
  (return [(xt/x:cat "or=("
                     (xt/x:str-join "," clauses)
                     ")")]))

(defn.xt compile-tree-select-item
  "compiles one tree-ir select item"
  {:added "4.1"}
  [item select-params-fn]
  (cond (xt/x:is-string? item)
        (return item)

        (and (xt/x:is-array? item)
             (>= (xt/x:len item) 3)
             (xt/x:is-string? (xt/x:first item)))
        (return (xt/x:cat (xt/x:first item)
                          ":"
                          (xt/x:first (xtd/nth item 2))
                          "("
                          (select-params-fn (xtd/second (xtd/nth item 2)))
                          ")"))

        :else
        (return (xt/x:to-string (-/pgrest-resolve-value item)))))

(defn.xt compile-tree-select-params
  "compiles tree-ir data/link params into PostgREST select syntax"
  {:added "4.1"}
  [params]
  (var custom (. params ["custom"]))
  (var data (. params ["data"]))
  (var links (. params ["links"]))
  (when (-/tree-count? custom)
    (return "count"))
  (var out [])
  (xt/x:arr-assign out (xt/x:arr-map (or data [])
                                     (fn [item]
                                       (return (-/pgrest-resolve-value item)))))
  (xt/x:arr-assign out (xt/x:arr-map (or links [])
                                     (fn [item]
                                       (return
                                        (-/compile-tree-select-item item -/compile-tree-select-params)))))
  (return (:? (> (xt/x:len out) 0)
              (xt/x:str-join "," out)
              "*")))

(defn.xt compile-control-params
  "compiles custom tree control keywords into PostgREST params"
  {:added "4.1"}
  [custom]
  (var order-cols nil)
  (var order-sort nil)
  (var limit nil)
  (var offset nil)
  (xt/for:array [entry (or custom [])]
    (when (== (. entry ["::"]) "sql/keyword")
      (var name (. entry ["name"]))
      (cond (== name "ORDER BY")
            (do (var tuple (xt/x:first (or (. entry ["args"]) [])))
                (:= order-cols
                    (xt/x:arr-map (or (. tuple ["args"]) [])
                                  (fn [arg]
                                    (return (. arg ["name"]))))))

            (or (== name "ASC")
                (== name "DESC"))
            (:= order-sort (xt/x:str-to-lower name))

            (== name "LIMIT")
            (:= limit (. (xt/x:first (or (. entry ["args"]) [])) ["name"]))

            (== name "OFFSET")
            (:= offset (. (xt/x:first (or (. entry ["args"]) [])) ["name"])))))
  (var out [])
  (when (xt/x:is-array? order-cols)
    (xt/x:arr-push out
                   (xt/x:cat "order="
                             (xt/x:str-join ","
                                            (xt/x:arr-map order-cols
                                                          (fn [col]
                                                            (return (:? (xt/x:not-nil? order-sort)
                                                                        (xt/x:cat col "." order-sort)
                                                                        col))))))))
  (when (xt/x:not-nil? limit)
    (xt/x:arr-push out (xt/x:cat "limit=" (-/value->query-text limit))))
  (when (xt/x:not-nil? offset)
    (xt/x:arr-push out (xt/x:cat "offset=" (-/value->query-text offset))))
  (return out))

(defn.xt compile-query-string
  "joins request params into a PostgREST query string"
  {:added "4.1"}
  [params]
  (return (xt/x:str-join "&" (or params []))))

(defn.xt compile-url
  "joins path and params into a request url"
  {:added "4.1"}
  [path params]
  (var query (-/compile-query-string params))
  (return (:? (xtd/not-empty? query)
              (xt/x:cat path "?" query)
              path)))

(defn.xt select-return
  "compiles a tree-ir selection into a PostgREST request map"
  {:added "4.1"}
  [schema tree indent opts]
  (:= tree (base-graph/select-tree schema tree opts))
  (var table-name (xt/x:first tree))
  (var params (xtd/second tree))
  (var where (-/flatten-forward-ref-filters schema
                                            table-name
                                            (or (. params ["where"]) [])))
  (xt/x:set-key params "where" where)
  (var custom (or (. params ["custom"]) []))
  (var select (-/compile-tree-select-params params))
  (var request-params [(xt/x:cat "select=" select)])
  (:= request-params (xt/x:arr-concat request-params (-/compile-where-params where)))
  (:= request-params (xt/x:arr-concat request-params (-/compile-control-params custom)))
  (var path (xt/x:cat "/rest/v1/" table-name))
  (var query (-/compile-query-string request-params))
  (var url (-/compile-url path request-params))
  (return {"type" "query"
           "table" table-name
           "method" "GET"
           "path" path
           "select" select
           "filters" where
           "params" request-params
           "query" query
           "url" url
           "headers" {}}))

(defn.xt select-tree
  "returns the tree unchanged for api parity with sql-graph"
  {:added "4.1"}
  [schema query opts]
  (return (base-graph/select-tree schema query opts)))

(defn.xt select
  "compiles a tree-ir query into a PostgREST request map"
  {:added "4.1"}
  [schema query opts]
  (return (-/select-return schema query 0 opts)))
