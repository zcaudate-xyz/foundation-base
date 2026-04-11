(ns xt.db.sql-raw
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.db.sql-util :as ut]]})

(defn.xt raw-delete
  "encodes a delete query"
  {:added "4.0"}
  [table-name where-params opts]
  (var table-fn   (xt/x:get-key opts "table_fn" (fn [x] (return x))))
  (var where-str  (:? (xt/x:nil? where-params) ""
                      :else (ut/encode-query-string where-params
                                                    "WHERE"
                                                    opts)))
  (var out-arr    [(xt/x:cat "DELETE FROM " (table-fn table-name))])
  (when (< 0 (xt/x:len where-str))
    (xt/x:arr-push out-arr where-str))
  (return (xt/x:cat (xt/x:join " " out-arr) ";")))

(defn.xt raw-insert-array
  "constructs an array for insert and upsert"
  {:added "4.0"}
  ([table-name columns values opts]
   (var table-fn   (xt/x:get-key opts "table_fn" (fn [x] (return x))))
   (var column-fn  (xt/x:get-key opts "column_fn" (fn [x] (return x))))
   (var out-arr    [(xt/x:cat "INSERT INTO " (table-fn table-name))
                    (xt/x:cat " (" (xt/x:join ", " (xt/x:arr-map columns column-fn)) ")")])
   (var val-fn
        (fn [data]
          (var s-arr (xt/x:arr-map
                      columns
                      (fn:> [k] (ut/encode-value (xt/x:get-key data k)))))
          (return
           (xt/x:cat "(" (xt/x:join "," s-arr) ")"))))
   (var val-arr    (xt/x:arr-map values val-fn))
   (var val-str    (xt/x:cat " VALUES\n "
                          (xt/x:join ",\n " val-arr)))
   (xt/x:arr-push out-arr val-str)
   (return out-arr)))

(defn.xt raw-insert
  "encodes an insert query"
  {:added "4.0"}
  [table-name columns values opts]
  (var out-arr (-/raw-insert-array table-name columns values opts))
  (return (xt/x:cat (xt/x:join "\n" out-arr) ";")))

(defn.xt raw-upsert
  "encodes an upsert query"
  {:added "4.0"}
  ([table-name id-column columns values opts]
   (var table-fn   (xt/x:get-key opts "table_fn" (fn [x] (return x))))
   (var column-fn  (xt/x:get-key opts "column_fn" (fn [x] (return x))))
   (var upsert-clause  (xt/x:get-key opts "upsert_clause"))
   (var out-arr (-/raw-insert-array table-name columns values opts))
   (var col-arr (-> columns
                    (xt/x:arr-filter (fn [col]
                                    (return (not= col id-column))))
                    (xt/x:arr-map (fn [col]
                                 (return (xt/x:cat (column-fn col)
                                                "=coalesce(\"excluded\"."
                                                (column-fn col)
                                                ","
                                                (column-fn col)
                                                ")"))))))
   (return (xt/x:cat (xt/x:join "\n" out-arr)
                  "\n"
                  (xt/x:cat "ON CONFLICT (" (column-fn id-column) ") DO UPDATE SET\n")
                  (xt/x:join ",\n" col-arr)
                  (:? (xt/x:is-string? upsert-clause)
                      (xt/x:cat "\nWHERE " upsert-clause)
                      "")
                  ";"))))

(defn.xt raw-update
  "encodes an update query"
  {:added "4.0"}
  ([table-name where-params data opts]
   (var table-fn   (xt/x:get-key opts "table_fn" (fn [x] (return x))))
   (var where-str  (:? (xt/x:nil? where-params) ""
                       :else (ut/encode-query-string where-params
                                                    "WHERE"
                                                    opts)))
   (var set-str    (ut/encode-query-string data
                                          "SET"
                                          opts))
   (var out-arr    [(xt/x:cat "UPDATE " (table-fn table-name))
                    set-str
                    where-str])
   (return (xt/x:cat (xt/x:join "\n " out-arr) ";"))))

(defn.xt raw-select
  "encodes an select query"
  {:added "4.0"}
  ([table-name where-params return-params opts]
   (var table-fn   (xt/x:get-key opts "table_fn" (fn [x] (return x))))
   (var column-fn  (xt/x:get-key opts "column_fn" (fn [x] (return x))))
   
   (var return-str  (:? (xt/x:is-string? return-params)
                        return-params
                        (xt/x:join ", " (xt/x:arr-map return-params column-fn))))
   (var where-str  (:? (xt/x:nil? where-params) ""
                       :else (ut/encode-query-string where-params
                                                     "WHERE"
                                                     opts)))
   (var out-arr    [(xt/x:cat "SELECT " return-str)
                    (xt/x:cat " FROM "(table-fn table-name))])
   (when (< 0 (xt/x:len where-str))
     (xt/x:arr-push out-arr where-str))
   (return (xt/x:cat (xt/x:join "\n " out-arr) ";"))))

