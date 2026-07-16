(ns dart.net.conn-postgres
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :refer [defimpl.xt]]))

(l/script :dart
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]
             [xt.net.conn-sql :as conn-sql]]
   :import [["package:postgres/postgres.dart" :as pg]]})

(defn.dt default-env
  []
  (return {:host     "127.0.0.1"
           :port     5432
           :user     "postgres"
           :password "postgres"
           :database "test"}))

(defn.dt normalise-scalar-output
  [value]
  (if (xt/x:is-string? value)
    (do (var parsed (:- "num.tryParse(" value ")"))
        (return (or parsed value))))
  (return value))

(defn.dt normalise-query-output
  [rows]
  (cond (== 0 (xt/x:len rows))
        (return [])

        (and (== 1 (xt/x:len rows))
             (== 1 (xt/x:len (. rows [0]))))
        (return (-/normalise-scalar-output (. rows [0] [0])))

        :else
        (return
         (xtd/arr-map rows
                       (fn [row]
                         (return (. row (toColumnMap))))))))

(defn.dt client-connect
  [client opts]
  (var #{defaults} client)
  (var env (xtd/obj-clone (-/default-env)))
  (xtd/obj-assign env defaults)
  (xtd/obj-assign env (or opts {}))
  (var endpoint (pg.Endpoint.new
                 :host (xt/x:get-key env "host")
                 :port (xt/x:get-key env "port")
                 :database (xt/x:get-key env "database")
                 :username (xt/x:get-key env "user")
                 :password (xt/x:get-key env "password")))
  (var settings (:- "pg.ConnectionSettings(connectTimeout: const Duration(seconds: 5), sslMode: pg.SslMode.disable)"))
  (return
   (promise/x:promise-then
    (pg.Connection.open endpoint :settings settings)
    (fn [session]
      (xt/x:set-key client "raw" session)
      (return client)))))

(defn.dt client-disconnect
  [client]
  (var #{raw} client)
  (. raw (close))
  (return true))

(defn.dt client-query
  [client query]
  (var #{raw} client)
  (return
   (promise/x:promise-then
    (. raw (execute query))
    (fn [rows]
      (return (-/normalise-query-output rows))))))

(defn.dt client-query-async
  [client query]
  (return (-/client-query client query)))

(defimpl.xt ^{:lang :dart}
  DartPostgresClient
  [defaults raw]
  conn-sql/ISqlClient
  {conn-sql/connect      -/client-connect
   conn-sql/disconnect   -/client-disconnect
   conn-sql/query        -/client-query
   conn-sql/query-async  -/client-query-async})

(defn.dt create
  [defaults]
  (return
   (-/DartPostgresClient (or defaults {}) nil)))
