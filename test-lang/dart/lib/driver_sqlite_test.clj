(ns dart.lib.driver-sqlite-test
  (:require [hara.runtime.basic.type-common :as common]
             [hara.lang :as l]
             [xt.lang.common-notify :as notify]
             [xt.lang.spec-promise :as spec-promise])
  (:use code.test))

(l/script- :dart
  {:runtime :twostep
   :require [[dart.lib.driver-sqlite :as dart-sqlite]
             [xt.lang.spec-promise :as spec-promise]
             [xt.lang.common-repl :as repl]
             [xt.protocol.impl.connection-sql :as dbsql]]})

(def CANARY-DART
  (common/program-exists? "dart"))

(fact "connects to sqlite through the Dart driver"
  (if CANARY-DART
    [(notify/wait-on :dart
       (spec-promise/x:promise-then
        (dart-sqlite/connect-constructor {:memory true} nil)
        (fn [conn]
          (repl/notify
           (dart-sqlite/raw-query (. conn ["raw"]) "SELECT 1;")))))
     (notify/wait-on :dart
        (spec-promise/x:promise-then
         (dbsql/connect (dart-sqlite/driver)
                        {:memory true})
         (fn [conn]
           (repl/notify
            (dbsql/query-sync conn "SELECT 1;")))))
     (notify/wait-on :dart
        (spec-promise/x:promise-then
         (dbsql/connect (dart-sqlite/driver)
                        {:memory true})
         (fn [conn]
           (dbsql/query conn "CREATE TABLE test (id INTEGER, name TEXT);")
           (dbsql/query conn "INSERT INTO test (id, name) VALUES (1, 'alpha');")
           (repl/notify
            (dbsql/query conn "SELECT name FROM test;")))))
     (notify/wait-on :dart
        (spec-promise/x:promise-then
         (dbsql/connect (dart-sqlite/driver)
                        {:memory true})
         (fn [conn]
           (repl/notify
            (dbsql/disconnect conn)))))]
    :dart-unavailable)
  => (any [1 1 "alpha" true]
          :dart-unavailable))
