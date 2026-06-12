(ns js.net.conn-sqlite
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :refer [defimpl.xt]]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-protocol :as protocol]
             [xt.net.conn-sql :as conn-sql]]
   :import [["@sqlite.org/sqlite-wasm" :as sqlite3InitModule]]})

(defn.js decode-json-scalar
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

(defn.js raw-query
  "raw query for sqlite-wasm results"
  {:added "4.1"}
  [db query]
  (var columns [])
  (var values (. db (exec {:sql         query
                           :rowMode     "array"
                           :columnNames columns
                           :returnValue "resultRows"})))
  (when (and (== 1 (xt/x:len values))
             (== 1 (xt/x:len (. values [0]))))
    (return (-/decode-json-scalar
             (. values [0] [0]))))
  (return (:? (xt/x:len columns)
              [{"columns" columns
                "values"  values}]
              values)))

(defn.js raw-init
  "creates an sqlite-wasm instance once sqlite3 is loaded"
  {:added "4.1"}
  [sqlite3 opts]
  (var config (or opts {}))
  (var filename (or (xt/x:get-key config "filename")
                    ":memory:"))
  (var flags (or (xt/x:get-key config "flags")
                 "c"))
  (var conn (new (. sqlite3 ["oo1"] ["DB"]) filename flags))
  (return conn))

(defn.js client-connect
  "connects to an embedded sqlite-wasm database

   (notify/wait-on :js
      (dbsql/connect {:constructor js-sqlite/connect-constructor}
                     {:success (fn [conn]
                                 (dbsql/query conn \"SELECT 1;\"
                                              (repl/<!)))}))
    => 1"
  {:added "4.1"}
  [client opts]
  (var #{defaults} client)
  (var init-module (or (. sqlite3InitModule ["default"])
                       sqlite3InitModule))
  (return
   (. (init-module)
      (then (fn [sqlite3]
              (return (-/raw-init sqlite3
                                  (-> {}
                                      (xt/x:obj-assign defaults)
                                      (xt/x:obj-assign opts))))))
      (then (fn [raw]
              (xt/x:set-key client "raw" raw)
              (return client))))))

(defimpl.xt SqliteClient
  [defaults raw]
  [conn-sql/ISqlClient
   {conn-sql/connect -/client-connect
    conn-sql/disconnect (fn [client]
                          (var #{raw} client)
                          (. raw (close))
                          (return true))
    conn-sql/query (fn [client query]
                     (var #{raw} client)
                     (return (-/raw-query raw query)))
    conn-sql/query-async (fn [client query]
                           (var #{raw} client)
                           (return (protocol/ensure-promise
                                    (-/raw-query raw query))))}])

(defn.js create
  [defaults]
  (return
   (-/SqliteClient defaults nil)))
