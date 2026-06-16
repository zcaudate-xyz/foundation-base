(ns lua.nginx.conn-sqlite
  (:require [hara.lang :as l]
            [std.lib.foundation :as f]))

(l/script :lua.nginx
  {:import [["lsqlite3" :as ngxsqlite]]
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-protocol :as protocol]
             [xt.net.conn-sql :as conn-sql]]})

(f/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ngxsqlite"
                                   :tag "lua"}]
  [lversion
   version
   temp_directory
   complete
   open
   open-memory
   open-ptr])

(defn.lua coerce-number
  "Performs a raw query"
  {:added "4.0"}
  [n]
  (return (or (tonumber n)
              n)))

(defn.lua decode-json-scalar
  [value]
  (cond (and (xt/x:is-string? value)
             (or (. value (find "^%["))
                 (. value (find "^%{"))
                 (== value "true")
                 (== value "false")
                 (== value "null")))
        (return (xt/x:json-decode value))

        :else
        (return value)))

(defn.lua query-returns-rows?
  "Checks whether a query should return row data."
  {:added "4.1"}
  [query]
  (var sql (. (. query (gsub "^%s+" ""))
              (lower)))
  (return (or (. sql (find "^select"))
              (. sql (find "^pragma"))
              (. sql (find "^with"))
              (. sql (find "^values"))
              (. sql (find "^explain")))))

(defn.lua raw-exec
  "performs a raw execution"
  {:added "4.0"}
  [db query]
  (var out [])
  (. db (exec query
              (fn [udata cols values names]
                (var entry {})
                (xt/for:array [[i k] names]
                  (:= (. entry [k])
                      (. values [i])))
                (xt/x:arr-push out entry)
                (return 0))))
  (return out))

(defn.lua raw-query
  "Performs a raw query"
  {:added "4.0"}
  [db query]
  (when (not (-/query-returns-rows? query))
    (-/raw-exec db query)
    (return []))
  (var out [])
  (. db (exec query
              (fn [udata cols values names]
                (cond (== cols 1)
                      (table.insert out
                                    (-/decode-json-scalar
                                     (-/coerce-number
                                      (xtd/first values))))
                        
                       :else
                       (table.insert out (xtd/arr-map values -/coerce-number)))
                (return 0))))
  (if (< 2 (len out))
    (return out)
   (return (xtd/first out))))

(defn.lua normalize-connect-opts
  "defaults to an in-memory database when no sqlite target is provided"
  {:added "4.1"}
  [m]
  (var out (xtd/obj-assign {} m))
  (when (and (not (xt/x:has-key? out "memory"))
             (not (xt/x:has-key? out "filename")))
    (xt/x:set-key out "memory" true))
  (return out))

(defn.lua client-connect
  [client opts]
  (var #{defaults} client)
  (var env (-/normalize-connect-opts (xtd/obj-assign defaults opts)))
  (var #{filename memory} env)
  (var instance (:? (or memory
                         (xt/x:nil? filename))
                     (-/open-memory)
                     (-/open filename)))
  (xt/x:set-key client "raw" instance)
  (return client))

(defn.lua client-disconnect
  [client]
  (var #{raw} client)
  (return (. raw (close))))

(defn.lua client-query
  [client input]
  (var #{raw} client)
  (return (-/raw-query raw input)))

(defn.lua client-query-async
  [client input]
  (return (protocol/ensure-promise (-/client-query client input))))

(defimpl.xt ^{:lang :lua}
  LuaNginxSqliteClient
  [defaults raw]
  conn-sql/ISqlClient
  {conn-sql/connect      -/client-connect
   conn-sql/disconnect   -/client-disconnect
   conn-sql/query        -/client-query
   conn-sql/query-async  -/client-query-async})

(defn.lua create
  [defaults]
  (return
   (-/LuaNginxSqliteClient (or defaults {}) nil)))
