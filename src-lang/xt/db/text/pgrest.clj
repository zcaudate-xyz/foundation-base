(ns xt.db.text.pgrest
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

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
