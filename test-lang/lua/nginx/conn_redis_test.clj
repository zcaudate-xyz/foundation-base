(ns lua.nginx.conn-redis-test
  (:require [std.lib.env :as env]
            [hara.lang :as l])
  (:use code.test))

(l/script- :lua.nginx
  {:runtime :basic
   :test-mode true
   :config  {:program :resty}
   :require [[lua.nginx.conn-redis :as redis]]})

(fact:global
 {:skip     (or (not (env/program-exists? "resty"))
                (not (env/program-exists? "redis-cli")))
  :setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer lua.nginx.conn-redis/create :added "4.1"}
(fact "creates a redis client"

  (redis/create {:host "127.0.0.1"})
  => (contains {"defaults" {"host" "127.0.0.1"}}))

^{:refer lua.nginx.conn-redis/client-connect :added "4.1"}
(fact "connects a redis client"

  (!.lua (var client (redis/create {}))
         (redis/client-connect client {})
         (type (. client ["raw"])))
  => "table")

^{:refer lua.nginx.conn-redis/client-disconnect :added "4.1"}
(fact "disconnects a redis client"

  (!.lua (var client (redis/create {}))
         (redis/client-connect client {})
         (redis/client-disconnect client))
  => integer?)

^{:refer lua.nginx.conn-redis/client-exec :added "4.1"}
(fact "executes a redis command"

  (!.lua (var client (redis/create {}))
         (redis/client-connect client {})
         (redis/client-exec client "set" ["test-key" "hello"])
         (var out (redis/client-exec client "get" ["test-key"]))
         (redis/client-disconnect client)
         out)
  => "hello")
