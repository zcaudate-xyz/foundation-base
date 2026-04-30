(ns js.lib.driver-redis-test
  (:require [lib.redis.bench :as bench]
             [std.lang :as l]
             [xt.lang.common-notify :as notify]
             [xt.lang.spec-promise :as spec-promise])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as spec-promise]
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
 
  (notify/wait-on :js
    (spec-promise/x:promise-then
     (js-driver/connect-constructor {:port 17001})
     (fn [conn]
       (spec-promise/x:promise-then
        ((xt/x:get-key conn "::exec") "ping" [])
        (repl/>notify)))))
  => "PONG"
 
  (notify/wait-on :js
    (spec-promise/x:promise-then
     (js-driver/connect-constructor {:port 17001})
     (fn [conn]
       (spec-promise/x:promise-then
        ((xt/x:get-key conn "::exec") "echo" ["hello"])
        (repl/>notify)))))
  => "hello")
