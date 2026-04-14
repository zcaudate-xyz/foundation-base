(ns lua.nginx.driver-redis-test
  (:require [lib.redis.bench :as bench]
            [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :lua
  {:runtime :basic
   :config  {:program :resty}
   :require [[xt.sys.conn-redis :as redis]
             [xt.lang.base-lib :as k]
             [lua.nginx.driver-redis :as lua-driver]]})

(fact:global
 {:setup    [(bench/start-redis-array [17001])
             (l/rt:restart)]
  :teardown [(l/rt:stop)
             (bench/stop-redis-array [17001])]})

^{:refer lua.nginx.driver-redis/connect-constructor :added "4.0"}
(fact "creates a xt.sys compatible constructor"
  ^:hidden
  
  (!.lua
   (var conn (redis/connect {:constructor lua-driver/connect-constructor
                             :port 17001}
                            {}))
   (redis/exec conn "ping" [] {}))
  => "PONG")
