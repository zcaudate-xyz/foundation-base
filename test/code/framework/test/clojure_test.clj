(ns code.framework.test.clojure-test
  (:use code.test)
  (:require [code.framework.test.clojure :refer :all]
            [code.framework.docstring :as docstring]
            [code.query.block :as nav]))

^{:refer code.framework.test.clojure/gather-is-form :added "3.0"}
(fact "Make docstring notation out of is form"
  (-> (nav/parse-string "(is (= 1 1))")
      (gather-is-form)
      (docstring/->docstring))
  => "1\n  => 1"

  (-> (nav/parse-string "(is (boolean? 4))")
      (gather-is-form)
      (docstring/->docstring))
  => "(boolean? 4)\n  => true")

^{:refer code.framework.test.clojure/gather-deftest-body :added "3.0"}
(fact "extracts and formats the body of a `deftest` form into docstring notation, used as a helper for `gather-deftest`"

  (-> "(\n  (is (= 1 1))^:hidden\n  (is (identical? 2 4)))"
      (nav/parse-string)
      (nav/down)
      (gather-deftest-body)
      (docstring/->docstring))
  => "\n  1\n  => 1")

^{:refer code.framework.test.clojure/gather-deftest :added "3.0"}
(fact "Make docstring notation out of deftest form"

  (-> "^{:refer example/hello-world :added \"0.1\"}
       (deftest hello-world-test\n  (is (= 1 1))\n  (is (identical? 2 4)))"
      (nav/parse-string)
      nav/down nav/right nav/down nav/right nav/right
      (gather-deftest)
      (update-in [:test] docstring/->docstring))
  => (contains '{:refer example/hello-world
                 :ns example,
                 :var hello-world,
                 :added "0.1",
                 :line {:row 2, :col 8, :end-row 4, :end-col 25},
                 :test "1\n  => 1\n  (identical? 2 4)\n  => true"}))
