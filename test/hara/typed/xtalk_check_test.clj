(ns hara.typed.xtalk-check-test
  (:use code.test)
  (:require [hara.typed.xtalk-check :refer :all]
            [hara.typed.xtalk-common :as types]
            [hara.typed.xtalk-parse :as parse]))

(defn fixture-analysis []
  (parse/analyze-namespace 'hara.model.spec-xtalk-typed-fixture))

(defn fixture-function [name]
  (some #(when (= name (:name %)) %) (:functions (fixture-analysis))))

^{:refer hara.typed.xtalk-check/function-env :added "4.1"}
(fact "builds function arg environments"
  (function-env
   (types/make-fn-def 'sample.route 'greet
                      [(types/make-arg 'user-id types/+str-type+ [])
                       (types/make-arg 'active? types/+bool-type+ [])]
                      types/+str-type+ {} ['user-id] nil))
  => '{user-id {:kind :primitive :name :xt/str}
       active? {:kind :primitive :name :xt/bool}})

^{:refer hara.typed.xtalk-check/check-fn-def :added "4.1"}
(fact "checks function definitions against inferred returns"
  [(-> (check-fn-def (fixture-function "find-user")) :errors)
   (-> (check-fn-def (fixture-function "wrong-user-name")) :errors first :tag)]
  => '[[] :return-type-mismatch])

^{:refer hara.typed.xtalk-check/check-function :added "4.1"}
(fact "checks fn defs or symbols when registered"
  (do
    (types/clear-registry!)
    (parse/register-types! (fixture-analysis))
    [(-> (check-function (fixture-function "find-user")) :function)
     (-> (check-function 'hara.model.spec-xtalk-typed-fixture/find-user) :function)
     (nil? (check-function 'sample.route/missing))])
  => '[hara.model.spec-xtalk-typed-fixture/find-user
        hara.model.spec-xtalk-typed-fixture/find-user
        true])
