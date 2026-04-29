(ns xt.old.sys.conn-dbsql-test
  (:require [rt.basic.type-common :as common]
             [std.lang :as l]
             [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :lua.nginx
  {:runtime :basic
   :config {:program :resty}
   :require [[xt.old.sys.conn-dbsql :as dbsql]
             [xt.lang.spec-promise :as spec-promise]
             [xt.lang.common-repl :as repl]
              [lua.nginx.driver-postgres :as lua-postgres]]})

(l/script- :js
  {:runtime :basic
   :require [[xt.old.sys.conn-dbsql :as dbsql]
             [xt.lang.spec-promise :as spec-promise]
              [xt.lang.common-repl :as repl]
              [js.lib.driver-postgres :as js-postgres]
              [js.lib.driver-sqlite :as js-sqlite]]})

(l/script- :dart
  {:runtime :twostep
   :require [[xt.old.sys.conn-dbsql :as dbsql]
             [xt.lang.spec-promise :as spec-promise]
             [xt.lang.common-repl :as repl]
              [dart.lib.driver-sqlite :as dart-sqlite]]})

(def CANARY-DART
  (common/program-exists? "dart"))

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.old.sys.conn-dbsql/connect :added "4.0"}
(fact "connects to a database"

  (notify/wait-on :lua.nginx
    (spec-promise/x:promise-then
     (dbsql/connect {:constructor lua-postgres/connect-constructor})
     (fn [conn]
       (repl/notify
        (dbsql/query conn "SELECT 1;")))))
  => 1

  (notify/wait-on :js
    (spec-promise/x:promise-then
     (dbsql/connect {:constructor js-postgres/connect-constructor})
     (fn [conn]
       (dbsql/query conn "SELECT 1;"
                    (repl/<!)))))
  => (any 1 [{"?column?" 1}])

  (notify/wait-on :js
    (spec-promise/x:promise-then
     (dbsql/connect {:constructor js-sqlite/connect-constructor})
     (fn [conn]
       (dbsql/query conn "SELECT 1;"
                    (repl/<!)))))
  => 1

  (if CANARY-DART
    (notify/wait-on :dart
      (spec-promise/x:promise-then
       (dbsql/connect {:constructor dart-sqlite/connect-constructor
                        :memory true}
                      nil)
       (fn [conn]
         (repl/notify
          (dbsql/query-sync conn "SELECT 1;")))))
    :dart-unavailable)
  => (any 1
          :dart-unavailable))

^{:refer xt.old.sys.conn-dbsql/disconnect :added "4.0"}
(fact "disconnects form database"

  (if CANARY-DART
    (notify/wait-on :dart
      (spec-promise/x:promise-then
       (dbsql/connect {:constructor dart-sqlite/connect-constructor
                        :memory true}
                      nil)
       (fn [conn]
         (repl/notify
          (dbsql/disconnect conn nil)))))
    :dart-unavailable)
  => (any true
          :dart-unavailable))

^{:refer xt.old.sys.conn-dbsql/query-base :added "4.0"}
(fact "calls query without the wrapper")

^{:refer xt.old.sys.conn-dbsql/query :added "4.0"}
(fact "sends a query")

^{:refer xt.old.sys.conn-dbsql/query-sync :added "4.0"}
(fact "sends a synchronous query"

  (if CANARY-DART
    (notify/wait-on :dart
      (spec-promise/x:promise-then
       (dbsql/connect {:constructor dart-sqlite/connect-constructor
                        :memory true}
                      nil)
       (fn [conn]
         (dbsql/query conn "CREATE TABLE test (id INTEGER, name TEXT);" nil)
         (dbsql/query conn "INSERT INTO test (id, name) VALUES (1, 'alpha');" nil)
         (repl/notify
          (dbsql/query-sync conn "SELECT name FROM test;")))))
    :dart-unavailable)
  => (any "alpha"
          :dart-unavailable))

^{:refer xt.old.sys.conn-dbsql/wrap-callback :added "4.1"}
(fact "TODO")
