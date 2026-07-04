(ns js.net.conn-redis-test
  (:use code.test)
  (:require [hara.lang :as l]
            [js.net.conn-redis :as redis]
            [xt.lang.common-notify :as notify]
            [xt.lang.common-repl :as repl]
            [xt.lang.spec-promise :as promise]))

(l/script- :js
  {:runtime :basic
   :require [[js.net.conn-redis :as redis]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]]})

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

  (notify/wait-on :js
    (-> (redis/client-connect (redis/create {}) {})
        (promise/x:promise-then
         (fn [client]
           (repl/notify (typeof client))))))
  => "object")

^{:refer js.net.conn-redis/client-disconnect :added "4.1"}
(fact "disconnects the redis client"

  (notify/wait-on :js
    (-> (redis/client-connect (redis/create {}) {})
        (promise/x:promise-then
         (fn [client]
           (-> (redis/client-disconnect client)
               (promise/x:promise-then
                (fn [res]
                  (repl/notify res))))))))
  => "OK")

^{:refer js.net.conn-redis/client-exec :added "4.1"}
(fact "executes a redis command"

  (notify/wait-on :js
    (-> (redis/client-connect (redis/create {}) {})
        (promise/x:promise-then
         (fn [client]
           (-> (redis/client-exec client "PING" [])
               (promise/x:promise-then
                (fn [res]
                  (redis/client-disconnect client)
                  (repl/notify res))))))))
  => "PONG")
