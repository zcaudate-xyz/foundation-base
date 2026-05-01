(ns xt.lib.sql-connection-test
  (:require [rt.basic.type-common :as common]
            [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lib.sql-connection :as sql]]})

(l/script- :lua
  {:runtime :basic
   :config {:program :resty}
   :require [[xt.lib.sql-connection :as sql]]})

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lib.sql-connection :as sql]]})

(def CANARY-DART
  (common/program-exists? "dart"))

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lib.sql-connection/connection-create :added "4.1"}
(fact "wraps SQL connection implementations with protocol-backed dispatch"

  (!.js
   (var conn (sql/connection-create
              {"tag" "raw"}
              {"query" (fn [raw query]
                         (return 1))
               "query_sync" (fn [raw query]
                              (return 2))
               "disconnect" (fn [raw]
                              (return true))}))
   [(sql/connection? conn)
    (sql/query conn "SELECT 1;")
    (sql/query-sync conn "SELECT 2;")
    (sql/disconnect conn)])
  => [true 1 2 true]

  (!.lua
   (var conn (sql/connection-create
              {"tag" "raw"}
              {"query" (fn [raw query]
                         (return 1))
               "query_sync" (fn [raw query]
                              (return 2))
               "disconnect" (fn [raw]
                              (return true))}))
   [(sql/connection? conn)
    (sql/query conn "SELECT 1;")
    (sql/query-sync conn "SELECT 2;")
    (sql/disconnect conn)])
  => [true 1 2 true]

  (if CANARY-DART
    (!.dt
      (var conn (sql/connection-create
                 {"tag" "raw"}
                 {"query" (fn [raw query]
                            (return 1))
                  "query_sync" (fn [raw query]
                                 (return 2))
                  "disconnect" (fn [raw]
                                 (return true))}))
      [(sql/connection? conn)
       (sql/query conn "SELECT 1;")
       (sql/query-sync conn "SELECT 2;")
       (sql/disconnect conn)])
    :dart-unavailable)
  => (any [true 1 2 true]
          :dart-unavailable))


^{:refer xt.lib.sql-connection/driver? :added "4.1"}
(fact "identifies wrapped sql drivers"
  (!.js
   (var impl
        {"connect" (fn [opts]
                     (return
                      (sql/connection-create
                       opts
                       {"query"      (fn [raw input]
                                       (return input))
                        "query_sync" (fn [raw input]
                                       (return input))
                        "disconnect" (fn [raw]
                                       (return true))})))})
   (var driver
        (sql/driver-create impl))
   [(sql/driver? driver)
    (sql/driver? {"::" "sql.connection"})
    (sql/driver? nil)])
  => [true false false])

^{:refer xt.lib.sql-connection/connection? :added "4.1"}
(fact "identifies wrapped sql connections"
  (!.js
   (var conn
        (sql/connection-create
         {"tag" "raw"}
         {"query"      (fn [raw input]
                         (return input))
          "query_sync" (fn [raw input]
                         (return input))
          "disconnect" (fn [raw]
                         (return true))}))
   [(sql/connection? conn)
    (sql/connection? {"::" "sql.connection.driver"})
    (sql/connection? nil)])
  => [true false false])

^{:refer xt.lib.sql-connection/ensure-promise :added "4.1"}
(fact "normalises sql values into host promises"
  (!.js
   [(promise/x:promise-native? (sql/ensure-promise 5))
    (promise/x:promise-native?
     (sql/ensure-promise
      (promise/x:promise
       (fn []
         (return 6)))))])
  => [true true]

  (notify/wait-on :js
    (promise/x:promise-then
     (sql/ensure-promise 5)
     (repl/>notify)))
  => 5)

^{:refer xt.lib.sql-connection/require-driver :added "4.1"}
(fact "requires wrapped sql drivers"
  (!.js
   (var impl
        {"connect" (fn [opts]
                     (return
                      (sql/connection-create
                       opts
                       {"query"      (fn [raw input]
                                       (return input))
                        "query_sync" (fn [raw input]
                                       (return input))
                        "disconnect" (fn [raw]
                                       (return true))})))})
   (var driver
        (sql/driver-create impl))
   [(sql/driver? (sql/require-driver driver))
    (xt/x:get-key (sql/require-driver driver) "::")])
  => [true "sql.connection.driver"])

^{:refer xt.lib.sql-connection/require-connection :added "4.1"}
(fact "requires wrapped sql connections"
  (!.js
   (var conn
        (sql/connection-create
         {"tag" "raw"}
         {"query"      (fn [raw input]
                         (return input))
          "query_sync" (fn [raw input]
                         (return input))
          "disconnect" (fn [raw]
                         (return true))}))
   [(sql/connection? (sql/require-connection conn))
    (xt/x:get-key (sql/require-connection conn) "::")])
  => [true "sql.connection"])

^{:refer xt.lib.sql-connection/driver-create :added "4.1"}
(fact "wraps sql driver implementations"
  (!.js
   (var impl
        {"connect" (fn [opts]
                     (return
                      (sql/connection-create
                       opts
                       {"query"      (fn [raw input]
                                       (return input))
                        "query_sync" (fn [raw input]
                                       (return input))
                        "disconnect" (fn [raw]
                                       (return true))})))})
   (sql/driver?
    (sql/driver-create impl)))
  => true)

^{:refer xt.lib.sql-connection/connect :added "4.1"}
(fact "connects through wrapped sql drivers"
  (notify/wait-on :js
    (var impl
         {"connect" (fn [opts]
                      (return
                       (sql/connection-create
                        opts
                        {"query"      (fn [raw input]
                                        (return input))
                         "query_sync" (fn [raw input]
                                        (return input))
                         "disconnect" (fn [raw]
                                        (return (xt/x:get-key raw "dsn")))})))})
    (promise/x:promise-then
     (sql/connect
      (sql/driver-create impl)
      {"dsn" "sqlite://connect"})
     (fn [conn]
       (repl/notify [(sql/connection? conn)
                     (sql/disconnect conn)]))))
  => [true "sqlite://connect"])

^{:refer xt.lib.sql-connection/disconnect :added "4.1"}
(fact "disconnects through wrapped sql connections"
  (!.js
   (var conn
        (sql/connection-create
         {"tag" "raw"}
         {"query"      (fn [raw input]
                         (return input))
          "query_sync" (fn [raw input]
                         (return input))
          "disconnect" (fn [raw]
                         (return (xt/x:get-key raw "tag")))}))
   (sql/disconnect conn))
  => "raw")

^{:refer xt.lib.sql-connection/query :added "4.1"}
(fact "queries through wrapped sql connections"
  (!.js
   (var conn
        (sql/connection-create
         {"tag" "raw"}
         {"query"      (fn [raw input]
                         (return [input (xt/x:get-key raw "tag")]))
          "query_sync" (fn [raw input]
                         (return input))
          "disconnect" (fn [raw]
                         (return true))}))
   (sql/query conn "SELECT 1;"))
  => ["SELECT 1;" "raw"])

^{:refer xt.lib.sql-connection/query-sync :added "4.1"}
(fact "runs sync queries through wrapped sql connections"
  (!.js
   (var conn
        (sql/connection-create
         {"tag" "raw"}
         {"query"      (fn [raw input]
                         (return input))
          "query_sync" (fn [raw input]
                         (return [input (xt/x:get-key raw "tag")]))
          "disconnect" (fn [raw]
                         (return true))}))
   (sql/query-sync conn "SELECT 2;"))
  => ["SELECT 2;" "raw"])
