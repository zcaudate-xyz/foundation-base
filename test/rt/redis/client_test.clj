(ns rt.redis.client-test
  (:require [lib.redis.bench :as bench]
            [lib.redis.event :as event]
            [net.resp.connection :as conn]
            [net.resp.wire :as wire]
            [rt.redis.client :as r]
            [rt.redis.eval-basic :as eval-basic]
            [rt.redis.eval-script :as eval-script]
            [std.concurrent :as cc]
            [std.lib :as h]
            [std.lib.component]
            [std.lib.foundation])
  (:use [code.test :exclude [run]])
  (:refer-clojure :exclude [read]))

(fact:global
 {:setup [(bench/start-redis-array [17001])]
  :teardown [(bench/stop-redis-array [17001])]})

^{:refer std.lang.base.runtime-h/wrap-start :adopt true :added "3.0"}
(fact "install setup steps for keys"
  ^:hidden

  (std.lib.component/with [|client| (r/client:create {:port 17001})]
    (-> ((std.lib.component/wrap-start identity [{:key :events  :start event/start:events-redis}])
         (assoc |client| :reset true :events event/+default+))
        ((comp event/events-string event/config:get))))
  => "h$tlgx")

^{:refer rt.redis.client/client-steps :added "3.0"}
(fact "clients steps for start up and shutdown")

^{:refer rt.redis.client/client-start :added "4.0"}
(fact "starts the client")

^{:refer rt.redis.client/client:create :added "3.0"}
(fact "creates a redis client"
  ^:hidden
  
  (r/client:create {:id "localhost"
                    :port 17001})
  => r/client?)

^{:refer rt.redis.client/client :added "3.0"}
(fact "creates and starts a redis client"
  ^:hidden

  (std.lib.component/with [|client| (r/client:create {:port 17001})]
    (cc/pool:with-resource [conn (:pool |client|)]
      (->> (conn/connection:request-bulk conn [["SET" "A" "0"]
                                               ["INCR" "A"]
                                               ["INCR" "A"]
                                               ["INCR" "A"]])
           (map std.lib.foundation/string))))
  => ["OK" "1" "2" "3"])

^{:refer rt.redis.client/test:client :added "3.0"}
(fact "creates a test client on docker"
  ^:hidden
  
  (std.lib.component/with [|client| (r/client:create {:port 17001})]
    (with-redefs [conn/test:config (fn [] {:port 17001})]
      (r/test:client)))
  => r/client?)

^{:refer rt.redis.client/invoke-ptr-redis :added "4.0"}
(fact "invokes the pointer in the redis context")

^{:refer rt.redis.client/client? :added "3.0"}
(fact "checks that instance is a client"
  
  (r/client? (r/client:create {}))
  => true)

(comment

  (std.lib.component/with [|client| (r/client:create {:port 17001})]
    (with-redefs [eval-basic/redis-invoke-ptr-basic (fn [_ _ _] :basic)
                  eval-script/redis-invoke-sha (fn [_ _ _ _] :script)]
      (r/invoke-ptr-redis {:mode :eval} nil nil)
      => :basic
      (r/invoke-ptr-redis {:mode :prod} nil nil)
      => :script))
  (./import)
  (std.lib.component.track/tracked:all [:redis]))
