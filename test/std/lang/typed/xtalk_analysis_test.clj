(ns std.lang.typed.xtalk-analysis-test
  (:use code.test)
  (:require [std.lang.typed.xtalk-analysis :refer :all]
            [std.lang.typed.xtalk-common :as types]))

^{:refer std.lang.typed.xtalk-analysis/analyze-file :added "4.1"}
(fact "analyzes files directly"
(count (:specs (analyze-file "test/std/lang/model/spec_xtalk_typed_fixture.clj")))
  => 3)

^{:refer std.lang.typed.xtalk-analysis/analyze-namespace :added "4.1"}
(fact "analyzes namespaces through source lookup"
  (:ns (analyze-namespace 'std.lang.model.spec-xtalk-typed-fixture))
  => 'std.lang.model.spec-xtalk-typed-fixture)

^{:refer std.lang.typed.xtalk-analysis/analyze-namespace-raw :added "4.1"}
(fact "exposes raw analysis without same-name spec attachment"
  (let [analysis (analyze-namespace-raw 'std.lang.model.spec-xtalk-typed-fixture)
        fn-def (some #(when (= "find-user" (:name %)) %)
                     (:functions analysis))]
    {:inputs (mapv (comp types/type->data :type) (:inputs fn-def))
     :output (types/type->data (:output fn-def))})
  => '{:inputs [{:kind :primitive :name :xt/unknown}
                {:kind :primitive :name :xt/unknown}]
       :output {:kind :primitive :name :xt/unknown}})

^{:refer std.lang.typed.xtalk-analysis/analyze-and-register! :added "4.1"}
(fact "registers analysis results"
  (do
    (types/clear-registry!)
    (analyze-and-register! 'std.lang.model.spec-xtalk-typed-fixture)
    (some? (types/get-function 'std.lang.model.spec-xtalk-typed-fixture/find-user)))
  => true)

^{:refer std.lang.typed.xtalk-analysis/resolve-function-def :added "4.1"}
(fact "resolves function defs from symbols"
  (do
    (types/clear-registry!)
    (:name (resolve-function-def 'std.lang.model.spec-xtalk-typed-fixture/find-user)))
  => "find-user")

^{:refer std.lang.typed.xtalk-analysis/get-function-report :added "4.1"}
(fact "returns function reports"
  (do
    (types/clear-registry!)
    (-> (get-function-report 'std.lang.model.spec-xtalk-typed-fixture/find-user) :errors))
  => [])

^{:refer std.lang.typed.xtalk-analysis/get-function-input-type :added "4.1"}
(fact "returns named input types as data"
  (do
    (types/clear-registry!)
    (get-function-input-type 'std.lang.model.spec-xtalk-typed-fixture/find-user 'id))
  => '{:kind :primitive :name :xt/str})

^{:refer std.lang.typed.xtalk-analysis/get-function-output-type :added "4.1"}
(fact "returns function output types as data"
  (do
    (types/clear-registry!)
    (get-function-output-type 'std.lang.model.spec-xtalk-typed-fixture/find-user))
  => '{:kind :maybe :item {:kind :named :name std.lang.model.spec-xtalk-typed-fixture/User}})

^{:refer std.lang.typed.xtalk-analysis/check-namespace :added "4.1"}
(fact "checks namespaces through analysis"
  (do
    (types/clear-registry!)
    (:namespace (check-namespace 'std.lang.model.spec-xtalk-typed-fixture)))
  => 'std.lang.model.spec-xtalk-typed-fixture)

^{:refer std.lang.typed.xtalk-analysis/report-json :added "4.1"}
(fact "renders reports as json"
  [(report-json {:a 1})
   (boolean (re-find #"\n" (report-json {:a 1} true)))]
  => ["{\"a\":1}" true])
