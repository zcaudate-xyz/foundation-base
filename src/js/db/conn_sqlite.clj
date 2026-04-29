(ns js.db.conn-sqlite
  (:require [std.lang :as l]))

(l/script :js
  {:import [["@sqlite.org/sqlite-wasm" :as sqlite3InitModule]]
   :require [[xt.lang.spec-base :as xt]
             [js.core.util :as ut]]
   :implements xt.protocol.conn-database})

(defn.js raw-query
  "normalises sqlite result rows"
  {:added "4.1"}
  [conn input]
  (var columns [])
  (var values (. conn (exec {:sql         input
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

(defn.js connect
  "opens a sqlite wasm database connection"
  {:added "4.1"}
  [opts callback]
  (:= callback (or callback ut/pass-callback))
  (var config (or opts {}))
  (var filename (or (xt/x:get-key config "filename")
                    ":memory:"))
  (var flags (or (xt/x:get-key config "flags")
                 "c"))
  (var init-module (or (. sqlite3InitModule ["default"])
                       sqlite3InitModule))
  (return (. (init-module)
             (then (fn [sqlite3]
                     (var conn (new (. sqlite3 ["oo1"] ["DB"]) filename flags))
                     (return (callback nil conn)))
                   (fn [err]
                     (return (callback err nil)))))))

(defn.js disconnect
  "closes a sqlite database connection"
  {:added "4.1"}
  [conn callback]
  (:= callback (or callback ut/pass-callback))
  (. conn (close))
  (return (callback nil true)))

(defn.js query
  "runs an asynchronous sqlite query"
  {:added "4.1"}
  [conn input callback]
  (:= callback (or callback ut/pass-callback))
  (return (callback nil (-/raw-query conn input))))

(defn.js query-sync
  "runs a synchronous sqlite query"
  {:added "4.1"}
  [conn input]
  (return (-/raw-query conn input)))
