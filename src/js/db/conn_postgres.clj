(ns js.db.conn-postgres
  (:require [std.lang :as l]
            [std.lib.foundation :as f])
  (:refer-clojure :exclude [print send]))

(l/script :js
  {:import [["pg" :as [* Postgres]]]
   :require [[xt.lang.common-data :as xtd]
             [js.core.util :as ut]
             [xt.lang.spec-base :as xt]]
   :implements xt.protocol.conn-database})

(f/template-entries [l/tmpl-entry {:type :fragment
                                   :base "Postgres"
                                   :tag "js"}]
  [Client])

(defn.js connect
  "opens a postgres database connection"
  {:added "4.1"}
  [opts callback]
  (:= callback (or callback ut/pass-callback))
  (var env (xtd/obj-assign {:host     "127.0.0.1"
                            :port     "5432"
                            :user     "postgres"
                            :password "postgres"
                            :database "test"}
                           opts))
  (var conn (new -/Client env))
  (. conn
     (connect)
     (then (fn []
             (return (callback nil conn)))
           (fn [err]
             (return (callback err nil)))))
  (return conn))

(defn.js disconnect
  "closes a postgres database connection"
  {:added "4.1"}
  [conn callback]
  (:= callback (or callback ut/pass-callback))
  (return (ut/wrap-callback (. conn (end))
                            callback)))

(defn.js query
  "runs an asynchronous postgres query"
  {:added "4.1"}
  [conn input callback]
  (:= callback (or callback ut/pass-callback))
  (return
   (ut/wrap-callback
    (. conn (query input))
    (fn [err res]
      (when err
        (return (callback err nil)))
      (var #{rows} res)
      (if (and (== 1 rows.length)
               (== 1 (xt/x:len (xtd/obj-keys (xtd/first rows)))))
        (return (callback nil
                          (xtd/obj-first-val
                           (xtd/first rows))))
        (return (callback nil rows)))))))

(defn.js query-sync
  "postgres does not expose a synchronous query path"
  {:added "4.1"}
  [conn input]
  (throw "Not Allowed"))
