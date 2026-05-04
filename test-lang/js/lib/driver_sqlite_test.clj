(ns js.lib.driver-sqlite-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.lang.spec-promise :as spec-promise])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.protocol.impl.connection-sql :as sql]
              [xt.lang.spec-promise :as spec-promise]
               [xt.lang.common-lib :as k]
               [xt.lang.common-repl :as repl]
               [js.lib.driver-sqlite :as js-sqlite]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer js.lib.driver-sqlite/raw-query :added "4.0" :unchecked true}
(fact "raw query for sql lite results")

^{:refer js.lib.driver-sqlite/wrap-connection :added "4.1"}
(fact "TODO")

^{:refer js.lib.driver-sqlite/make-instance :added "4.0" :unchecked true}
(fact "creates a instance once SQL is loaded")

^{:refer js.lib.driver-sqlite/connect-constructor :added "4.0" :unchecked true}
 (fact "connects to an embedded sqlite file"

  (notify/wait-on :js
    (spec-promise/x:promise-then
     (sql/connect (js-sqlite/driver) {})
     (fn [conn]
        (repl/notify
         (sql/query conn "SELECT 1;")))))
  => 1)

^{:refer js.lib.driver-sqlite/driver :added "4.1"}
(fact "TODO")