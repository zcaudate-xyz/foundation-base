(ns dart.net.conn-postgres
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :refer [defimpl.xt]]))

(l/script :dart
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-protocol :as protocol]
             [xt.net.conn-sql :as conn-sql]]
   :import [["package:postgres/postgres.dart" :as pg]]})

(defn.dt default-env
  []
  (return {:host     "127.0.0.1"
           :port     5432
           :user     "postgres"
           :password "postgres"
           :database "test"}))

(defn.dt client-connect
  [client opts]
  (var #{defaults} client)
  (var env (xtd/obj-assign defaults opts))
  (var session (pg.Session.new
                 :host (xt/x:get-key env "host")
                 :port (xt/x:get-key env "port")
                 :database (xt/x:get-key env "database")
                 :username (xt/x:get-key env "user")
                 :password (xt/x:get-key env "password")))
  (xt/x:set-key client "raw" session)
  (return client))

(defn.dt client-disconnect
  [client]
  (var #{raw} client)
  (. raw (close))
  (return true))

(defn.dt client-query
  [client query]
  (var #{raw} client)
  (var result (. raw (execute query)))
  (return result))

(defn.dt client-query-async
  [client query]
  (return (protocol/ensure-promise (-/client-query client query))))

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
