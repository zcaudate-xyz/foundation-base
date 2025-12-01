(ns js.lib.driver-redis-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]
            [xt.lang.base-notify :as notify]
            [lib.redis.bench :as bench]))

(l/script- :js
  {:runtime :basic
   :require [[xt.sys.conn-redis :as redis]
             [xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [js.lib.driver-redis :as js-driver]]})

(fact:global
 {:setup    [(bench/start-redis-array [17001])
             (l/rt:restart)
             (Thread/sleep 500)]
  :teardown [(l/rt:stop)
             (bench/stop-redis-array [17001])]})

^{:refer js.lib.driver-redis/connect-constructor :added "4.0" :unchecked true}
(fact "creates a connection"
  ^:hidden

  (notify/wait-on :js
    (redis/connect {:constructor js-driver/connect-constructor
                    :port 17001}
                   {:success (fn [conn]
                               (redis/exec conn "ping" []
                                           (repl/<!)))}))
  => "PONG"
  
  (notify/wait-on :js
    (redis/connect {:constructor js-driver/connect-constructor
                    :port 17001}
                   {:success (fn [conn]
                               (redis/exec conn "echo" ["hello"]
                                           (repl/<!)))}))
  => "hello")
