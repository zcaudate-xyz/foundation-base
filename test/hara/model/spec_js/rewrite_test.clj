(ns hara.model.spec-js.rewrite-test
  (:require [hara.model.spec-js.rewrite :as rewrite])
  (:use code.test))

^{:refer hara.model.spec-js.rewrite/js-rewrite-stage :added "4.1"}
(fact "keeps JavaScript staging stable when no JS-specific lowering is required"
  (rewrite/js-rewrite-stage
   '(do
      (var f (fn [x] (return x)))
      (f 1))
   nil)
  => '(do
        (var f (fn [x] (return x)))
        (f 1)))
