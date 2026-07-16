(ns python.net.conn-postgres
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :refer [defimpl.xt]]))

(l/script :python
  {:require [[xt.lang.common-data :as xtd]
             [xt.lang.common-resource :as rt]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [python.core :as py]
             [xt.net.conn-sql :as conn-sql]]})

(defn.py default-env
  "Gets the default postgres env."
  {:added "4.1"}
  []
  (return (or (rt/xt-config "python.net.conn-postgres")
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
  (rt/xt-config-set "python.net.conn-postgres" env)
  (return env))

(defn.py load-module
  "Loads psycopg or psycopg2."
  {:added "4.1"}
  []
  (try
    (return (py/__import__ "psycopg"))
    (catch e
      (return (py/__import__ "psycopg2")))))

(defn.py coerce-number-string
  [value]
  (if (not (xt/x:is-string? value))
    (return value))
  (var trimmed (. value (strip)))
  (if (== trimmed "")
    (return value))
  (try
    (return (int trimmed))
    (catch e
      (try
        (return (float trimmed))
        (catch e
          (return value))))))

(defn.py normalise-scalar-output
  [value]
  (cond (or (xt/x:nil? value)
            (xt/x:is-boolean? value)
            (xt/x:is-array? value))
        (return value)

        (xt/x:is-string? value)
        (return (-/coerce-number-string value))

        (py/hasattr value "as_tuple")
        (return (-/coerce-number-string
                 (xt/x:to-string value)))

        (xt/x:is-object? value)
        (return value)

        :else
        (return value)))

(defn.py normalise-query-output
  [rows]
  (cond (== 0 (xt/x:len rows))
        (return [])

        (and (== 1 (xt/x:len rows))
             (== 1 (xt/x:len (. rows [0]))))
        (return (-/normalise-scalar-output
                 (. rows [0] [0])))

        :else
        (return rows)))

(defn.py raw-query
  "Runs a raw postgres query and normalises the result shape."
  {:added "4.1"}
  [conn query]
  (var cursor (. conn (cursor)))
  (. cursor (execute query))
  (var rows (:? (. cursor description)
                (. cursor (fetchall))
                []))
  (. conn (commit))
  (. cursor (close))
  (return (-/normalise-query-output rows)))

(defn.py client-connect
  [client opts]
  (var #{defaults} client)
  (var env (xtd/obj-clone (-/default-env)))
  (xtd/obj-assign env defaults)
  (xtd/obj-assign env (or opts {}))
  (var dsn (xt/x:cat "dbname=" (xt/x:get-key env "database")
                     " user=" (xt/x:get-key env "user")
                     " password=" (xt/x:get-key env "password")
                     " host=" (xt/x:get-key env "host")
                     " port=" (xt/x:to-string (xt/x:get-key env "port"))))
  (var pg (-/load-module))
  (var raw (. pg (connect dsn)))
  (:= (. raw autocommit) true)
  (xt/x:set-key client "raw" raw)
  (return client))

(defn.py client-disconnect
  [client]
  (var #{raw} client)
  (. raw (close))
  (return true))

(defn.py client-query
  [client query]
  (var #{raw} client)
  (return (-/raw-query raw query)))

(defn.py client-query-async
  [client query]
  (return (promise/x:promise-run (-/client-query client query))))

(defimpl.xt ^{:lang :python}
  PythonPostgresClient
  [defaults raw]
  conn-sql/ISqlClient
  {conn-sql/connect      -/client-connect
   conn-sql/disconnect   -/client-disconnect
   conn-sql/query        -/client-query
   conn-sql/query-async  -/client-query-async})

(defn.py create
  [defaults]
  (return
   (-/PythonPostgresClient (or defaults {}) nil)))
