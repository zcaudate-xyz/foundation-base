(ns rt.redis-test
  (:use code.test)
  (:require [rt.redis :refer :all]
            [rt.redis.eval-script :as script]
            [std.lib :as h]
            [net.resp.connection :as conn]
            [std.concurrent :as cc]))

^{:refer rt.redis/generate-script :added "4.0"}
(fact "generates a script given a pointer"
  (with-redefs [script/raw-compile (fn [_] {:body "script"})]
    (generate-script 'ptr))
  => "script")

^{:refer rt.redis/test:req :added "4.0"}
(fact "does a request on a single test connection"
  (with-redefs [conn/with-test:connection (fn [f & _] (f :conn))
                cc/req (fn [_ args] args)]
    (test:req :a 1))
  => [:a 1])

^{:refer rt.redis/test:invoke :added "4.0"}
(fact "does a script call on a single test connection"
  (with-redefs [conn/with-test:connection (fn [f & _] (f :conn))
                rt.redis.client/invoke-ptr-redis (fn [_ ptr args] [ptr args])]
    (test:invoke :ptr 1 2))
  => [:ptr '(1 2)])
