(ns js.net.conn-redis-test
  (:use code.test)
  (:require [hara.lang :as l]
            [js.net.conn-redis :as redis]
            [xt.lang.common-data :as xtd]))

(l/script- :js
  {:runtime :basic
   :require [[js.net.conn-redis :as redis]
             [xt.lang.common-data :as xtd]]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:scaffold :js)]
  :teardown [(l/rt:stop)]})

^{:refer js.net.conn-redis/create :added "4.1"}
(fact "creates a redis client record"

  (!.js
   (typeof (redis/create {})))
  => "object")

^{:refer js.net.conn-redis/client-connect :added "4.1"}
(fact "connects the redis client"

  (!.js
   (var client (redis/create {}))
   (var raw {"connect" (fn [] (return (Promise.resolve "connected")))})
   (xtd/obj-assign client {"raw" raw})
   (redis/client-connect client {}))
  => "connected")

^{:refer js.net.conn-redis/client-disconnect :added "4.1"}
(fact "disconnects the redis client"

  (!.js
   (var client {})
   (var closed [])
   (xtd/obj-assign client {"raw" {"quit" (fn [] (. closed (push "closed")) (return (Promise.resolve "closed")))}})
   (redis/client-disconnect client))
  => "closed")

^{:refer js.net.conn-redis/client-exec :added "4.1"}
(fact "executes a redis command"

  (!.js
   (var client {})
   (var sent [])
   (xtd/obj-assign client {"raw" {"sendCommand" (fn [cmd]
                                                   (. sent (push cmd))
                                                   (return (Promise.resolve "ok")))}})
   (redis/client-exec client "GET" ["key"]))
  => "ok")
