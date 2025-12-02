(ns lib.redis.impl.common-test
  (:use code.test)
  (:require [lib.redis.impl.common :refer :all]
            [std.concurrent :as cc]
            [std.lib :as h]))

^{:refer lib.redis.impl.common/opts:cache :added "3.0"}
(fact "creates a opts map for bulk operations"
  (opts:cache {:a 1}) => map?)

^{:refer lib.redis.impl.common/make-key :added "3.0"}
(fact "creates a namespaced key"

  (make-key nil :hello)
  => "hello" ^:hidden

  (make-key "hello" :there)
  => "hello:there")

^{:refer lib.redis.impl.common/unmake-key :added "3.0"}
(fact "removes the namespaced portion"

  (unmake-key "hello" "hello:there")
  => "there")

^{:refer lib.redis.impl.common/return-default :added "3.0"}
(fact "return for default values"
  ^:hidden

  (return-default 1 {})
  => 1

  (return-default 1 {:async true})
  => h/future?)

^{:refer lib.redis.impl.common/return:format :added "3.0"}
(fact "constructs a return function"
  ^:hidden

  ((return:format :edn) (.getBytes (str {:a 1 :b 2})))
  => {:a 1, :b 2})

^{:refer lib.redis.impl.common/return:string :added "3.0"}
(fact "return function for string"
  ^:hidden

  (return:string (.getBytes "hello") nil)
  => "hello")

^{:refer lib.redis.impl.common/return:raw :added "3.0"}
(fact  "return function for string"
  ^:hidden

  (return:raw (.getBytes "hello") nil)
  => bytes?)

^{:refer lib.redis.impl.common/return:keys :added "3.0"}
(fact  "return function for keys"
  ^:hidden

  (return:keys ["test/hello"] {:namespace "test"})
  => '("hello"))

^{:refer lib.redis.impl.common/return:kv-hash :added "3.0"}
(fact  "return function for string"
  ^:hidden

  (return:kv-hash [(.getBytes "a") (.getBytes (str {:a 1}))]
                  {:format :edn})
  => '("a" {:a 1}))

^{:refer lib.redis.impl.common/process:key :added "3.0"}
(fact "input function for key"
  ^:hidden

  (process:key "hello" {:namespace "test"})
  => "test:hello")

^{:refer lib.redis.impl.common/process:key-multi :added "3.0"}
(fact "input function for keys"
  ^:hidden

  (process:key-multi ["a" "b" "c"] {:namespace "test"})
  => '("test:a" "test:b" "test:c"))

^{:refer lib.redis.impl.common/process:unchanged :added "3.0"}
(fact "input function to unchange input"
  ^:hidden

  (process:unchanged "hello" {})
  => "hello")

^{:refer lib.redis.impl.common/process:data :added "3.0"}
(fact "input function for data"
  ^:hidden

  (process:data {:a 1} {:format :json})
  => "{\"a\":1}")

^{:refer lib.redis.impl.common/process:data-multi :added "3.0"}
(fact "input function for multi data"
  ^:hidden

  (process:data-multi [{:a 1} {:b 2}] {:format :json})
  => '("{\"a\":1}" "{\"b\":2}"))

^{:refer lib.redis.impl.common/process:kv-hash :added "3.0"}
(fact "input function for multi hash methods"
  ^:hidden

  (process:kv-hash ["a" {:a 1}] {:format :json
                                 :namespace "test"})
  => '("a" "{\"a\":1}"))

^{:refer lib.redis.impl.common/process:kv :added "3.0"}
(fact "input function for multi key methods"
  ^:hidden

  (process:kv ["a" {:a 1}] {:format :json
                            :namespace "test"})
  => '("test:a" "{\"a\":1}"))

^{:refer lib.redis.impl.common/in:hash-args :added "3.0"}
(fact "create args from hash-map"
  ^:hidden

  (in:hash-args [{:a 1 :b 2}])
  => '(:a 1 :b 2)

  (in:hash-args [:a 1 :b 2])
  => '[:a 1 :b 2])
