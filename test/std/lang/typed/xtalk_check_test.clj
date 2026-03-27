(ns std.lang.typed.xtalk-check-test
  (:use code.test)
  (:require [std.lang.typed.xtalk-check :refer :all]
            [std.lang.typed.xtalk-common :as types]
            [std.lang.typed.xtalk-parse :as parse]))

(defn fixture-analysis []
  (parse/analyze-namespace 'std.lang.model.spec-xtalk-typed-fixture))

(defn fixture-function [name]
  (some #(when (= name (:name %)) %) (:functions (fixture-analysis))))

^{:refer std.lang.typed.xtalk-check/function-env :added "4.1"}
(fact "builds function arg environments"
  (function-env
   (types/make-fn-def 'sample.route 'greet
                      [(types/make-arg 'user-id types/+str-type+ [])
                       (types/make-arg 'active? types/+bool-type+ [])]
                      types/+str-type+ {} ['user-id] nil))
  => '{user-id {:kind :primitive :name :xt/str}
       active? {:kind :primitive :name :xt/bool}})

^{:refer std.lang.typed.xtalk-check/check-fn-def :added "4.1"}
(fact "checks function definitions against inferred returns"
  [(-> (check-fn-def (fixture-function "find-user")) :errors)
   (-> (check-fn-def (fixture-function "wrong-user-name")) :errors first :tag)]
  => '[[] :return-type-mismatch])

^{:refer std.lang.typed.xtalk-check/check-function :added "4.1"}
(fact "checks fn defs or symbols when registered"
  (do
    (types/clear-registry!)
    (parse/register-types! (fixture-analysis))
    [(-> (check-function (fixture-function "find-user")) :function)
     (-> (check-function 'std.lang.model.spec-xtalk-typed-fixture/find-user) :function)
     (nil? (check-function 'sample.route/missing))])
  => '[std.lang.model.spec-xtalk-typed-fixture/find-user
        std.lang.model.spec-xtalk-typed-fixture/find-user
        true])
