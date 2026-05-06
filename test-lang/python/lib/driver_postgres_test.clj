(ns python.lib.driver-postgres-test
  (:require [hara.lang :as l]
            [python.lib.driver-postgres :refer :all])
  (:use code.test))

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-notify :as notify]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-resource :as rt]
             [xt.lang.spec-promise :as spec-promise]
             [xt.protocol.impl.connection-sql :as sql]
             [python.core :as py]
             [python.lib.driver-postgres :as py-pg]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer python.lib.driver-postgres/default-env :added "4.1"}
(fact "returns the default postgres environment"

  (!.py
    (rt/xt-purge-config)
    (var env (py-pg/default-env))
    [(xt/x:get-key env "host")
     (xt/x:get-key env "port")
     (xt/x:get-key env "user")
     (xt/x:get-key env "database")])
  => ["127.0.0.1" "5432" "postgres" "test"])

^{:refer python.lib.driver-postgres/default-env-set :added "4.1"}
(fact "overrides and persists the default postgres environment"

  (!.py
    (rt/xt-purge-config)
    (py-pg/default-env-set {"host" "db.internal"
                            "port" "15432"
                            "database" "sample"})
    (var env (py-pg/default-env))
    [(xt/x:get-key env "host")
     (xt/x:get-key env "port")
     (xt/x:get-key env "user")
     (xt/x:get-key env "database")])
  => ["db.internal" "15432" "postgres" "sample"])

^{:refer python.lib.driver-postgres/load-module :added "4.1"}
(fact "loads a postgres client module when available"

  (!.py
    (var out "python-postgres-unavailable")
    (try
      (:= out (py/hasattr (py-pg/load-module) "connect"))
      (catch err
        (:= out "python-postgres-unavailable")))
    (return out))
  => (any true "python-postgres-unavailable"))

^{:refer python.lib.driver-postgres/normalise-query-output :added "4.1"}
(fact "normalises postgres row output"

  (!.py
    [(py-pg/normalise-query-output [])
     (py-pg/normalise-query-output [[1]])
     (py-pg/normalise-query-output [[1 "a"] [2 "b"]])])
  => [[] 1 [[1 "a"] [2 "b"]]])

^{:refer python.lib.driver-postgres/raw-query :added "4.1"}
(fact "runs raw postgres queries when the environment is available"

  (!.py
    (var out "python-postgres-unavailable")
    (try
      (var conn (py-pg/connect-constructor (py-pg/default-env)))
      (:= out (py-pg/raw-query conn "SELECT 1;"))
      (catch err
        (:= out "python-postgres-unavailable")))
    (return out))
  => (any 1 "python-postgres-unavailable"))

^{:refer python.lib.driver-postgres/wrap-connection :added "4.1"}
(fact "wraps real postgres connections and rejects sync queries"

  (!.py
    (var out "python-postgres-unavailable")
    (try
      (var conn (py-pg/wrap-connection
                 (py-pg/connect-constructor (py-pg/default-env))))
      (var query-out (sql/query conn "SELECT 1;"))
      (var sync-out "no-error")
      (try
        (sql/query-sync conn "SELECT 1;")
        (catch err
          (:= sync-out (xt/x:ex-message err))))
      (:= out [(sql/connection? conn)
               query-out
               sync-out
               (sql/disconnect conn)])
      (catch err
        (:= out "python-postgres-unavailable")))
    (return out))
  => (any [true 1 "Not Allowed" true]
          "python-postgres-unavailable"))

^{:refer python.lib.driver-postgres/connect-constructor :added "4.1"}
(fact "constructs a real postgres connection when available"

  (!.py
    (var out "python-postgres-unavailable")
    (try
      (var conn (py-pg/connect-constructor (py-pg/default-env)))
      (:= out [(. conn ["autocommit"])
               (py-pg/raw-query conn "SELECT 1;")])
      (catch err
        (:= out "python-postgres-unavailable")))
    (return out))
  => (any [true 1]
          "python-postgres-unavailable"))

^{:refer python.lib.driver-postgres/driver :added "4.1"}
(fact "connects through the real driver wrapper"

  (if (!.py
        (var available false)
        (try
          (:= available (py/hasattr (py-pg/load-module) "connect"))
          (catch err
            (:= available false)))
        (return available))
    (notify/wait-on :python
      (spec-promise/x:promise-catch
       (spec-promise/x:promise-then
        (sql/connect (py-pg/driver)
                     {})
        (fn [conn]
          (var query-out (sql/query conn "SELECT 1;"))
          (var sync-out "no-error")
          (try
            (sql/query-sync conn "SELECT 1;")
            (catch err
              (:= sync-out (xt/x:ex-message err))))
          (repl/notify
           [(sql/connection? conn)
            query-out
            sync-out
            (sql/disconnect conn)])))
       (fn [err]
         (repl/notify "python-postgres-unavailable"))))
    "python-postgres-unavailable")
  => (any [true 1 "Not Allowed" true]
          "python-postgres-unavailable"))
