(ns js.lib.driver-sqlite-wasm
  (:require [std.lang :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [js.core.util :as ut]
             [xt.lib.connection-sql :as sqlrt]]
   :import [["@sqlite.org/sqlite-wasm" :as sqlite3InitModule]]})

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
    (return (. values [0] [0])))
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

(defn.js ^{:static/override true}
  connect-constructor
  "connects to an embedded sqlite-wasm database"
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
