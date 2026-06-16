(ns python.net.conn-sqlite
  (:require [hara.lang :as l]))

(l/script :python
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-protocol :as protocol]
             [python.core :as py]
             [xt.net.conn-sql :as conn-sql]]})

(defn.py query-returns-rows?
  "Checks whether a query should return row data."
  {:added "4.1"}
  [query]
  (var sql (. (. query (lstrip)) (lower)))
  (return (or (. sql (startswith "select"))
              (. sql (startswith "pragma"))
              (. sql (startswith "with"))
              (. sql (startswith "values"))
              (. sql (startswith "explain")))))

(defn.py query-multi-statement?
  "Checks whether a query string contains multiple statements."
  {:added "4.1"}
  [query]
  (var statements
       (xt/x:arr-filter
        (. query (split ";"))
        (fn [part]
          (return (> (xt/x:len (. part (strip))) 0)))))
  (return (> (xt/x:len statements) 1)))

(defn.py decode-json-scalar
  [value]
  (cond (and (xt/x:is-string? value)
            (or (. value (startswith "["))
                (. value (startswith "{"))
                (== value "true")
                (== value "false")
                (== value "null")))
        (return (xt/x:json-decode value))

        :else
        (return value)))

(defn.py raw-query
  "Runs a raw sqlite query and normalises the result shape."
  {:added "4.1"}
  [db query]
  (var cursor (. db (cursor)))
  (var returns-rows? (-/query-returns-rows? query))
  (var multi-statement? (and (not returns-rows?)
                             (-/query-multi-statement? query)))
  (if multi-statement?
    (. cursor (executescript query))
    (. cursor (execute query)))
  (var rows (:? returns-rows?
                (. cursor (fetchall))
                []))
  (. db (commit))
  (. cursor (close))
  (cond (== 0 (xt/x:len rows))
        (return [])

        (and (== 1 (xt/x:len rows))
             (== 1 (xt/x:len (. rows [0]))))
        (return (-/decode-json-scalar
                 (. rows [0] [0])))

        :else
        (return rows)))

(defn.py client-connect
  [client opts]
  (var #{defaults} client)
  (var config (or opts {}))
  (var sqlite3 (py/pkg "sqlite3"))
  (var filename (or (xt/x:get-key config "filename")
                    ":memory:"))
  (var raw (. sqlite3 (connect filename
                              :check_same_thread false)))
  (xt/x:set-key client "raw" raw)
  (return client))

(defn.py client-disconnect
  [client]
  (var #{raw} client)
  (. raw (close))
  (return true))

(defn.py client-query
  [client query]
  (var #{raw} client)
  (return (-/raw-query raw query)))

(defn.py client-query-async
  [client query]
  (return (promise/x:promise-run (-/client-query client query))))

(defimpl.xt ^{:lang :python}
  PythonSqliteClient
  [defaults raw]
  conn-sql/ISqlClient
  {conn-sql/connect      -/client-connect
   conn-sql/disconnect   -/client-disconnect
   conn-sql/query        -/client-query
   conn-sql/query-async  -/client-query-async})

(defn.py create
  [defaults]
  (return
   (-/PythonSqliteClient (or defaults {}) nil)))
