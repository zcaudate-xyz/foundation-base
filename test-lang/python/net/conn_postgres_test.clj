(ns python.net.conn-postgres-test
  (:require [hara.lang :as l]
            [std.lib.env :as env])
  (:use code.test))

(l/script- :python
  {:runtime :basic
   :require [[python.net.conn-postgres :as pg]
             [xt.lang.spec-promise :as p]
             [xt.lang.common-data :as xtd]]})

(fact:global
 {:skip     (or (not (env/program-exists? "python3"))
                (not (env/program-exists? "psql")))
  :setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer python.net.conn-postgres/default-env :added "4.1"}
(fact "gets the default env"

  (pg/default-env)
  => (contains {"host" "127.0.0.1"
                "port" "5432"
                "user" "postgres"
                "database" "test"}))

^{:refer python.net.conn-postgres/default-env-set :added "4.1"}
(fact "sets the default env"

  (!.py (do (pg/default-env-set {:host "other"})
            (var out (pg/default-env))
            (pg/default-env-set {:host "127.0.0.1"})
            (return out)))
  => (contains {"host" "other"
                "port" "5432"
                "user" "postgres"
                "database" "test"}))

^{:refer python.net.conn-postgres/load-module :added "4.1"}
(fact "loads a postgres driver module"

  (!.py (str (pg/load-module)))
  => #"<module 'psycopg")

^{:refer python.net.conn-postgres/coerce-number-string :added "4.1"}
(fact "coerces numeric strings to numbers"

  (!.py [(pg/coerce-number-string "42")
         (pg/coerce-number-string "3.14")
         (pg/coerce-number-string "abc")
         (pg/coerce-number-string 7)])
  => [42 3.14 "abc" 7])

^{:refer python.net.conn-postgres/normalise-scalar-output :added "4.1"}
(fact "normalises scalar output values"

  (!.py [(pg/normalise-scalar-output nil)
         (pg/normalise-scalar-output true)
         (pg/normalise-scalar-output "42")
         (pg/normalise-scalar-output "abc")
         (pg/normalise-scalar-output {"a" 1})])
  => [nil true 42 "abc" {"a" 1}])

^{:refer python.net.conn-postgres/normalise-query-output :added "4.1"}
(fact "normalises query output"

  (!.py (pg/normalise-query-output []))
  => []

  (!.py (pg/normalise-query-output [["1"]]))
  => 1

  (!.py (pg/normalise-query-output [[1 "hello"]]))
  => [[1 "hello"]]

  (!.py (pg/normalise-query-output [["1"] ["2"]]))
  => [["1"] ["2"]])

^{:refer python.net.conn-postgres/create :added "4.1"}
(fact "creates a postgres client"

  (pg/create {:host "127.0.0.1"})
  => (contains {"::" "python.net.conn_postgres/PythonPostgresClient"
                "defaults" {"host" "127.0.0.1"}}))

^{:refer python.net.conn-postgres/client-connect :added "4.1"}
(fact "connects a postgres client"

  (!.py (do (var client (pg/create {}))
            (pg/client-connect client {})
            (var out (pg/client-query client "SELECT 1 AS n"))
            (pg/client-disconnect client)
            (return out)))
  => 1)

^{:refer python.net.conn-postgres/client-disconnect :added "4.1"}
(fact "disconnects a postgres client"

  (!.py (do (var client (pg/create {}))
            (pg/client-connect client {})
            (return (pg/client-disconnect client))))
  => true)

^{:refer python.net.conn-postgres/client-query :added "4.1"}
(fact "queries a postgres client"

  (!.py (do (var client (pg/create {}))
            (pg/client-connect client {})
            (var out (pg/client-query client "SELECT 1 AS n, 'hello' AS s"))
            (pg/client-disconnect client)
            (return out)))
  => [[1 "hello"]])

^{:refer python.net.conn-postgres/client-query-async :added "4.1"}
(fact "queries a postgres client asynchronously"

  (!.py (do (var client (pg/create {}))
            (pg/client-connect client {})
            (var p (pg/client-query-async client "SELECT 1"))
            (return (p/x:promise-native? p))))
  => true)

^{:refer python.net.conn-postgres/raw-query :added "4.1"}
(fact "performs a raw query on a connected postgres client"

  (!.py (do (var client (pg/create {}))
            (pg/client-connect client {})
            (var out (pg/raw-query (. client ["raw"]) "SELECT 3 AS n"))
            (pg/client-disconnect client)
            (return out)))
  => 3)
