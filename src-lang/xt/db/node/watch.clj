(ns xt.db.node.watch
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(defn.xt remove-query-watch
  "removes a query from the table watch index"
  {:added "4.1"}
  [state query-key tables]
  (var watch (xt/x:get-key state "watch"))
  (when (xt/x:is-object? tables)
    (xt/for:object [[table _] tables]
      (var queries (xt/x:get-key watch table))
      (when (xt/x:not-nil? queries)
        (xt/x:del-key queries query-key)
        (when (== 0 (xt/x:len (xt/x:obj-keys queries)))
          (xt/x:del-key watch table)))))
  (return true))

(defn.xt watch-query
  "indexes a query by each table it touches"
  {:added "4.1"}
  [state query-key tables]
  (var watch (xt/x:get-key state "watch"))
  (when (xt/x:is-object? tables)
    (xt/for:object [[table _] tables]
      (var queries (xt/x:get-key watch table))
      (when (xt/x:nil? queries)
        (:= queries {})
        (xt/x:set-key watch table queries))
      (xt/x:set-key queries query-key true)))
  (return tables))

(defn.xt affected-query-ids
  "gets cached query ids affected by a set of tables"
  {:added "4.1"}
  [state tables]
  (var out {})
  (var watch (xt/x:get-key state "watch"))
  (cond (xt/x:is-array? tables)
        (xt/for:array [table tables]
          (xt/for:object [[query-key _] (or (xt/x:get-key watch table) {})]
            (xt/x:set-key out query-key true)))

        (xt/x:is-object? tables)
        (xt/for:object [[table _] tables]
          (xt/for:object [[query-key _] (or (xt/x:get-key watch table) {})]
            (xt/x:set-key out query-key true))))
  (return (xt/x:obj-keys out)))

(defn.xt remove-query
  "removes a cached query and its watch entries"
  {:added "4.1"}
  [state query-key]
  (var queries (xt/x:get-key state "queries"))
  (var prev (xt/x:get-key queries query-key))
  (when (xt/x:not-nil? prev)
    (-/remove-query-watch state query-key (xt/x:get-key prev "tables"))
    (xt/x:del-key queries query-key))
  (return prev))
