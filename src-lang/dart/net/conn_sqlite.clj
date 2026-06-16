(ns dart.net.conn-sqlite
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :refer [defimpl.xt]]))

(l/script :dart
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-protocol :as protocol]
             [xt.net.conn-sql :as conn-sql]]
   :import [["package:sqlite3/sqlite3.dart" :as sqlite]]})

(defn.dt query-returns-rows?
  "Checks whether a query should be executed with `select`."
  {:added "4.1"}
  [query]
  (var sql (. (. query (trimLeft)) (toLowerCase)))
  (return (or (. sql (startsWith "select"))
              (. sql (startsWith "pragma"))
              (. sql (startsWith "with"))
              (. sql (startsWith "values"))
              (. sql (startsWith "explain")))))

(defn.dt error-output
  "Normalises thrown values into a printable error payload."
  {:added "4.1"}
  [err]
  (if (xt/x:nil? err)
    (return nil))
  (return (. err (toString))))

(defn.dt callback-return
  "Calls a node-style callback when present, otherwise returns or throws."
  {:added "4.1"}
  [callback err result]
  (if (xt/x:not-nil? callback)
    (return (callback (-/error-output err) result)))
  (if (xt/x:not-nil? err)
    (throw err))
  (return result))

(defn.dt decode-json-scalar
  [value]
  (cond (and (xt/x:is-string? value)
             (or (. value (startsWith "["))
                 (. value (startsWith "{"))
                 (== value "true")
                 (== value "false")
                 (== value "null")))
        (return (xt/x:json-decode value))

        :else
        (return value)))

(defn.dt raw-query
  "Runs a raw sqlite query and normalises the result shape."
  {:added "4.1"}
  [db query]
  (if (-/query-returns-rows? query)
    (do (var result (. db (select query)))
        (var columns (. result columnNames))
        (var values nil)
        (:= values [])
        (xt/for:iter [row (xt/x:iter-from result)]
          (var row-values nil)
          (:= row-values [])
          (xt/for:array [[i _] columns]
            (xt/x:arr-push row-values (. row [i])))
          (xt/x:arr-push values row-values))
        (when (and (== 1 (xt/x:len values))
                   (== 1 (xt/x:len (. values [0]))))
          (return (-/decode-json-scalar
                   (. values [0] [0]))))
        (if (> (xt/x:len columns) 0)
          (return [{"columns" columns
                    "values" values}]))
        (return values))
    (do (. db (execute query))
        (return []))))

(defn.dt client-connect
  [client opts]
  (var #{defaults} client)
  (var env (or opts {}))
  (var memory (xt/x:get-key env "memory"))
  (var filename (xt/x:get-key env "filename"))
  (when (and (xt/x:nil? memory)
             (xt/x:nil? filename))
    (:= memory true))
  (var db nil)
  (if memory
    (:= db (sqlite.sqlite3.openInMemory))
    (:= db (sqlite.sqlite3.open filename)))
  (xt/x:set-key client "raw" db)
  (return client))

(defn.dt client-disconnect
  [client]
  (var #{raw} client)
  (. raw (dispose))
  (return true))

(defn.dt client-query
  [client query]
  (var #{raw} client)
  (return (-/raw-query raw query)))

(defn.dt client-query-async
  [client query]
  (return (protocol/ensure-promise (-/client-query client query))))

(defimpl.xt ^{:lang :dart}
  DartSqliteClient
  [defaults raw]
  conn-sql/ISqlClient
  {conn-sql/connect      -/client-connect
   conn-sql/disconnect   -/client-disconnect
   conn-sql/query        -/client-query
   conn-sql/query-async  -/client-query-async})

(defn.dt create
  [defaults]
  (return
   (-/DartSqliteClient (or defaults {}) nil)))
