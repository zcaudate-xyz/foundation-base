(ns lua.nginx.driver-redis-test
  (:require [lib.redis.bench :as bench]
            [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :lua.nginx
  {:runtime :basic
   :config  {:program :resty}
   :require [[xt.protocol.impl.redis-connection :as redis]
              [xt.lang.common-lib :as k]
              [lua.nginx.driver-redis :as lua-driver]]})

(fact:global
 {:setup    [(bench/start-redis-array [17001])
             (l/rt:restart)]
  :teardown [(l/rt:stop)
             (bench/stop-redis-array [17001])]})

^{:refer lua.nginx.driver-redis/connect-constructor :added "4.0"}
(fact "creates a xt.sys compatible constructor"

  (notify/wait-on [:lua.nginx 2000]
    (!.lua
     (var driver (lua-driver/driver))
     (. (redis/connect driver {:port 17001})
        (then (fn [conn]
                (return (redis/exec conn "ping" [])))))))
  => "PONG")
