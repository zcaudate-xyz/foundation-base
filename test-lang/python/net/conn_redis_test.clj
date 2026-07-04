(ns python.net.conn-redis-test
  (:require [hara.lang :as l]
            [std.lib.env :as env])
  (:use code.test))

(defn- redis-module-available?
  []
  (try
    (let [process (.start (ProcessBuilder. (into-array String ["python3" "-c" "import redis"])))]
      (= 0 (.waitFor process)))
    (catch Exception _ false)))

(l/script- :python
  {:runtime :basic
   :require [[python.net.conn-redis :as redis]]})

(fact:global
 {:skip     (or (not (env/program-exists? "python3"))
                (not (env/program-exists? "redis-cli"))
                (not (redis-module-available?)))
  :setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer python.net.conn-redis/load-module :added "4.1"}
(fact "loads the redis python module"

  (!.py (str (redis/load-module)))
  => #"<module 'redis'")

^{:refer python.net.conn-redis/create :added "4.1"}
(fact "creates a redis client"

  (redis/create {})
  => (contains {"::" "python.net.conn_redis/PythonRedisClient"
                "defaults" {}}))

^{:refer python.net.conn-redis/client-connect :added "4.1"}
(fact "connects a redis client"

  (!.py (do (var client (redis/create {}))
            (redis/client-connect client {})
            (var out (redis/client-exec client "ping" []))
            (redis/client-disconnect client)
            (return out)))
  => true)

^{:refer python.net.conn-redis/client-disconnect :added "4.1"}
(fact "disconnects a redis client"

  (!.py (do (var client (redis/create {}))
            (redis/client-connect client {})
            (return (redis/client-disconnect client))))
  => true)

^{:refer python.net.conn-redis/client-exec :added "4.1"}
(fact "executes a redis command"

  (!.py (do (var client (redis/create {}))
            (redis/client-connect client {})
            (redis/client-exec client "set" ["test-key" "test-value"])
            (var out (. (redis/client-exec client "get" ["test-key"]) (decode "utf-8")))
            (redis/client-disconnect client)
            (return out)))
  => "test-value")
