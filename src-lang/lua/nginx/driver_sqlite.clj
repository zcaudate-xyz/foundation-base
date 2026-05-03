(ns lua.nginx.driver-sqlite
  (:require [hara.lang :as l]
             [std.lib.foundation :as f]))

(l/script :lua.nginx
  {:import [["lsqlite3" :as ngxsqlite]] :require [[xt.lang.spec-base :as xt] [xt.lang.common-data :as xtd] [xt.lib.connection-sql :as sqlrt]]})

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
  (var out [])
  (. db (exec query
              (fn [udata cols values names]
                (cond (== cols 1)
                      (table.insert out (-/coerce-number (xtd/first values)))
                       
                       :else
                       (table.insert out (xtd/arr-map values -/coerce-number)))
                (return 0))))
  (if (< 2 (len out))
    (return out)
    (return (xtd/first out))))

(defn.lua connect-constructor
  "create db connection"
  {:added "4.0"}
  [m]
  (local #{filename memory} m)
  (local instance (:? memory
                      (-/open-memory)
                      (-/open (or filename
                                  "sqlite.db"))))
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
                          (-/connect-constructor m))))})))
