(ns rt.redis.eval-script-test
  (:use code.test)
  (:require [rt.redis.eval-script :refer :all]
            [rt.redis.client :as r]
            [lib.redis.bench :as bench]
            [kmi.redis :as redis]
            [std.concurrent :as cc]
            [std.lib :as h]
            [std.lang :as l]
            [std.lang.base.pointer :as ptr]
            [std.lang.base.impl :as impl]
            [lib.redis.script :as script]))

(fact:global
 {:setup [(bench/start-redis-array [17001])]
  :teardown [(bench/stop-redis-array [17001])]})

^{:refer rt.redis.eval-script/raw-compile-form :added "4.0"}
(fact "converts a ptr into a form"
  (with-redefs [ptr/get-entry (fn [_] {:form '(defn foo [x] x) :id 'foo :module 'mod :rt/redis {:nkeys 0}})]
    (raw-compile-form 'ptr))
  => '(return (mod/foo (unpack ARGV))))

^{:refer rt.redis.eval-script/raw-compile :added "4.0"}
(fact "compiles a function as body and sha"
  (with-redefs [raw-compile-form (fn [_] '(return 1))
                impl/emit-script (fn [& _] "return 1")]
    (raw-compile 'ptr))
  => {:body "return 1", :sha "e95b5d8294339e7c23973902f50464772228717d"})

^{:refer rt.redis.eval-script/raw-prep-in-fn :added "4.0"}
(fact "prepares the arguments for entry"
  (raw-prep-in-fn {:rt/redis {:nkeys 1}} [:key :arg])
  => [[:key] [:arg]])

^{:refer rt.redis.eval-script/raw-prep-out-fn :added "4.0"}
(fact "prepares arguments out"
  (raw-prep-out-fn {:rt/redis {:encode {:out true}}} "{\"a\":1}")
  => {:a 1})

^{:refer rt.redis.eval-script/rt-install-fn :added "4.0"}
(fact "retries the function if not installed"
  (with-redefs [script/script:load (fn [& _] :load)
                script/script:evalsha (fn [& _] :eval)]
    ((rt-install-fn {} "sha" "body" [] []) (Exception. "NOSCRIPT")))
  => :eval)

^{:refer rt.redis.eval-script/redis-invoke-sha :added "4.0"
  :setup    [(def -client- (r/client {:port 17001}))
             (cc/req -client- ["FLUSHDB"])
             (cc/req -client- ["SCRIPT" "FLUSH"])]
  :teardown [(h/stop -client-)]}
(fact "creates a sha call"
  (with-redefs [ptr/get-entry (fn [_] {:rt/redis {:nkeys 0}})
                raw-compile (fn [_] {:body "return 1" :sha "sha"})
                raw-prep-in-fn (fn [_ args] [[] args])
                script/script:evalsha (fn [& _] 1)]
    (redis-invoke-sha -client- 'ptr ["PING"]))
  => 1)
