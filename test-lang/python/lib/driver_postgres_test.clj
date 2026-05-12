(ns python.lib.driver-postgres-test
  (:require [hara.lang :as l]
            [python.lib.driver-postgres :refer :all])
  (:use code.test))

(l/script- :postgres
  {:runtime :jdbc.client
   :config {:dbname "test-scratch"}
   :require [[postgres.sample.scratch-v1 :as scratch]]})

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
 {:setup    [(l/rt:restart)
             (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

(def +scratch-env+
  {"host"     "127.0.0.1"
   "port"     "5432"
   "user"     "postgres"
   "password" "postgres"
   "database" "test-scratch"})

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
(fact "loads a postgres client module"

  (!.py
    (py/hasattr (py-pg/load-module) "connect"))
  => true)

^{:refer python.lib.driver-postgres/normalise-query-output :added "4.1"}
(fact "normalises postgres row output"

  (!.py
    [(py-pg/normalise-query-output [])
     (py-pg/normalise-query-output [[1]])
     (py-pg/normalise-query-output [[1 "a"] [2 "b"]])])
  => [[] "1" [[1 "a"] [2 "b"]]])

^{:refer python.lib.driver-postgres/raw-query :added "4.1"}
(fact "runs raw postgres queries against the scratch sample app"

  (!.py
    (var conn (py-pg/connect-constructor (@! +scratch-env+)))
    (py-pg/raw-query conn "SELECT (\"scratch\".addf(1,2))::int;"))
  => "3")

^{:refer python.lib.driver-postgres/wrap-connection :added "4.1"}
(fact "wraps real postgres scratch connections with promise query and sync query-sync support"

  (!.py
    (var conn (py-pg/wrap-connection
               (py-pg/connect-constructor (@! +scratch-env+))))
    [(sql/connection? conn)
     (spec-promise/x:promise-native? (sql/query conn "SELECT \"scratch\".ping();"))
     (sql/query-sync conn "SELECT (\"scratch\".addf(1,2))::int;")
     (sql/disconnect conn)])
   => [true true "3" true])

^{:refer python.lib.driver-postgres/connect-constructor :added "4.1"}
(fact "constructs a real scratch postgres connection"

  (!.py
    (var conn (py-pg/connect-constructor (@! +scratch-env+)))
    [(. conn autocommit)
     (py-pg/raw-query conn "SELECT \"scratch\".ping();")])
  => [true "pong"])

^{:refer python.lib.driver-postgres/driver :added "4.1"}
(fact "connects through the real driver wrapper with async query and sync query-sync support"

  (notify/wait-on [:python 5000]
    (spec-promise/x:promise-then
     (sql/connect (py-pg/driver)
                  (@! +scratch-env+))
     (fn [conn]
       (spec-promise/x:promise-then
        (sql/query conn "SELECT \"scratch\".ping();")
        (fn [out]
          (repl/notify
           [(sql/connection? conn)
            out
            (sql/query-sync conn "SELECT (\"scratch\".addf(10,20))::int;")
            (sql/disconnect conn)]))))))
  => [true "pong" "30" true])
