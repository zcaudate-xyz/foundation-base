(ns js.lib.driver-sqlite-wasm-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.sys.conn-dbsql :as dbsql]
             [xt.lang.common-repl :as repl]
             [js.lib.driver-sqlite-wasm :as js-sqlite-wasm]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer js.lib.driver-sqlite-wasm/raw-query :added "4.1" :unchecked true}
(fact "raw query for sqlite-wasm results")

^{:refer js.lib.driver-sqlite-wasm/set-methods :added "4.1" :unchecked true}
(fact "sets the query and disconnect methods")

^{:refer js.lib.driver-sqlite-wasm/make-instance :added "4.1" :unchecked true}
(fact "creates an sqlite-wasm instance once sqlite3 is loaded")

^{:refer js.lib.driver-sqlite-wasm/connect-constructor :added "4.1" :unchecked true}
(fact "connects to an embedded sqlite-wasm database"
  ^:hidden

  (notify/wait-on :js
    (dbsql/connect {:constructor js-sqlite-wasm/connect-constructor}
                   {:success (fn [conn]
                               (dbsql/query conn "SELECT 1;"
                                            (repl/<!)))}))
  => 1)
