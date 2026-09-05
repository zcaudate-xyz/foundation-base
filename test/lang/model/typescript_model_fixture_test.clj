(ns lang.model.typescript-model-fixture-test
  (:require [lang.typed :as typed]
            [lang.typed.xtalk-common :as types]
            [lang.model.typescript-model-fixture])
  (:use code.test))

(fact "typescript typed fixture loads its specs"
  (-> (typed/spec-def (typed/load-ns 'lang.model.typescript-model-fixture)
                       'lang.model.typescript-model-fixture/User)
      :type
      types/type->data
      :kind)
  => :record)
