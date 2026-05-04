(ns hara.model.typescript-model-fixture-test
  (:require [hara.typed.xtalk :as typed]
            [hara.typed.xtalk-common :as types]
            [hara.model.typescript-model-fixture])
  (:use code.test))

(fact "typescript typed fixture loads its specs"
  (-> (typed/get-spec 'hara.model.typescript-model-fixture/User)
      :type
      types/type->data
      :kind)
  => :record)
