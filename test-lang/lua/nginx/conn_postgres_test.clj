(ns lua.nginx.conn-postgres-test
  (:require [std.lib.env :as env]
            [hara.lang :as l])
  (:use code.test))

(l/script- :lua.nginx
  {:runtime :basic
   :test-mode true
   :config  {:program :resty}
   :require [[lua.nginx.conn-postgres :as pg]
             [lua.nginx.common-promise :as p]
             [xt.lang.common-data :as xtd]]})

(fact:global
 {:skip     (or (not (env/program-exists? "resty"))
                (not (env/program-exists? "psql")))
  :setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer lua.nginx.conn-postgres/default-env :added "4.1"}
(fact "gets the default env"

  (pg/default-env)
  => (contains {"host" "127.0.0.1"
                 "port" "5432"
                 "user" "postgres"
                 "database" "test"}))

^{:refer lua.nginx.conn-postgres/default-env-set :added "4.1"}
(fact "sets the default env"

  (!.lua (pg/default-env-set {:host "other"})
         (var out (pg/default-env))
         (pg/default-env-set {:host "127.0.0.1"})
         out)
  => (contains {"host" "other"
                 "port" "5432"}))

^{:refer lua.nginx.conn-postgres/coerce-number-string :added "4.1"}
(fact "coerces numeric strings to numbers"

  (!.lua [(pg/coerce-number-string "42")
          (pg/coerce-number-string "3.14")
          (pg/coerce-number-string "abc")
          (pg/coerce-number-string 7)])
  => [42 3.14 "abc" 7])

^{:refer lua.nginx.conn-postgres/normalise-scalar-output :added "4.1"}
(fact "normalises scalar output values"

  (!.lua [(pg/normalise-scalar-output nil)
          (pg/normalise-scalar-output true)
          (pg/normalise-scalar-output "42")
          (pg/normalise-scalar-output "abc")
          (pg/normalise-scalar-output {"a" 1})])
  => [nil true 42 "abc" {"a" 1}])

^{:refer lua.nginx.conn-postgres/normalise-query-output :added "4.1"}
(fact "normalises query output"

  (!.lua (pg/normalise-query-output []))
  => []

  (!.lua (pg/normalise-query-output [{"a" "1"}]))
  => 1

  (!.lua (pg/normalise-query-output [{"a" "1" "b" "2"}]))
  => [{"a" "1" "b" "2"}]

  (!.lua (pg/normalise-query-output [{"a" "1"} {"a" "2"}]))
  => [{"a" "1"} {"a" "2"}])

^{:refer lua.nginx.conn-postgres/db-error :added "4.1"}
(fact "parses a postgres error string"

  (!.lua (pg/db-error (cat "Mrelation does not exist" (string.char 0) "C42P01" (string.char 0) "Ddetail") true "SELECT 1"))
  => (contains {"debug" {"M" "relation does not exist"
                         "C" "42P01"
                         "D" "detail"}}))

^{:refer lua.nginx.conn-postgres/create :added "4.1"}
(fact "creates a postgres client"

  (pg/create {:host "127.0.0.1"})
  => (contains {"defaults" {"host" "127.0.0.1"}}))

^{:refer lua.nginx.conn-postgres/client-connect :added "4.1"}
(fact "connects a postgres client"

  (!.lua (var client (pg/create (pg/default-env)))
         (pg/client-connect client {})
         (type (. client ["raw"])))
  => "table")

^{:refer lua.nginx.conn-postgres/client-disconnect :added "4.1"}
(fact "disconnects a postgres client"

  (!.lua (var client (pg/create (pg/default-env)))
         (pg/client-connect client {})
         (pg/client-disconnect client))
  => integer?)

^{:refer lua.nginx.conn-postgres/client-query :added "4.1"}
(fact "queries a postgres client"

  (!.lua (var client (pg/create (pg/default-env)))
         (pg/client-connect client {})
         (var out (pg/client-query client "SELECT 1 AS n, 'hello' AS s"))
         (pg/client-disconnect client)
         out)
  => {"n" 1 "s" "hello"})

^{:refer lua.nginx.conn-postgres/client-query-async :added "4.1"}
(fact "queries a postgres client asynchronously"

  (!.lua (var client (pg/create (pg/default-env)))
         (pg/client-connect client {})
         (var p (pg/client-query-async client "SELECT 2 AS n"))
         (pg/client-disconnect client)
         (type p))
  => "table")

^{:refer lua.nginx.conn-postgres/raw-query :added "4.1"}
(fact "performs a raw query on a connected postgres client"

  (!.lua (var client (pg/create (pg/default-env)))
         (pg/client-connect client {})
         (var out (pg/raw-query (. client ["raw"]) "SELECT 3 AS n"))
         (pg/client-disconnect client)
         out)
  => 3)
