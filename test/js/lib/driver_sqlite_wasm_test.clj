(ns js.lib.driver-sqlite-wasm-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.lang.spec-promise :as spec-promise])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lib.connection-sql :as sql]
              [xt.lang.spec-promise :as spec-promise]
               [xt.lang.common-repl :as repl]
               [js.lib.driver-sqlite-wasm :as js-sqlite-wasm]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer js.lib.driver-sqlite-wasm/raw-query :added "4.1" :unchecked true}
(fact "raw query for sqlite-wasm results")

^{:refer js.lib.driver-sqlite-wasm/make-instance :added "4.1" :unchecked true}
(fact "creates an sqlite-wasm instance once sqlite3 is loaded")

^{:refer js.lib.driver-sqlite-wasm/connect-constructor :added "4.1" :unchecked true}
(fact "connects to an embedded sqlite-wasm database"

  (notify/wait-on :js
    (spec-promise/x:promise-then
     (sql/connect (js-sqlite-wasm/driver) {})
     (fn [conn]
        (repl/notify
         (sql/query conn "SELECT 1;")))))
  => 1)
