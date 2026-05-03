(ns hara.lang.model.typescript-model-fixture-test
  (:require [hara.lang.typed.xtalk :as typed]
            [hara.lang.typed.xtalk-common :as types]
            [hara.lang.model.typescript-model-fixture])
  (:use code.test))

(fact "typescript typed fixture loads its specs"
  (-> (typed/get-spec 'hara.lang.model.typescript-model-fixture/User)
      :type
      types/type->data
      :kind)
  => :record)
