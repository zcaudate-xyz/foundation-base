(ns lua.nginx.driver-sqlite
  (:require [std.lang :as l]
            [std.lib :as h]
            [xt.sys.conn-dbsql :as dbsql]))

(l/script :lua
  {:require [[xt.lang.base-lib :as k]]
   :import [["lsqlite3" :as ngxsqlite]]})

(h/template-entries [l/tmpl-entry {:type :fragment
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
                (k/for:array [[i k] names]
                  (:= (. entry [k])
                      (. values [i])))
                (x:arr-push out entry)
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
                      (table.insert out (-/coerce-number (k/first values)))
                      
                      :else
                      (table.insert out (k/arr-map values -/coerce-number)))
                (return 0))))
  (if (< 2 (len out))
    (return out)
    (return (k/first out))))

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

