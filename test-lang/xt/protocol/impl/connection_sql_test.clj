(ns xt.protocol.impl.connection-sql-test
  (:use code.test)
  (:require [hara.runtime.basic.type-common  :as common]
            [hara.lang              :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as xt-promise]
             [xt.protocol.impl.connection-sql :as sql]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as xt-promise]
             [xt.protocol.impl.connection-sql :as sql]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as xt-promise]
             [xt.protocol.impl.connection-sql :as sql]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.protocol.impl.connection-sql/driver? :added "4.1"}
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
  => [true false false]

  (!.lua
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
  => [true false false]

  (!.py
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

^{:refer xt.protocol.impl.connection-sql/connection? :added "4.1"}
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
  => [true false false]

  (!.lua
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
  => [true false false]

  (!.py
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

^{:refer xt.protocol.impl.connection-sql/ensure-promise :added "4.1"}
(fact "normalises sql values into host promises"

  (!.js
    [(xt-promise/x:promise-native? (sql/ensure-promise 5))
     (xt-promise/x:promise-native?
      (sql/ensure-promise
       (xt-promise/x:promise
        (fn []
          (return 6)))))])
  => [true true]

  (notify/wait-on :js
    (xt-promise/x:promise-then
     (sql/ensure-promise 5)
     (repl/>notify)))
  => 5

  (!.lua
    [(xt-promise/x:promise-native? (sql/ensure-promise 5))
     (xt-promise/x:promise-native?
      (sql/ensure-promise
       (xt-promise/x:promise
        (fn []
          (return 6)))))])
  => [true true]

  (notify/wait-on :lua
    (xt-promise/x:promise-then
     (sql/ensure-promise 5)
     (repl/>notify)))
  => 5

  (!.py
    [(xt-promise/x:promise-native? (sql/ensure-promise 5))
     (xt-promise/x:promise-native?
      (sql/ensure-promise
       (xt-promise/x:promise
        (fn []
          (return 6)))))])
  => [true true]

  (notify/wait-on :python
    (xt-promise/x:promise-then
     (sql/ensure-promise 5)
     (repl/>notify)))
  => 5)

^{:refer xt.protocol.impl.connection-sql/require-driver :added "4.1"}
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
  => [true "sql.connection.driver"]

  (!.lua
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
  => [true "sql.connection.driver"]

  (!.py
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

^{:refer xt.protocol.impl.connection-sql/require-connection :added "4.1"}
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
  => [true "sql.connection"]

  (!.lua
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
  => [true "sql.connection"]

  (!.py
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

^{:refer xt.protocol.impl.connection-sql/connection-create :added "4.1"}
(fact "wraps SQL connection implementations with protocol-backed dispatch"

  (!.js
    (var conn (sql/connection-create
               {"tag"         "raw"}
               {"query"       (fn [raw query]
                                (return 1))
                "query_sync"  (fn [raw query]
                                (return 2))
                "disconnect"  (fn [raw]
                                (return true))}))
    [(sql/connection? conn)
     (sql/query conn "SELECT 1;")
     (sql/query-sync conn "SELECT 2;")
     (sql/disconnect conn)])
  => [true 1 2 true]

  (!.lua
    (var conn (sql/connection-create
               {"tag"         "raw"}
               {"query"       (fn [raw query]
                                (return 1))
                "query_sync"  (fn [raw query]
                                (return 2))
                "disconnect"  (fn [raw]
                                (return true))}))
    [(sql/connection? conn)
     (sql/query conn "SELECT 1;")
     (sql/query-sync conn "SELECT 2;")
     (sql/disconnect conn)])
  => [true 1 2 true]

  (!.py
    (var conn (sql/connection-create
               {"tag"         "raw"}
               {"query"       (fn [raw query]
                                (return 1))
                "query_sync"  (fn [raw query]
                                (return 2))
                "disconnect"  (fn [raw]
                                (return true))}))
    [(sql/connection? conn)
     (sql/query conn "SELECT 1;")
     (sql/query-sync conn "SELECT 2;")
     (sql/disconnect conn)])
  => [true 1 2 true])

^{:refer xt.protocol.impl.connection-sql/driver-create :added "4.1"}
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
  => true

  (!.lua
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
  => true

  (!.py
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

^{:refer xt.protocol.impl.connection-sql/connect :added "4.1"}
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
    (xt-promise/x:promise-then
     (sql/connect
      (sql/driver-create impl)
      {"dsn" "sqlite://connect"})
     (fn [conn]
       (repl/notify [(sql/connection? conn)
                     (sql/disconnect conn)]))))
  => [true "sqlite://connect"]

  (notify/wait-on :lua
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
    (xt-promise/x:promise-then
     (sql/connect
      (sql/driver-create impl)
      {"dsn" "sqlite://connect"})
     (fn [conn]
       (repl/notify [(sql/connection? conn)
                     (sql/disconnect conn)]))))
  => [true "sqlite://connect"]

  (notify/wait-on :python
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
    (xt-promise/x:promise-then
     (sql/connect
      (sql/driver-create impl)
      {"dsn" "sqlite://connect"})
     (fn [conn]
       (repl/notify [(sql/connection? conn)
                     (sql/disconnect conn)]))))
  => [true "sqlite://connect"])

^{:refer xt.protocol.impl.connection-sql/disconnect :added "4.1"}
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
  => "raw"

  (!.lua
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
  => "raw"

  (!.py
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

^{:refer xt.protocol.impl.connection-sql/query :added "4.1"}
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
  => ["SELECT 1;" "raw"]

  (!.lua
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
  => ["SELECT 1;" "raw"]

  (!.py
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

^{:refer xt.protocol.impl.connection-sql/query-sync :added "4.1"}
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
  => ["SELECT 2;" "raw"]

  (!.lua
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
  => ["SELECT 2;" "raw"]
  
  (!.py
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

(comment
  (s/snapto '[xt.protocol.impl.connection-sql])
  
  (s/seedgen-langadd '[xt.protocol.impl.connection-sql] {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.protocol.impl.connection-sql] {:lang [:lua :python] :write true}))