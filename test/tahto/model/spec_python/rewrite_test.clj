(ns tahto.model.spec-python.rewrite-test
  (:require [clojure.walk]
            [tahto.model.spec-python :as py]
            [tahto.model.spec-python.rewrite :as rewrite])
  (:use code.test))

^{:refer tahto.model.spec-python.rewrite/python-normalize-form :added "4.1"}
(fact "normalizes python forms")

^{:refer tahto.model.spec-python.rewrite/python-rewrite-stage :added "4.1"}
(fact "keeps top-level named functions intact"
  (rewrite/python-rewrite-stage
   '(fn f-raw [x]
      (return (* x 10)))
   nil)
  => '(fn f-raw [x]
        (return (* x 10))))
