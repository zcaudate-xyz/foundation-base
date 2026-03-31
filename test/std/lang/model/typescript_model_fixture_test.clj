(ns std.lang.model.typescript-model-fixture-test
  (:require [std.lang.typed.xtalk :as typed]
            [std.lang.typed.xtalk-common :as types]
            [std.lang.model.typescript-model-fixture])
  (:use code.test))

(fact "typescript typed fixture loads its specs"
  (-> (typed/get-spec 'std.lang.model.typescript-model-fixture/User)
      :type
      types/type->data
      :kind)
  => :record)
