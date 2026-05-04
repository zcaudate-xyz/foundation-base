(ns hara.runtime.redis.eval-script-test
  (:require [clojure.string :as str]
             [kmi.redis :as redis]
             [lib.redis.bench :as bench]
             [lib.redis.script :as script]
             [hara.runtime.redis.client :as r]
            [hara.runtime.redis.eval-script :refer :all]
            [std.concurrent :as cc]
            [hara.lang :as l]
            [hara.lang.impl :as impl]
            [hara.lang.pointer :as ptr]
            [std.lib.component :as component]
            [std.string.prose :as prose])
  (:use code.test))

(fact:global
 {:setup [(bench/start-redis-array [17001])]
  :teardown [(bench/stop-redis-array [17001])]})

^{:refer hara.runtime.redis.eval-script/raw-compile-form :added "4.0"}
(fact "converts a ptr into a form"

  (raw-compile-form redis/scan-sub)
  => '(return (kmi.redis/scan-sub (. KEYS [1]) (unpack ARGV))))

^{:refer hara.runtime.redis.eval-script/raw-compile :added "4.0"}
(fact "compiles a function as body and sha"

  (raw-compile redis/scan-sub)
  => (contains
      {:body #(and (string? %)
                   (str/includes? % "local function scan_sub(key)")
                   (str/includes? % "return scan_sub(KEYS[1],unpack(ARGV))"))
       :sha string?}))

^{:refer hara.runtime.redis.eval-script/raw-prep-in-fn :added "4.0"}
(fact "prepares the arguments for entry"

  (raw-prep-in-fn {:rt/redis {:nkeys 1}} [:key :arg])
  => [[:key] [:arg]])

^{:refer hara.runtime.redis.eval-script/raw-prep-out-fn :added "4.0"}
(fact "prepares arguments out"

  (raw-prep-out-fn {:rt/redis {:encode {:out true}}} "{\"a\":1}")
  => {:a 1})

^{:refer hara.runtime.redis.eval-script/rt-install-fn :added "4.0"}
(fact "retries the function if not installed"

  (with-redefs [script/script:load (fn [& _] :load)
                script/script:evalsha (fn [& _] :eval)]
    ((rt-install-fn {} "sha" "body" [] []) (Exception. "NOSCRIPT")))
  => :eval)

^{:refer hara.runtime.redis.eval-script/redis-invoke-sha :added "4.0"
  :setup    [(def -client- (r/client {:port 17001}))
             (cc/req -client- ["FLUSHDB"])
             (cc/req -client- ["SCRIPT" "FLUSH"])]
  :teardown [(component/stop -client-)]}
(fact "creates a sha call"

  (with-redefs [ptr/get-entry (fn [_] {:rt/redis {:nkeys 0}})
                raw-compile (fn [_] {:body "return 1" :sha "sha"})
                raw-prep-in-fn (fn [_ args] [[] args])
                script/script:evalsha (fn [& _] 1)]
    (redis-invoke-sha -client- 'ptr ["PING"]))
  => 1)
