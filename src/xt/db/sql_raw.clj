(ns xt.db.sql-raw
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.db.sql-util :as ut]]})

(defn.xt raw-delete
  "encodes a delete query"
  {:added "4.0"}
  [table-name where-params opts]
  (var table-fn   (k/get-key opts "table_fn" k/identity))
  (var where-str  (:? (k/nil? where-params) ""
                      :else (ut/encode-query-string where-params
                                                    "WHERE"
                                                    opts)))
  (var out-arr    [(k/cat "DELETE FROM " (table-fn table-name))])
  (when (< 0 (k/len where-str))
    (x:arr-push out-arr where-str))
  (return (k/cat (k/join " " out-arr) ";")))

(defn.xt raw-insert-array
  "constructs an array for insert and upsert"
  {:added "4.0"}
  ([table-name columns values opts]
   (var table-fn   (k/get-key opts "table_fn" k/identity))
   (var column-fn  (k/get-key opts "column_fn" k/identity))
   (var out-arr    [(k/cat "INSERT INTO " (table-fn table-name))
                    (k/cat " (" (k/join ", " (k/arr-map columns column-fn)) ")")])
   (var val-fn
        (fn [data]
          (var s-arr (k/arr-map
                      columns
                      (fn:> [k] (ut/encode-value (k/get-key data k)))))
          (return
           (k/cat "(" (k/join "," s-arr) ")"))))
   (var val-arr    (k/arr-map values val-fn))
   (var val-str    (k/cat " VALUES\n "
                          (k/join ",\n " val-arr)))
   (x:arr-push out-arr val-str)
   (return out-arr)))

(defn.xt raw-insert
  "encodes an insert query"
  {:added "4.0"}
  [table-name columns values opts]
  (var out-arr (-/raw-insert-array table-name columns values opts))
  (return (k/cat (k/join "\n" out-arr) ";")))

(defn.xt raw-upsert
  "encodes an upsert query"
  {:added "4.0"}
  ([table-name id-column columns values opts]
   (var table-fn   (k/get-key opts "table_fn" k/identity))
   (var column-fn  (k/get-key opts "column_fn" k/identity))
   (var upsert-clause  (k/get-key opts "upsert_clause"))
   (var out-arr (-/raw-insert-array table-name columns values opts))
   (var col-arr (-> columns
                    (k/arr-filter (fn [col]
                                    (return (not= col id-column))))
                    (k/arr-map (fn [col]
                                 (return (k/cat (column-fn col)
                                                "=coalesce(\"excluded\"."
                                                (column-fn col)
                                                ","
                                                (column-fn col)
                                                ")"))))))
   (return (k/cat (k/join "\n" out-arr)
                  "\n"
                  (k/cat "ON CONFLICT (" (column-fn id-column) ") DO UPDATE SET\n")
                  (k/join ",\n" col-arr)
                  (:? (k/is-string? upsert-clause)
                      (k/cat "\nWHERE " upsert-clause)
                      "")
                  ";"))))

(defn.xt raw-update
  "encodes an update query"
  {:added "4.0"}
  ([table-name where-params data opts]
   (var table-fn   (k/get-key opts "table_fn" k/identity))
   (var where-str  (:? (k/nil? where-params) ""
                       :else (ut/encode-query-string where-params
                                                    "WHERE"
                                                    opts)))
   (var set-str    (ut/encode-query-string data
                                          "SET"
                                          opts))
   (var out-arr    [(k/cat "UPDATE " (table-fn table-name))
                    set-str
                    where-str])
   (return (k/cat (k/join "\n " out-arr) ";"))))

(defn.xt raw-select
  "encodes an select query"
  {:added "4.0"}
  ([table-name where-params return-params opts]
   (var table-fn   (k/get-key opts "table_fn" k/identity))
   (var column-fn  (k/get-key opts "column_fn" k/identity))
   
   (var return-str  (:? (k/is-string? return-params)
                        return-params
                        (k/join ", " (k/arr-map return-params column-fn))))
   (var where-str  (:? (k/nil? where-params) ""
                       :else (ut/encode-query-string where-params
                                                     "WHERE"
                                                     opts)))
   (var out-arr    [(k/cat "SELECT " return-str)
                    (k/cat " FROM "(table-fn table-name))])
   (when (< 0 (k/len where-str))
     (x:arr-push out-arr where-str))
   (return (k/cat (k/join "\n " out-arr) ";"))))

