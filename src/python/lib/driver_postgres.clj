(ns python.lib.driver-postgres
  (:require [std.lang :as l]))

(l/script :python
  {:require [[xt.lang.common-data :as xtd]
             [xt.lang.common-space :as rt]
             [xt.lang.spec-base :as xt]
             [python.core.system :as pysys]
             [xt.lib.sql-connection :as sqlrt]]})

(defn.py default-env
  "Gets the default postgres env."
  {:added "4.1"}
  []
  (return (or (rt/xt-config "python.lib.driver-postgres")
              {:host     "127.0.0.1"
               :port     "5432"
               :user     "postgres"
               :password "postgres"
               :database "test"})))

(defn.py default-env-set
  "Sets the default postgres env."
  {:added "4.1"}
  [m]
  (var env (xtd/obj-assign (xtd/obj-clone (-/default-env)) m))
  (rt/xt-config-set "python.lib.driver-postgres" env)
  (return env))

(defn.py load-module
  "Loads psycopg or psycopg2."
  {:added "4.1"}
  []
  (try
    (return (pysys/__import__ "psycopg"))
    (catch e
      (return (pysys/__import__ "psycopg2")))))

(defn.py normalise-query-output
  [rows]
  (cond (== 0 (xt/x:len rows))
        (return [])

        (and (== 1 (xt/x:len rows))
             (== 1 (xt/x:len (. rows [0]))))
        (return (. rows [0] [0]))

        :else
        (return rows)))

(defn.py raw-query
  "Runs a raw postgres query and normalises the result shape."
  {:added "4.1"}
  [conn query]
  (var cursor (. conn (cursor)))
  (. cursor (execute query))
  (var rows (:? (. cursor ["description"])
                (. cursor (fetchall))
                []))
  (. conn (commit))
  (. cursor (close))
  (return (-/normalise-query-output rows)))

(defn.py wrap-connection
  [conn]
  (return
   (sqlrt/connection-create
    conn
    {"disconnect" (fn [raw]
                    (. raw (close))
                    (return true))
     "query" (fn [raw query]
               (return (-/raw-query raw query)))
     "query_sync" (fn [raw query]
                    (xt/x:err "Not Allowed"))})))

(defn.py connect-constructor
  "Constructs a postgres connection through psycopg."
  {:added "4.1"}
  [m]
  (var env (xtd/obj-assign (-/default-env) (or m {})))
  (var dsn (xt/x:cat "dbname=" (xt/x:get-key env "database")
                     " user=" (xt/x:get-key env "user")
                     " password=" (xt/x:get-key env "password")
                     " host=" (xt/x:get-key env "host")
                     " port=" (xt/x:get-key env "port")))
  (var pg (-/load-module))
  (var conn (. pg (connect dsn)))
  (:= (. conn ["autocommit"]) true)
  (return conn))

(defn.py driver
  []
  (return
   (sqlrt/driver-create
    {"connect" (fn [m]
                 (return
                  (-/wrap-connection
                   (-/connect-constructor m))))})))
