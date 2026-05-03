(ns js.lib.driver-redis-test
  (:require [lib.redis.bench :as bench]
             [hara.lang :as l]
             [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.protocol.impl.redis-connection :as redis]
             [xt.lang.spec-base :as xt]
               [xt.lang.common-lib :as k]
               [xt.lang.common-repl :as repl]
               [js.lib.driver-redis :as js-driver]]})

(fact:global
 {:setup    [(bench/start-redis-array [17001])
             (l/rt:restart)
             (Thread/sleep 500)]
  :teardown [(l/rt:stop)
             (bench/stop-redis-array [17001])]})

^{:refer js.lib.driver-redis/connect-constructor :added "4.0" :unchecked true}
(fact "creates a connection"
  (!.js
   (== "Promise"
       (. (js-driver/connect-constructor {:port 17001})
          ["constructor"]
          ["name"])))
  => true

  (do
    (notify/wait-on [:js 5000]
      (. (redis/connect (js-driver/driver) {:port 17001})
         (then (fn [conn]
                 (:= (!:G CONN) conn)
                 (repl/notify true)))))
    (notify/wait-on :js
      (. (redis/exec (!:G CONN) "ping" [])
         (then (fn [out]
                 (repl/notify out))))))
  => "PONG"

  (notify/wait-on :js
    (. (redis/exec (!:G CONN) "echo" ["hello"])
       (then (fn [out]
               (repl/notify out)))))
  => "hello")
