(ns lua.nginx.driver-sqlite-test
  (:require [std.json :as json]
            [std.lang :as l])
  (:use code.test))

(l/script- :lua.nginx
  {:runtime :basic
   :config {:program :resty}
   :require [[xt.lang.common-lib :as k]
             [lua.nginx.driver-sqlite :as lua-sqlite]
             [xt.sys.conn-dbsql :as driver]]})

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
  => [1 2 3 4])

^{:refer lua.nginx.driver-sqlite/connect-constructor :added "4.0"}
(fact "create db connection"

  ;; ENCODING TRUE INTEGER
  (!.lua
   (local conn (lua-sqlite/connect-constructor {:memory true}))
   (driver/query conn "select 1;"))
  => 1)
