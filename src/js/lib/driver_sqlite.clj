(ns js.lib.driver-sqlite
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.core.util :as ut]]})

(defn.js raw-query
  "raw query for sql lite results"
  {:added "4.0"}
  [db query]
  (var out (. db (exec query)))
  (when (== 1 (k/len out))
    (var values (. out [0] ["values"]))
    (when (and (not= nil values)
               (== 1 (k/len values)))
      (return (. values [0] [0]))))
  (return out))

(defn.js set-methods
  "sets the query and disconnect methods"
  {:added "4.0"}
  [db]
  (:= (. db ["::disconnect"])
      (fn [callback]
        (:= callback (or callback ut/pass-callback))
        (return (callback nil (. db (close))))))
  (:= (. db ["::query"])
      (fn [query callback]
        (:= callback (or callback ut/pass-callback))
        (return (callback nil (-/raw-query db query)))))
  (:= (. db ["::query_sync"])
      (fn [query]
        (return (-/raw-query db query))))
  (return db))

(defn.js make-instance
  "creates a instance once SQL is loaded"
  {:added "4.0"}
  [SQL]
  (var conn (new SQL.Database))
  (return (-/set-methods conn)))

(defn.js  ^{:static/override true}
  connect-constructor
  "connects to an embeded sqlite file
 
   (notify/wait-on :js
     (:= (!:G initSqlJs) (require \"sql.js\"))
     (dbsql/connect {:constructor js-sqlite/connect-constructor}
                    {:success (fn [conn]
                                (dbsql/query conn \"SELECT 1;\"
                                             (repl/<!)))}))
   => 1"
  {:added "4.0"}
  [m callback]
  (:= callback (or callback ut/pass-callback))
  (return (. ((!:G initSqlJs))
             (then (fn [SQL]
                     (var conn (new SQL.Database))
                     (return (callback nil (-/set-methods conn))))))))

(comment
  (System/getenv)
  (l/rt:module-purge :lua)
  (l/rt:module-purge :js)
  (./create-tests))
