tahto/typed/xtalk_check_test.clj:1:(ns tahto.typed.xtalk-check-test
  (:use code.test)
tahto/typed/xtalk_check_test.clj:3:  (:require [tahto.typed.xtalk-check :refer :all]
tahto/typed/xtalk_check_test.clj:4:            [tahto.typed.xtalk-common :as types]
tahto/typed/xtalk_check_test.clj:5:            [tahto.typed.xtalk-parse :as parse]))

(defn fixture-analysis []
tahto/typed/xtalk_check_test.clj:8:  (parse/analyze-namespace 'tahto.model.spec-xtalk-typed-fixture))

(defn fixture-function [name]
  (some #(when (= name (:name %)) %) (:functions (fixture-analysis))))

tahto/typed/xtalk_check_test.clj:13:^{:refer tahto.typed.xtalk-check/function-env :added "4.1"}
(fact "builds function arg environments"
  (function-env
   (types/make-fn-def 'sample.route 'greet
                      [(types/make-arg 'user-id types/+str-type+ [])
                       (types/make-arg 'active? types/+bool-type+ [])]
                      types/+str-type+ {} ['user-id] nil))
  => '{user-id {:kind :primitive :name :xt/str}
       active? {:kind :primitive :name :xt/bool}})

tahto/typed/xtalk_check_test.clj:23:^{:refer tahto.typed.xtalk-check/check-fn-def :added "4.1"}
(fact "checks function definitions against inferred returns"
  [(-> (check-fn-def (fixture-function "find-user")) :errors)
   (-> (check-fn-def (fixture-function "wrong-user-name")) :errors first :tag)]
  => '[[] :return-type-mismatch])

tahto/typed/xtalk_check_test.clj:29:^{:refer tahto.typed.xtalk-check/check-function :added "4.1"}
(fact "checks fn defs or symbols when registered"
  (do
    (types/clear-registry!)
    (parse/register-types! (fixture-analysis))
    [(-> (check-function (fixture-function "find-user")) :function)
tahto/typed/xtalk_check_test.clj:35:     (-> (check-function 'tahto.model.spec-xtalk-typed-fixture/find-user) :function)
     (nil? (check-function 'sample.route/missing))])
tahto/typed/xtalk_check_test.clj:37:  => '[tahto.model.spec-xtalk-typed-fixture/find-user
tahto/typed/xtalk_check_test.clj:38:        tahto.model.spec-xtalk-typed-fixture/find-user
        true])
