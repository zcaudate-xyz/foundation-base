(ns tahto.model.typescript-model-fixture-test
  (:require [tahto.typed :as typed]
            [tahto.typed.xtalk-common :as types]
            [tahto.model.typescript-model-fixture])
  (:use code.test))

(fact "typescript typed fixture loads its specs"
  (-> (typed/spec-def (typed/load-ns 'tahto.model.typescript-model-fixture)
                       'tahto.model.typescript-model-fixture/User)
      :type
      types/type->data
      :kind)
  => :record)
