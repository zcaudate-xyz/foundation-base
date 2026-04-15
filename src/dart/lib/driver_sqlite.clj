(ns dart.lib.driver-sqlite
  (:require [std.lang :as l]))

(l/script :dart
  {:require [[xt.lang.common-spec :as xt]]
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

(defn.dt raw-query
  "Runs a raw sqlite query and normalises the result shape."
  {:added "4.1"}
  [db query]
  (if (-/query-returns-rows? query)
    (do (var result (. db (select query)))
        (var columns (. result columnNames))
        (var values nil)
        (:= values [])
        (xt/for:iter [row result]
          (var row-values nil)
          (:= row-values [])
          (xt/for:array [[i _] columns]
            (xt/x:arr-push row-values (. row [i])))
          (xt/x:arr-push values row-values))
        (when (and (== 1 (xt/x:len values))
                   (== 1 (xt/x:len (. values [0]))))
          (return (. values [0] [0])))
        (if (> (xt/x:len columns) 0)
          (return [{"columns" columns
                    "values" values}]))
        (return values))
    (do (. db (execute query))
        (return []))))

(defn.dt set-methods
  "Attaches the DBSQL driver methods expected by `xt.sys.conn-dbsql`."
  {:added "4.1"}
  [db]
  (var conn {"raw" db})
  (:= (. conn ["::disconnect"])
      (fn [callback]
        (try (. db (dispose))
             (return (-/callback-return callback nil true))
             (catch err
               (return (-/callback-return callback err nil))))))
  (:= (. conn ["::query"])
      (fn [query callback]
        (try (return (-/callback-return callback nil (-/raw-query db query)))
             (catch err
               (return (-/callback-return callback err nil))))))
  (:= (. conn ["::query_sync"])
      (fn [query]
        (return (-/raw-query db query))))
  (:= (. conn ["run"])
      (fn [query]
        (. db (execute query))
        (return conn)))
  (return conn))

(defn.dt ^{:static/override true} connect-constructor
  "Connects to a SQLite database through `package:sqlite3`."
  {:added "4.1"}
  [m callback]
  (try
    (when (xt/x:nil? m)
      (:= m {}))
    (var memory (xt/x:get-key m "memory"))
    (var filename (xt/x:get-key m "filename"))
    (when (xt/x:nil? filename)
      (:= filename "sqlite.db"))
    (var db nil)
    (if memory
      (:= db (sqlite.sqlite3.openInMemory))
      (:= db (sqlite.sqlite3.open filename)))
    (return (-/callback-return callback nil (-/set-methods db)))
    (catch err
      (return (-/callback-return callback err nil)))))
