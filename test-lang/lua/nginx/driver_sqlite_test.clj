(ns lua.nginx.driver-sqlite-test
  (:require [std.json :as json]
            [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.lang.spec-promise :as spec-promise])
  (:use code.test))

(l/script- :lua.nginx
  {:runtime :basic
   :config {:program :resty}
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as spec-promise]
              [lua.nginx.driver-sqlite :as lua-sqlite]
              [xt.protocol.impl.connection-sql :as driver]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer lua.nginx.driver-sqlite/CANARY :adopt true :added "4.0"}
(fact "preliminary checks"

  (!.lua
   (:= ngxsqlite (require "lsqlite3"))
   [(lua-sqlite/version)
    (lua-sqlite/lversion)])
  => (contains [string?
                string?]))

^{:refer lua.nginx.driver-sqlite/coerce-number :added "4.0"}
(fact "Performs a raw query"

  (lua-sqlite/coerce-number "hello")
  => "hello"

  (lua-sqlite/coerce-number "1")
  => 1)

^{:refer lua.nginx.driver-sqlite/raw-exec :added "4.0"}
(fact "performs a raw execution"

  (!.lua
   (local conn (lua-sqlite/connect-constructor {:memory true}))
   (lua-sqlite/raw-exec (. conn ["raw"]) "select 1;"))
  => [{"1" "1"}]

  (!.lua
    (local conn (lua-sqlite/connect-constructor {:memory true}))
    (lua-sqlite/raw-exec (. conn ["raw"]) "select value from json_each('[1,2,3,4]')"))
  => [{"value" "1"} {"value" "2"} {"value" "3"} {"value" "4"}])

^{:refer lua.nginx.driver-sqlite/raw-query :added "4.0"}
(fact "Performs a raw query"

  (!.lua
   (local conn (lua-sqlite/connect-constructor {:memory true}))
   (lua-sqlite/raw-query (. conn ["raw"]) "select 1;"))
  => 1

  (!.lua
   (local conn (lua-sqlite/connect-constructor {:memory true}))
   (lua-sqlite/raw-query (. conn ["raw"]) "select value from json_each('[1,2,3,4]')"))
  => [1 2 3 4]

  (!.lua
   (local conn (lua-sqlite/connect-constructor {:memory true}))
   (lua-sqlite/raw-query (. conn ["raw"])
                         "create table hello (id integer); insert into hello values (1);"))
  => [])

^{:refer lua.nginx.driver-sqlite/connect-constructor :added "4.0"}
(fact "create db connection"

  (!.lua
   (local conn (lua-sqlite/connect-constructor {}))
   [ (not (xt/x:nil? (. conn ["raw"])))
     ((xt/x:get-key conn "::query_sync") "select 1;")])
  => [true 1]

  (!.lua
   (local conn (lua-sqlite/connect-constructor {:memory true}))
   [(not (xt/x:nil? (. conn ["raw"])))
    ((xt/x:get-key conn "::query") "select 1;")
    ((xt/x:get-key conn "::query_sync") "select 2;")])
  => [true 1 2])


^{:refer lua.nginx.driver-sqlite/wrap-connection :added "4.1"}
(fact "wraps the raw sqlite connection with the SQL protocol"

  (!.lua
   (local raw (lua-sqlite/connect-constructor {:memory true}))
   (local conn (lua-sqlite/wrap-connection raw))
   [(driver/connection? conn)
    (driver/query conn "select 3;")
    (driver/query-sync conn "select 4;")])
  => [true 3 4])

^{:refer lua.nginx.driver-sqlite/driver :added "4.1"}
(fact "connects through the sqlite driver wrapper"

  (notify/wait-on [:lua.nginx 2000]
    (spec-promise/x:promise-then
     (driver/connect (lua-sqlite/driver) {:memory true})
     (fn [conn]
       (repl/notify
        [(driver/connection? conn)
         (driver/query conn "select 5;")
         (driver/query-sync conn "select 6;")]))))
  => [true 5 6])
