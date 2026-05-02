(ns js.lib.driver-postgres
  (:require [std.lang :as l]
             [std.lib.foundation :as f])
  (:refer-clojure :exclude [print send]))

(l/script :js
  {:require [[xt.lang.common-lib :as k]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-resource :as rt]
             [js.core.util :as ut]
             [xt.lang.spec-base :as xt]
             [xt.lib.connection-sql :as sqlrt]]
   :import [["pg" :as [* Postgres]]]})

(defn.js default-env
  "gets the default env"
  {:added "4.0"}
  []
  (return (or (rt/xt-config "js.lib.driver-postgres")
              {:host     "127.0.0.1"
               :port     "5432"
               :user     "postgres"
               :password "postgres"
               :database "test"})))

(defn.js default-env-set
  "sets the default env"
  {:added "4.0"}
  [m]
  (var env (xtd/obj-assign (xtd/obj-clone (-/default-env)) m))
  (rt/xt-config-set "js.lib.driver-postgres" env)
  (return env))


(def$.js Client Postgres.Client)

(defn.js normalise-query-output
  [res]
  (var #{rows} res)
  (if (and (== 1 rows.length)
           (== 1 (xt/x:len (xtd/obj-keys (xtd/first rows)))))
    (return (xtd/obj-first-val (xtd/first rows)))
    (return rows)))

(defn.js wrap-connection
  [conn]
  (return
   (sqlrt/connection-create
    conn
    {"disconnect" (fn [raw]
                    (return (. raw (end))))
     "query"      (fn [raw input]
                    (return
                     (. (. raw (query input))
                        (then -/normalise-query-output))))
     "query_sync" (fn [raw input]
                    (throw "Not Allowed"))})))

(defn.js connect-constructor
  "constructs the postgres instance"
  {:added "4.0"}
  [m callback]
  (var env (xtd/obj-assign (-/default-env)
                          m))
  (var conn (new -/Client env))
  (var promise
       (. (. conn (connect))
          (then (fn []
                  (return conn)))))
  (if callback
    (return (ut/wrap-callback promise callback))
    (return promise)))

(defn.js driver
  []
  (return
   (sqlrt/driver-create
    {"connect"
     (fn [m]
       (return
        (. (-/connect-constructor m)
           (then -/wrap-connection))))})))
