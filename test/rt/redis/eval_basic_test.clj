(ns rt.redis.eval-basic-test
  (:use code.test)
  (:require [rt.redis.eval-basic :refer :all]
            [lib.redis.bench :as bench]
            [std.lang :as l]
            [xt.lang.base-lib :as k]
            [lib.redis.script :as script]))

(l/script- :lua
  {:runtime :redis.client
   :config {:port 17001}})

(fact:global
 {:setup [(bench/start-redis-array [17001])]
  :teardown [(bench/stop-redis-array [17001])]})

^{:refer rt.redis.eval-basic/rt-exception :added "4.0"}
(fact "processes an exception"
  (try
    (rt-exception (Exception. "ERR Error running script (user_script:1)") {} "body")
    (catch clojure.lang.ExceptionInfo e
      (.getMessage e)))
  => string?)

^{:refer rt.redis.eval-basic/redis-raw-eval :added "4.0"}
(fact "conducts a raw ewal"
  (with-redefs [script/script:eval (fn [& _] 3)]
    (redis-raw-eval (l/rt :lua)
                    "return 1 + 2"))
  => 3)

^{:refer rt.redis.eval-basic/redis-body-transform :added "4.0"}
(fact "transform body into output form"

  (redis-body-transform '(+ 1 2 3)
                        {})
  => '(return (return-wrap (fn [] (return (+ 1 2 3))))))

^{:refer rt.redis.eval-basic/redis-invoke-ptr-basic :added "4.0"}
(fact "invokes pointer for redis eval"
  (with-redefs [redis-raw-eval (fn [_ _] [2 3 4 5 6])]
    (redis-invoke-ptr-basic
     (l/rt :lua)
     k/arr-map [[1 2 3 4 5] k/inc]))
  => [2 3 4 5 6])
