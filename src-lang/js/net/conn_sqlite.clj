(ns js.net.conn-sqlite
  (:require [hara.lang :as l]))

(l/script :js
  {:import [["@sqlite.org/sqlite-wasm" :as sqlite3InitModule]]
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-protocol :as protocol]]})

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

(defn.js make-instance
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

(defn.js  ^{:static/override true}
  create-db
  "connects to an embedded sqlite-wasm database

   (notify/wait-on :js
      (dbsql/connect {:constructor js-sqlite/connect-constructor}
                     {:success (fn [conn]
                                 (dbsql/query conn \"SELECT 1;\"
                                              (repl/<!)))}))
    => 1"
  {:added "4.1"}
  [opts]
  (var init-module (or (. sqlite3InitModule ["default"])
                       sqlite3InitModule))
  (return
   (. (init-module)
      (then (fn [sqlite3]
              (return (-/make-instance sqlite3 m)))))))

(defn.js client-methods
  []
  (return
   {"connect"     (fn [client opts]
                    (return
                     (. (-/create-db)
                        (then (fn [raw]
                                (xt/x:set-key client "raw" raw)
                                (return client))))))
    "disconnect"  (fn [client]
                    (var #{raw} client)
                    (. raw (close))
                    (return true))
    "query"       (fn [client query]
                    (var #{raw} client)
                    (return (-/raw-query raw query)))
    "query_async" (fn [client query]
                    (var #{raw} client)
                    (return (protocol/ensure-promise
                             (-/raw-query raw query))))}))


