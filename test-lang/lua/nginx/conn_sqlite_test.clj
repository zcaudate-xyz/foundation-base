(ns lua.nginx.conn-sqlite-test
  (:require [std.lib.env :as env]
            [hara.lang :as l])
  (:use code.test))

(l/script- :lua.nginx
  {:runtime :basic
   :test-mode true
   :config  {:program :resty}
   :require [[lua.nginx.conn-sqlite :as sqlite]
             [lua.nginx.common-promise :as p]]})

(fact:global
 {:skip     (not (env/program-exists? "resty"))
  :setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer lua.nginx.conn-sqlite/coerce-number :added "4.1"}
(fact "coerces strings to numbers"

  (!.lua [(sqlite/coerce-number "42")
          (sqlite/coerce-number "3.14")
          (sqlite/coerce-number "abc")
          (sqlite/coerce-number 7)])
  => [42 3.14 "abc" 7])

^{:refer lua.nginx.conn-sqlite/decode-json-scalar :added "4.1"}
(fact "decodes json-like scalar strings"

  (!.lua [(sqlite/decode-json-scalar "[1,2,3]")
          (sqlite/decode-json-scalar "{\"a\":1}")
          (sqlite/decode-json-scalar "true")
          (sqlite/decode-json-scalar "null")
          (sqlite/decode-json-scalar "hello")])
  => [[1 2 3] {"a" 1} true nil "hello"])

^{:refer lua.nginx.conn-sqlite/query-returns-rows? :added "4.1"}
(fact "checks whether a query returns row data"

  (!.lua (sqlite/query-returns-rows? "SELECT * FROM t"))
  => integer?

  (!.lua (sqlite/query-returns-rows? "  select * from t"))
  => integer?

  (!.lua (sqlite/query-returns-rows? "PRAGMA user_version"))
  => integer?

  (!.lua (sqlite/query-returns-rows? "INSERT INTO t VALUES (1)"))
  => nil

  (!.lua (sqlite/query-returns-rows? "CREATE TABLE t (id INT)"))
  => nil)

^{:refer lua.nginx.conn-sqlite/normalize-connect-opts :added "4.1"}
(fact "defaults to an in-memory database"

  (!.lua (sqlite/normalize-connect-opts {}))
  => {"memory" true}

  (!.lua (sqlite/normalize-connect-opts {:filename "/tmp/test.db"}))
  => {"filename" "/tmp/test.db"})

^{:refer lua.nginx.conn-sqlite/create :added "4.1"}
(fact "creates a sqlite client"

  (sqlite/create {:memory true})
  => (contains {"defaults" {"memory" true}}))

^{:refer lua.nginx.conn-sqlite/client-connect :added "4.1"}
(fact "connects a sqlite client"

  (!.lua (var client (sqlite/create {}))
         (sqlite/client-connect client {})
         (type (. client ["raw"])))
  => "userdata")

^{:refer lua.nginx.conn-sqlite/client-disconnect :added "4.1"}
(fact "disconnects a sqlite client"

  (!.lua (var client (sqlite/create {}))
         (sqlite/client-connect client {})
         (sqlite/client-disconnect client))
  => integer?)

^{:refer lua.nginx.conn-sqlite/raw-exec :added "4.1"}
(fact "performs a raw execution"

  (!.lua (var client (sqlite/create {}))
         (sqlite/client-connect client {})
         (sqlite/raw-exec (. client ["raw"]) "CREATE TABLE t (id INT, name TEXT)")
         (sqlite/raw-exec (. client ["raw"]) "INSERT INTO t VALUES (1, 'hello')")
         (var out (sqlite/raw-exec (. client ["raw"]) "SELECT * FROM t"))
         (sqlite/client-disconnect client)
         out)
  => [{"id" "1" "name" "hello"}])

^{:refer lua.nginx.conn-sqlite/raw-query :added "4.1"}
(fact "performs a raw query"

  (!.lua (var client (sqlite/create {}))
         (sqlite/client-connect client {})
         (sqlite/raw-exec (. client ["raw"]) "CREATE TABLE t (id INT)")
         (sqlite/raw-exec (. client ["raw"]) "INSERT INTO t VALUES (1)")
         (sqlite/raw-exec (. client ["raw"]) "INSERT INTO t VALUES (2)")
         (sqlite/raw-exec (. client ["raw"]) "INSERT INTO t VALUES (3)")
         (var out (sqlite/raw-query (. client ["raw"]) "SELECT * FROM t"))
         (sqlite/client-disconnect client)
         out)
  => [1 2 3])

^{:refer lua.nginx.conn-sqlite/client-query :added "4.1"}
(fact "queries a sqlite client"

  (!.lua (var client (sqlite/create {}))
         (sqlite/client-connect client {})
         (sqlite/client-query client "CREATE TABLE t (id INT)")
         (sqlite/client-query client "INSERT INTO t VALUES (42)")
         (var out (sqlite/client-query client "SELECT * FROM t"))
         (sqlite/client-disconnect client)
         out)
  => 42)

^{:refer lua.nginx.conn-sqlite/client-query-async :added "4.1"}
(fact "queries a sqlite client asynchronously"

  (!.lua (var client (sqlite/create {}))
         (sqlite/client-connect client {})
         (var p (sqlite/client-query-async client "SELECT 1 AS n"))
         (sqlite/client-disconnect client)
         (type p))
  => "table")
