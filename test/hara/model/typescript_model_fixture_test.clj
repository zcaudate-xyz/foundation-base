tahto/model/typescript_model_fixture_test.clj:1:(ns tahto.model.typescript-model-fixture-test
tahto/model/typescript_model_fixture_test.clj:2:  (:require [tahto.typed :as typed]
tahto/model/typescript_model_fixture_test.clj:3:            [tahto.typed.xtalk-common :as types]
tahto/model/typescript_model_fixture_test.clj:4:            [tahto.model.typescript-model-fixture])
  (:use code.test))

(fact "typescript typed fixture loads its specs"
tahto/model/typescript_model_fixture_test.clj:8:  (-> (typed/spec-def (typed/load-ns 'tahto.model.typescript-model-fixture)
tahto/model/typescript_model_fixture_test.clj:9:                       'tahto.model.typescript-model-fixture/User)
      :type
      types/type->data
      :kind)
  => :record)
