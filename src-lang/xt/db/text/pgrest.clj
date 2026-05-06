(ns xt.db.text.pgrest
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(defn.xt compile-select-item
  "compiles a returning entry to PostgREST select syntax"
  {:added "4.1"}
  [item]
  (cond (xt/x:is-string? item)
        (return item)

        (and (xt/x:is-array? item)
             (== 2 (xt/x:len item))
             (xt/x:is-string? (xt/x:first item))
             (xt/x:is-string? (xt/x:second item)))
        (return (xt/x:cat (xt/x:first item)
                          ":"
                          (xt/x:second item)))

        :else
        (do (var rel (xt/x:first item))
            (var entries (xt/x:second item))
            (return (xt/x:cat rel
                              "("
                              (xt/x:str-join ","
                                             (xt/x:arr-map entries -/compile-select-item))
                              ")")))))

(defn.xt compile-select
  "compiles a return vector to PostgREST select syntax"
  {:added "4.1"}
  [entries]
  (var out (xt/x:str-join ","
                          (xt/x:arr-map (or entries []) -/compile-select-item)))
  (return (:? (xtd/not-empty? out)
              out
              "*")))

(defn.xt filter-operator?
  "checks whether a value looks like a PostgREST filter operator"
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
              (== op "in")
              (== op "fts")
              (== op "plfts")
              (== op "phfts")
              (== op "wfts")
              (== op "cs")
              (== op "cd")
              (== op "ov")
              (== op "sl")
              (== op "sr")
              (== op "nxl")
              (== op "nxr")
              (== op "adj")
              (== op "not")
              (== op "match"))))

(defn.xt top-level-control?
  "checks whether a top-level key is reserved for request controls"
  {:added "4.1"}
  [prefix key]
  (return (and (== prefix "")
               (or (== key "$order")
                   (== key "$limit")
                   (== key "$offset")
                   (== key "order")
                   (== key "limit")
                   (== key "offset")))))

(defn.xt value->query-text
  "formats a value for PostgREST query-string output"
  {:added "4.1"}
  [value]
  (cond (xt/x:nil? value)
        (return "null")

        (xt/x:is-string? value)
        (return value)

        (xt/x:is-number? value)
        (return (xt/x:to-string value))

        (xt/x:is-boolean? value)
        (return (:? value "true" "false"))

        :else
        (return (xt/x:to-string value))))

(defn.xt normalise-in-values
  "normalises Supabase-style `in` operands into a flat array"
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
  (var path  (xt/x:get-key filter "path"))
  (var op    (xt/x:get-key filter "op"))
  (var value (xt/x:get-key filter "value"))
  (return (xt/x:cat path
                    "."
                    (-/compile-filter-value op value))))

(defn.xt compile-order-value
  "compiles ordering declarations into PostgREST order syntax"
  {:added "4.1"}
  [value]
  (cond (xt/x:is-string? value)
        (return value)

        (and (xt/x:is-array? value)
             (xtd/not-empty? value)
             (xt/x:is-string? (xt/x:first value)))
        (return (xt/x:str-join "."
                               (xt/x:arr-map value -/value->query-text)))

        (xt/x:is-array? value)
        (return (xt/x:str-join ","
                               (xt/x:arr-map value -/compile-order-value)))

        :else
        (return (-/value->query-text value))))

(defn.xt compile-filters-into
  "compiles nested where params into PostgREST filter descriptors"
  {:added "4.1"}
  [prefix obj out]
  (xt/for:array [key (xtd/arr-sort (xt/x:obj-keys obj)
                                   xt/x:to-string
                                   xt/x:str-comp)]
    (var value (xt/x:get-key obj key))
    (when (not (-/top-level-control? prefix key))
      (var path (:? (xtd/not-empty? prefix)
                    (xt/x:cat prefix "." key)
                    key))
      (cond (and (== prefix "")
                 (or (== key "$or")
                     (== key "or")
                      (== key "or_")))
            (xt/x:arr-push out {"path" "or"
                                "op" "or"
                                "value" value})

            (and (xt/x:is-object? value)
                 (not (xt/x:is-array? value)))
            (-/compile-filters-into path value out)

            (and (xt/x:is-array? value)
                 (xt/x:is-string? (xt/x:first value))
                 (-/filter-operator? (xt/x:first value)))
            (xt/x:arr-push out {"path" path
                                "op" (xt/x:first value)
                                "value" (:? (== "in" (xt/x:first value))
                                            (-/normalise-in-values (xt/x:second value))
                                            (xt/x:second value))})

            :else
            (xt/x:arr-push out {"path" path
                                "op" "eq"
                                "value" value}))))
  (return out))

(defn.xt compile-or-clause
  "compiles an `or` clause entry into its fragment form"
  {:added "4.1"}
  [entry]
  (cond (xt/x:is-string? entry)
        (return entry)

        (and (xt/x:is-object? entry)
             (xt/x:not-nil? (xt/x:get-key entry "path"))
             (xt/x:not-nil? (xt/x:get-key entry "op")))
        (return (-/compile-filter-fragment entry))

        :else
        (do (var clauses [])
            (xt/for:array [filter (-/compile-filters-into "" entry [])]
              (xt/x:arr-push clauses (-/compile-filter-fragment filter)))
            (return (xt/x:str-join "," clauses)))))

(defn.xt apply-filter
  "compiles one filter descriptor into one or more PostgREST query params"
  {:added "4.1"}
  [filter]
  (var path  (xt/x:get-key filter "path"))
  (var op    (xt/x:get-key filter "op"))
  (var value (xt/x:get-key filter "value"))
  (cond (== op "match")
        (do (var out [])
            (xt/for:array [sub (-/compile-filters-into "" value [])]
              (:= out (xt/x:arr-concat out (-/apply-filter sub))))
            (return out))

        (== op "or")
        (return [(xt/x:cat "or=("
                           (xt/x:str-join ","
                                          (xt/x:arr-map (or value [])
                                                        -/compile-or-clause))
                           ")")])

        :else
        (return [(xt/x:cat path
                           "="
                           (-/compile-filter-value op value))])))

(defn.xt compile-filter-params
  "compiles filter descriptors into query-string params"
  {:added "4.1"}
  [filters]
  (var out [])
  (xt/for:array [filter (or filters [])]
    (:= out (xt/x:arr-concat out (-/apply-filter filter))))
  (return out))

(defn.xt compile-control-params
  "compiles request-level controls like order, limit, and offset"
  {:added "4.1"}
  [where]
  (var out [])
  (var order (xt/x:get-key where "$order"))
  (var limit (xt/x:get-key where "$limit"))
  (var offset (xt/x:get-key where "$offset"))
  (when (xt/x:nil? order)
    (:= order (xt/x:get-key where "order")))
  (when (xt/x:nil? limit)
    (:= limit (xt/x:get-key where "limit")))
  (when (xt/x:nil? offset)
    (:= offset (xt/x:get-key where "offset")))
  (when (xt/x:not-nil? order)
    (xt/x:arr-push out (xt/x:cat "order="
                                 (-/compile-order-value order))))
  (when (xt/x:not-nil? limit)
    (xt/x:arr-push out (xt/x:cat "limit="
                                 (-/value->query-text limit))))
  (when (xt/x:not-nil? offset)
    (xt/x:arr-push out (xt/x:cat "offset="
                                 (-/value->query-text offset))))
  (return out))

(defn.xt compile-query-string
  "joins request params into a PostgREST query string"
  {:added "4.1"}
  [params]
  (return (xt/x:str-join "&" (or params []))))

(defn.xt compile-url
  "joins a PostgREST path and params into a request url"
  {:added "4.1"}
  [path params]
  (var query (-/compile-query-string params))
  (return (:? (xtd/not-empty? query)
              (xt/x:cat path "?" query)
              path)))

(defn.xt compile-rpc
  "compiles an RPC call into a PostgREST request description"
  {:added "4.1"}
  [fn-name args]
  (var rpc-name (xt/x:str-replace (xt/x:to-string fn-name) "-" "_"))
  (var path (xt/x:cat "/rest/v1/rpc/" rpc-name))
  (return {"type" "rpc"
           "fn" rpc-name
           "method" "POST"
           "path" path
           "url" path
           "body" (or args {})
           "headers" {"Content-Type" "application/json"}}))

(defn.xt compile-request
  "compiles a PostgREST query plan into a transport-friendly request map"
  {:added "4.1"}
  [query-plan]
  (var table-name (xt/x:first query-plan))
  (var second (xt/x:second query-plan))
  (var third  (xt/x:last query-plan))
  (var where (:? (xt/x:is-object? second)
                 second
                 {}))
  (var returning (:? (xt/x:is-object? second)
                     third
                     second))
  (var select (-/compile-select returning))
  (var filters (-/compile-filters-into "" where []))
  (var params [])
  (xt/x:arr-push params (xt/x:cat "select=" select))
  (:= params (xt/x:arr-concat params (-/compile-filter-params filters)))
  (:= params (xt/x:arr-concat params (-/compile-control-params where)))
  (var path (xt/x:cat "/rest/v1/" table-name))
  (var query (-/compile-query-string params))
  (return {"type" "query"
           "table" table-name
           "method" "GET"
           "path" path
           "select" select
           "filters" filters
           "params" params
           "query" query
           "url" (-/compile-url path params)
           "headers" {}}))

(defn.xt compile-query
  "compiles a query plan into a PostgREST request description"
  {:added "4.1"}
  [query-plan]
  (return (-/compile-request query-plan)))
