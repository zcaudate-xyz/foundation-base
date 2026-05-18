(ns js.lib.driver-sqlite
  (:require [hara.lang :as l]))

(l/script :js
  {:import [["@sqlite.org/sqlite-wasm" :as sqlite3InitModule]]
   :require [[xt.lang.spec-base :as xt]
             [js.core.util :as ut]
             [xt.protocol.impl.connection-sql :as sqlrt]]})

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

(defn.js wrap-connection
  [db]
  (return
   (sqlrt/connection-create
    db
    {"disconnect" (fn [raw]
                    (. raw (close))
                    (return true))
     "query" (fn [raw query]
               (return (-/raw-query raw query)))
     "query_sync" (fn [raw query]
                    (return (-/raw-query raw query)))})))

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
  connect-constructor
  "connects to an embedded sqlite-wasm database

   (notify/wait-on :js
      (dbsql/connect {:constructor js-sqlite/connect-constructor}
                     {:success (fn [conn]
                                 (dbsql/query conn \"SELECT 1;\"
                                              (repl/<!)))}))
    => 1"
  {:added "4.1"}
  [m callback]
  (var init-module (or (. sqlite3InitModule ["default"])
                       sqlite3InitModule))
  (var promise
       (. (init-module)
          (then (fn [sqlite3]
                  (return (-/make-instance sqlite3 m))))))
  (if callback
    (return (ut/wrap-callback promise callback))
    (return promise)))

(defn.js driver
  []
  (return
   (sqlrt/driver-create
    {"connect" (fn [m]
                 (return
                  (. (-/connect-constructor m)
                     (then -/wrap-connection))))})))

(comment
  (System/getenv)
  (l/rt:module-purge :lua)
  (l/rt:module-purge :js)
  (./create-tests))
