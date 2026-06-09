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
                    (return (== (xt/x:get-key entry "::")
                                "sql/count"))))))

(defn.xt value->query-text
  "formats a value for PostgREST query-string output"
  {:added "4.1"}
  [value]
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
  (return (xt/x:cat (xt/x:get-key filter "path")
                    "."
                    (-/compile-filter-value (xt/x:get-key filter "op")
                                            (xt/x:get-key filter "value")))))

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
               (xt/x:nil? (xt/x:get-key value "::")))
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
                     (return (xt/x:cat (xt/x:get-key filter "path")
                                       "="
                                       (-/compile-filter-value (xt/x:get-key filter "op")
                                                               (xt/x:get-key filter "value"))))))))
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
                          "("
                          (select-params-fn (xtd/second (xtd/nth item 2)))
                          ")"))

        :else
        (return (xt/x:to-string item))))

(defn.xt compile-tree-select-params
  "compiles tree-ir data/link params into PostgREST select syntax"
  {:added "4.1"}
  [params]
  (var custom (xt/x:get-key params "custom"))
  (var data (xt/x:get-key params "data"))
  (var links (xt/x:get-key params "links"))
  (when (-/tree-count? custom)
    (return "count"))
  (var out [])
  (xt/x:arr-assign out (or data []))
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
    (when (== (xt/x:get-key entry "::") "sql/keyword")
      (var name (xt/x:get-key entry "name"))
      (cond (== name "ORDER BY")
            (do (var tuple (xt/x:first (or (xt/x:get-key entry "args") [])))
                (:= order-cols
                    (xt/x:arr-map (or (xt/x:get-key tuple "args") [])
                                  (fn [arg]
                                    (return (xt/x:get-key arg "name"))))))

            (or (== name "ASC")
                (== name "DESC"))
            (:= order-sort (xt/x:str-to-lower name))

            (== name "LIMIT")
            (:= limit (xt/x:get-key (xt/x:first (or (xt/x:get-key entry "args") []))
                                    "name"))

            (== name "OFFSET")
            (:= offset (xt/x:get-key (xt/x:first (or (xt/x:get-key entry "args") []))
                                     "name")))))
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
  (var where (or (xt/x:get-key params "where") []))
  (var custom (or (xt/x:get-key params "custom") []))
  (var select (-/compile-tree-select-params params))
  (var request-params [(xt/x:cat "select=" select)])
  (:= request-params (xt/x:arr-concat request-params (-/compile-where-params where)))
  (:= request-params (xt/x:arr-concat request-params (-/compile-control-params custom)))
  (var path (xt/x:cat "/rest/v1/" table-name))
  (var query (-/compile-query-string request-params))
  (return {"type" "query"
           "table" table-name
           "method" "GET"
           "path" path
           "select" select
           "filters" where
           "params" request-params
           "query" query
           "url" (-/compile-url path request-params)
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
