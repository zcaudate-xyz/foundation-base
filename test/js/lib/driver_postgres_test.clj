(ns js.lib.driver-postgres-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.lang.spec-promise :as spec-promise])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lib.connection-sql :as sql]
              [xt.lang.spec-promise :as spec-promise]
               [xt.lang.common-lib :as k]
               [xt.lang.common-repl :as repl]
               [js.lib.driver-postgres :as js-postgres]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer js.lib.driver-postgres/default-env :added "4.0" :unchecked true}
(fact "gets the default env")

^{:refer js.lib.driver-postgres/default-env-set :added "4.0" :unchecked true}
(fact "sets the default env")

^{:refer js.lib.driver-postgres/connect-constructor :added "4.0" :unchecked true}
 (fact "constructs the postgres instance"
 
   (!.js
    (== "Promise"
        (. (js-postgres/connect-constructor (js-postgres/default-env))
           ["constructor"]
           ["name"])))
  => true


  (do (notify/wait-on [:js 5000]
        (spec-promise/x:promise-then
         (sql/connect (js-postgres/driver) {})
         (fn [conn]
            (:= (!:G CONN) conn)
            (repl/notify true))))
      (notify/wait-on :js
        (spec-promise/x:promise-then
         (sql/query (!:G CONN) "SELECT 1;")
         (fn [out]
            (repl/notify out)))))
  => (any nil 1 [{"?column?" 1}]))

(comment
  (l/with:input
    (!.js
     (sql/connect {:constructor js-postgres/connect-constructor}
                  {:success (fn [conn]
                              (sql/query conn "SELECT 1;"
                                         (repl/<!)))}))))
