(ns lua.nginx.driver-postgres-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.lang.spec-promise :as spec-promise]
            [lua.nginx.driver-postgres :refer :all])
  (:use code.test))

(l/script- :postgres
  {:runtime :jdbc.client
   :config {:dbname "test-scratch"}
   :require [[postgres.sample.scratch-v1 :as scratch]]})

(l/script- :lua.nginx
  {:runtime :basic
   :config {:program :resty}
   :require [[xt.protocol.impl.connection-sql :as sql]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as spec-promise]
             [lua.nginx.driver-postgres :as lua-pg]]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

^{:refer lua.nginx.driver-postgres/default-env :added "4.0"}
(fact "gets the default env")

^{:refer lua.nginx.driver-postgres/default-env-set :added "4.0"}
(fact "sets the default env")

^{:refer lua.nginx.driver-postgres/db-error :added "4.0"}
(fact "gets the db error")

^{:refer lua.nginx.driver-postgres/normalise-query-output :added "4.1"}
(fact "normalises postgres query output"

  (!.lua
    [(lua-pg/normalise-query-output [])
     (lua-pg/normalise-query-output [{"value" 1}])
     (lua-pg/normalise-query-output [{"value" 1}
                                     {"value" 2}])
     (lua-pg/normalise-query-output true)])
  => [[] "1" [{"value" 1}
              {"value" 2}]
      []])

^{:refer lua.nginx.driver-postgres/raw-query :added "4.0"}
(fact "runs live queries against the scratch sample app"

  (!.lua
    (local conn (lua-pg/connect-constructor {:database "test-scratch"}))
    (lua-pg/raw-query conn "SELECT \"scratch\".addf(1,2);"))
  => "3")

^{:refer lua.nginx.driver-postgres/connect-constructor :added "4.0"}
(fact "connects to postgres and can call scratch functions"

  (!.lua
    (local conn (lua-pg/connect-constructor {:database "test-scratch"}))
    (lua-pg/raw-query conn "SELECT \"scratch\".ping();"))
  => "pong")


^{:refer lua.nginx.driver-postgres/wrap-connection :added "4.1"}
(fact "wraps the live connection with the SQL protocol"

  (!.lua
    (local raw (lua-pg/connect-constructor {:database "test-scratch"}))
    (local conn (lua-pg/wrap-connection raw))
    [(sql/connection? conn)
     (sql/query conn "SELECT \"scratch\".addf(3,4);")])
   => [true "7"])

^{:refer lua.nginx.driver-postgres/driver :added "4.1"}
(fact "connects through the driver wrapper to the scratch sample app"

  (notify/wait-on [:lua.nginx 2000]
    (spec-promise/x:promise-then
      (sql/connect (lua-pg/driver) {:database "test-scratch"})
      (fn [conn]
        (repl/notify
         [(sql/connection? conn)
          (sql/query conn "SELECT \"scratch\".addf(10,20);")]))))
  => [true "30"])
