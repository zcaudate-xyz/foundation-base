(ns js.lib.driver-postgres-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]
            [xt.lang.base-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.sys.conn-dbsql :as dbsql]
             [xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [js.lib.driver-postgres :as js-postgres]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer js.lib.driver-postgres/default-env :added "4.0" :unchecked true}
(fact "gets the default env")

^{:refer js.lib.driver-postgres/default-env-set :added "4.0" :unchecked true}
(fact "sets the default env")

^{:refer js.lib.driver-postgres/set-methods :added "4.0" :unchecked true}
(fact "sets the methods for the object")

^{:refer js.lib.driver-postgres/connect-constructor :added "4.0" :unchecked true}
(fact "constructs the postgres instance"
  ^:hidden

  (str (!.js
        (js-postgres/connect-constructor (js-postgres/default-env))))
  => "<Client>\n[object Object]"

  
  (do (notify/wait-on [:js 5000]
        (dbsql/connect {:constructor js-postgres/connect-constructor}
                       {:success (fn [conn]
                                   (:= (!:G CONN) conn)
                                   (repl/notify true))}))
      (notify/wait-on :js
        (dbsql/query (!:G CONN) "SELECT 1;" (repl/<!))))
  => (any nil 1 [{"?column?" 1}]))

(comment
  (l/with:input
    (!.js
     (dbsql/connect {:constructor js-postgres/connect-constructor}
                    {:success (fn [conn]
                                (dbsql/query conn "SELECT 1;"
                                             (repl/<!)))}))))
