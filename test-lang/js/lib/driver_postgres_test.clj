(ns js.lib.driver-postgres-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.lang.spec-promise :as spec-promise])
  (:use code.test))

(l/script- :postgres
  {:runtime :jdbc.client
   :config {:dbname "test-scratch"}
   :require [[postgres.sample.scratch-v1 :as scratch]]})

(l/script- :js
  {:runtime :basic
   :require [[xt.protocol.impl.connection-sql :as sql]
              [xt.lang.spec-promise :as spec-promise]
                [xt.lang.common-lib :as k]
                [xt.lang.common-repl :as repl]
                [js.lib.driver-postgres :as js-postgres]]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

^{:refer js.lib.driver-postgres/default-env :added "4.0" :unchecked true}
(fact "gets the default env")

^{:refer js.lib.driver-postgres/default-env-set :added "4.0" :unchecked true}
(fact "sets the default env")

^{:refer js.lib.driver-postgres/normalise-query-output :added "4.1"}
(fact "normalises postgres query output"

  (!.js
    [(js-postgres/normalise-query-output {"rows" []})
     (js-postgres/normalise-query-output {"rows" [{"value" 1}]})
     (js-postgres/normalise-query-output {"rows" [{"value" 1}
                                                   {"value" 2}]})])
  => [[] 1 [{"value" 1}
            {"value" 2}]])

^{:refer js.lib.driver-postgres/wrap-connection :added "4.1"}
(fact "wraps a live postgres connection"

  (notify/wait-on [:js 5000]
    (spec-promise/x:promise-then
     (js-postgres/connect-constructor {:database "test-scratch"})
     (fn [raw]
       (var conn (js-postgres/wrap-connection raw))
       (spec-promise/x:promise-then
        (sql/query conn "SELECT \"scratch\".addf(1,2);")
        (fn [out]
          (spec-promise/x:promise-then
           (sql/disconnect conn)
           (fn [_]
             (repl/notify [(sql/connection? conn)
                           out]))))))))
  => [true 3])

^{:refer js.lib.driver-postgres/connect-constructor :added "4.0" :unchecked true}
 (fact "constructs the postgres instance against the scratch sample app"
 
   (!.js
     (== "Promise"
         (. (js-postgres/connect-constructor {:database "test-scratch"})
            ["constructor"]
            ["name"])))
  => true
 
  (notify/wait-on [:js 5000]
    (spec-promise/x:promise-then
     (js-postgres/connect-constructor {:database "test-scratch"})
     (fn [raw]
       (var conn (js-postgres/wrap-connection raw))
       (spec-promise/x:promise-then
        (sql/query conn "SELECT \"scratch\".ping();")
        (fn [out]
          (spec-promise/x:promise-then
           (sql/disconnect conn)
           (fn [_]
             (repl/notify out))))))))
  => "pong")

^{:refer js.lib.driver-postgres/driver :added "4.1"}
(fact "connects through the driver wrapper to the scratch sample app"

  (notify/wait-on [:js 5000]
    (spec-promise/x:promise-then
     (sql/connect (js-postgres/driver) {:database "test-scratch"})
     (fn [conn]
       (spec-promise/x:promise-then
        (sql/query conn "SELECT \"scratch\".addf(10,20);")
        (fn [out]
          (spec-promise/x:promise-then
           (sql/disconnect conn)
           (fn [_]
             (repl/notify [(sql/connection? conn)
                           out]))))))))
  => [true 30])

(comment
  (l/with:input
    (!.js
     (sql/connect {:constructor js-postgres/connect-constructor}
                  {:success (fn [conn]
                              (sql/query conn "SELECT 1;"
                                         (repl/<!)))}))))
