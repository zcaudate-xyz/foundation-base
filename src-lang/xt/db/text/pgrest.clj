(ns xt.db.text.pgrest
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(defn.xt query-client?
  "checks whether a value looks like a PostgREST query client"
  {:added "4.1"}
  [value]
  (return (and (xt/x:is-object? value)
               (xt/x:is-function? (xt/x:get-key value "from")))))

(defn.xt resolve-client
  "resolves a PostgREST client from db or opts"
  {:added "4.1"}
  [db opts]
  (var db-supabase (xt/x:get-key db "supabase"))
  (var db-client   (xt/x:get-key db "client"))
  (var opt-supabase (xt/x:get-key opts "supabase"))
  (var opt-client   (xt/x:get-key opts "client"))
  (cond (-/query-client? db-supabase)
        (return db-supabase)

        (-/query-client? db-client)
        (return db-client)

        (-/query-client? opt-supabase)
        (return opt-supabase)

        (-/query-client? opt-client)
        (return opt-client)

        (-/query-client? db)
        (return db)

        (-/query-client? opts)
        (return opts)

        :else
        (return nil)))

(defn.xt resolve-schema-name
  "resolves an optional PostgREST schema name from db or opts"
  {:added "4.1"}
  [db opts]
  (var db-schema (xt/x:get-key db "schema"))
  (var opt-schema (xt/x:get-key opts "schema"))
  (var schema-name (or (xt/x:get-key db "schema-name")
                       (xt/x:get-key db "schema_name")
                       (xt/x:get-key db "supabase-schema")
                       (xt/x:get-key db "supabase_schema")
                       (:? (xt/x:is-string? db-schema) db-schema nil)
                       (xt/x:get-key opts "schema-name")
                       (xt/x:get-key opts "schema_name")
                       (xt/x:get-key opts "supabase-schema")
                       (xt/x:get-key opts "supabase_schema")
                       (:? (xt/x:is-string? opt-schema) opt-schema nil)))
  (return (:? (xt/x:is-string? schema-name)
              schema-name
              nil)))

(defn.xt compile-select-item
  "compiles a returning entry to PostgREST select syntax"
  {:added "4.1"}
  [item]
  (if (xt/x:is-string? item)
    (return item)
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
  (return (xt/x:str-join ","
                         (xt/x:arr-map (or entries []) -/compile-select-item))))

(defn.xt compile-filters-into
  "compiles nested where params into PostgREST filter descriptors"
  {:added "4.1"}
  [prefix obj out]
  (xt/for:object [[key value] obj]
    (var path (:? (xtd/not-empty? prefix)
                  (xt/x:cat prefix "." key)
                  key))
    (cond (and (xt/x:is-object? value)
               (not (xt/x:is-array? value)))
          (-/compile-filters-into path value out)

          (and (xt/x:is-array? value)
               (== "in" (xt/x:first value)))
          (do (var in-values (xt/x:first (xt/x:second value)))
              (xt/x:arr-push out {"path" path
                                  "op" "in"
                                  "value" in-values}))

          :else
          (xt/x:arr-push out {"path" path
                              "op" "eq"
                              "value" value})))
  (return out))

(defn.xt compile-query
  "compiles a query plan into a PostgREST request description"
  {:added "4.1"}
  [query-plan]
  (var table (xt/x:first query-plan))
  (var second (xt/x:second query-plan))
  (var third  (xt/x:last query-plan))
  (var where (:? (xt/x:is-object? second)
                 second
                 {}))
  (var returning (:? (xt/x:is-object? second)
                     third
                     second))
  (return {"table" table
           "select" (-/compile-select returning)
           "filters" (-/compile-filters-into "" where [])}))

(defn.xt invoke-method-1
  "invokes a PostgREST query-builder method by key with one argument"
  {:added "4.1"}
  [obj method arg]
  (var f (xt/x:get-key obj method))
  (when (not (xt/x:is-function? f))
    (xt/x:err (xt/x:cat "pgrest method not found - "
                        method)))
  (return (f arg)))

(defn.xt invoke-method-2
  "invokes a PostgREST query-builder method by key with two arguments"
  {:added "4.1"}
  [obj method arg1 arg2]
  (var f (xt/x:get-key obj method))
  (when (not (xt/x:is-function? f))
    (xt/x:err (xt/x:cat "pgrest method not found - "
                        method)))
  (return (f arg1 arg2)))

(defn.xt apply-filter
  "applies one compiled filter to a PostgREST query builder"
  {:added "4.1"}
  [query filter]
  (var path  (xt/x:get-key filter "path"))
  (var op    (xt/x:get-key filter "op"))
  (var value (xt/x:get-key filter "value"))
  (cond (== op "eq")
        (return (-/invoke-method-2 query "eq" path value))

        (== op "neq")
        (return (-/invoke-method-2 query "neq" path value))

        (== op "gt")
        (return (-/invoke-method-2 query "gt" path value))

        (== op "gte")
        (return (-/invoke-method-2 query "gte" path value))

        (== op "lt")
        (return (-/invoke-method-2 query "lt" path value))

        (== op "lte")
        (return (-/invoke-method-2 query "lte" path value))

        (== op "like")
        (return (-/invoke-method-2 query "like" path value))

        (== op "ilike")
        (return (-/invoke-method-2 query "ilike" path value))

        (== op "is")
        (return (-/invoke-method-2 query "is" path value))

        (== op "in")
        (return (-/invoke-method-2 query "in" path value))

        (== op "contains")
        (return (-/invoke-method-2 query "contains" path value))

        (== op "containedBy")
        (return (-/invoke-method-2 query "containedBy" path value))

        (== op "match")
        (return (-/invoke-method-1 query "match" value))

        :else
        (xt/x:err (xt/x:cat "unsupported pgrest filter op - "
                            (xt/x:to-string op)))))

(defn.xt apply-filters
  "applies compiled filters to a PostgREST query builder"
  {:added "4.1"}
  [query filters]
  (var out query)
  (xt/for:array [filter (or filters [])]
    (:= out (-/apply-filter out filter)))
  (return out))

(defn.xt execute-query-default
  "executes a compiled query using a PostgREST-compatible client descriptor"
  {:added "4.1"}
  [db compiled opts]
  (var client (-/resolve-client db opts))
  (when (xt/x:nil? client)
    (return nil))
  (var schema-name (-/resolve-schema-name db opts))
  (var query (:? (and (xt/x:not-nil? schema-name)
                      (xt/x:is-function? (xt/x:get-key client "schema")))
                  (-/invoke-method-1 client "schema" schema-name)
                  client))
  (:= query (-/invoke-method-1 query "from" (xt/x:get-key compiled "table")))
  (:= query (-/invoke-method-1 query
                               "select"
                               (:? (xtd/not-empty? (xt/x:get-key compiled "select"))
                                   (xt/x:get-key compiled "select")
                                   "*")))
  (return (-/apply-filters query
                           (xt/x:get-key compiled "filters"))))
