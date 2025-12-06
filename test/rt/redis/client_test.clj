(ns rt.redis.client-test
  (:use [code.test :exclude [run]])
  (:require [lib.redis.bench :as bench]
            [lib.redis.event :as event]
            [rt.redis.client :as r]
            [net.resp.connection :as conn]
            [net.resp.wire :as wire]
            [std.concurrent :as cc]
            [std.lib :as h]
            [rt.redis.eval-script :as eval-script]
            [rt.redis.eval-basic :as eval-basic]))

;; Force registration of redis runtime
(defonce +setup+
  r/+redis-oneshot+)

(fact:global
 {:setup [(bench/start-redis-array [17001])]
  :teardown [(bench/stop-redis-array [17001])]})

^{:refer std.lang.base.runtime-h/wrap-start :adopt true :added "3.0"}
(fact "install setup steps for keys" ^:hidden
  (let [client (-> (r/client:create {:port 17001}) (h/start))]
    (try
      (-> ((h/wrap-start identity [{:key :events  :start event/start:events-redis}])
           (assoc client :reset true :events event/+default+))
          ((comp event/events-string event/config:get)))
      => "h$tlgx"
      (finally
        (h/stop client)))))

^{:refer rt.redis.client/client-steps :added "3.0"}
(fact "clients steps for start up and shutdown")

^{:refer rt.redis.client/client-start :added "4.0"}
(fact "starts the client")

^{:refer rt.redis.client/client:create :added "3.0"}
(fact "creates a redis client"

  (r/client:create {:id "localhost"
                    :port 17001})
  => r/client?)

^{:refer rt.redis.client/client :added "3.0"}
(fact "creates and starts a redis client" ^:hidden
  (let [client (-> (r/client:create {:port 17001}) (h/start))]
    (try
      (cc/pool:with-resource [conn (:pool client)]
                             (->> (conn/connection:request-bulk conn [["SET" "A" "0"]
                                                                      ["INCR" "A"]
                                                                      ["INCR" "A"]
                                                                      ["INCR" "A"]])
                                  (map h/string)))
      => ["OK" "1" "2" "3"]
      (finally
        (h/stop client)))))

^{:refer rt.redis.client/test:client :added "3.0"}
(fact "creates a test client on docker"
  (with-redefs [conn/test:config (fn [] {:port 17001})]
    (r/test:client))
  => r/client?)

^{:refer rt.redis.client/invoke-ptr-redis :added "4.0"}
(fact "invokes the pointer in the redis context"
  (with-redefs [eval-basic/redis-invoke-ptr-basic (fn [_ _ _] :basic)
                eval-script/redis-invoke-sha (fn [_ _ _ _] :script)
                h/error (fn [& _] {:rt/redis true})]
    (r/invoke-ptr-redis {:mode :eval} nil nil)
    => :basic
    (r/invoke-ptr-redis {:mode :prod} nil nil)
    => :script))

^{:refer rt.redis.client/client? :added "3.0"}
(fact "checks that instance is a client"
  (r/client? (r/client:create {}))
  => true)

(comment

  (./import)
  (h/tracked:all [:redis]))
