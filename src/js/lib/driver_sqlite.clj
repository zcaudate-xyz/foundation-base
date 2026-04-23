(ns js.lib.driver-sqlite
  (:require [std.lang :as l]))

(l/script :js
  {:import [["@sqlite.org/sqlite-wasm" :as sqlite3InitModule]] :require [[xt.lang.spec-base :as xt] [js.core.util :as ut]]})

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

(defn.js set-methods
  "sets the query and disconnect methods"
  {:added "4.1"}
  [db]
  (:= (. db ["::disconnect"])
      (fn [callback]
        (:= callback (or callback ut/pass-callback))
        (. db (close))
        (return (callback nil true))))
  (:= (. db ["::query"])
      (fn [query callback]
        (:= callback (or callback ut/pass-callback))
        (return (callback nil (-/raw-query db query)))))
  (:= (. db ["::query_sync"])
      (fn [query]
        (return (-/raw-query db query))))
  (:= (. db ["run"])
      (fn [query]
        (. db (exec {:sql query}))
        (return db)))
  (return db))

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
  (return (-/set-methods conn)))

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
  (:= callback (or callback ut/pass-callback))
  (var init-module (or (. sqlite3InitModule ["default"])
                       sqlite3InitModule))
  (return (. (init-module)
             (then (fn [sqlite3]
                     (return (callback nil (-/make-instance sqlite3 m))))
                   (fn [err]
                     (return (callback err nil)))))))

(comment
  (System/getenv)
  (l/rt:module-purge :lua)
  (l/rt:module-purge :js)
  (./create-tests))
