(ns python.lib.driver-sqlite
  (:require [hara.lang :as l]))

(l/script :python
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [python.core :as py]
             [xt.protocol.impl.connection-sql :as sqlrt]]})

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
        (return (. rows [0] [0]))

        :else
        (return rows)))

(defn.py wrap-connection
  [conn]
  (return
   (sqlrt/connection-create
    conn
     {"disconnect" (fn [raw]
                     (. raw (close))
                     (return true))
      "query" (fn [raw query]
                (return (promise/x:promise-run
                         (-/raw-query raw query))))
      "query_sync" (fn [raw query]
                     (return (-/raw-query raw query)))})))

(defn.py connect-constructor
  "Connects to a sqlite database through the Python stdlib sqlite3 module."
  {:added "4.1"}
  [m]
  (var config (or m {}))
  (var sqlite3 (py/pkg "sqlite3"))
  (var filename (or (xt/x:get-key config "filename")
                    ":memory:"))
  (return (. sqlite3 (connect filename
                              :check_same_thread false))))

(defn.py driver
  []
  (return
   (sqlrt/driver-create
    {"connect" (fn [m]
                 (return
                  (-/wrap-connection
                   (-/connect-constructor m))))})))
