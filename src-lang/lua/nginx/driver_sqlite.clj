(ns lua.nginx.driver-sqlite
  (:require [hara.lang :as l]
             [std.lib.foundation :as f]))

(l/script :lua.nginx
  {:import [["lsqlite3" :as ngxsqlite]]
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.protocol.impl.connection-sql :as sqlrt]]})

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

(defn.lua connect-constructor
  "create db connection"
  {:added "4.0"}
  [m]
  (local #{filename memory} m)
  (local instance (:? (or memory
                          (xt/x:nil? filename))
                      (-/open-memory)
                      (-/open filename)))
  (local conn {:raw instance})
  (:= (. conn ["::disconnect"])
      (fn []
        (return (. conn (close)))))
  (:= (. conn ["::query"])
      (fn [query]
        (return (-/raw-query instance query))))
  (:= (. conn ["::query_sync"])
      (fn [query]
        (return (-/raw-query instance query))))
  (return conn))

(defn.lua wrap-connection
  [conn]
  (return
   (sqlrt/connection-create
    conn
    {"disconnect" (fn [raw]
                    (var disconnect-fn (xt/x:get-key raw "::disconnect"))
                    (return (disconnect-fn)))
     "query" (fn [raw query]
               (var query-fn (xt/x:get-key raw "::query"))
               (return (query-fn query)))
     "query_sync" (fn [raw query]
                    (var query-sync-fn (xt/x:get-key raw "::query_sync"))
                    (return (query-sync-fn query)))})))

(defn.lua driver
  []
  (return
   (sqlrt/driver-create
     {"connect" (fn [m]
                  (return (-/wrap-connection
                           (-/connect-constructor
                            (-/normalize-connect-opts m)))))})))
