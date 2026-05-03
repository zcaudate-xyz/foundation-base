(ns hara.lang.typed.xtalk-analysis-test
  (:use code.test)
  (:require [hara.lang.typed.xtalk-analysis :refer :all]
            [hara.lang.typed.xtalk-common :as types]))

^{:refer hara.lang.typed.xtalk-analysis/analyze-file :added "4.1"}
(fact "analyzes files directly"
(count (:specs (analyze-file "test/hara.lang/model/spec_xtalk_typed_fixture.clj")))
  => 3)

^{:refer hara.lang.typed.xtalk-analysis/analyze-file-raw :added "4.1"}
(fact "returns raw parsed analysis without spec attachment"
  (let [result (analyze-file-raw "test/hara.lang/model/spec_xtalk_typed_fixture.clj")]
    [(map? result)
     (contains? result :specs)
     (= (:ns result) 'hara.lang.model.spec-xtalk-typed-fixture)])
  => [true true true])

^{:refer hara.lang.typed.xtalk-analysis/analyze-namespace :added "4.1"}
(fact "provides attached namespace analysis examples"
  (let [analysis (analyze-namespace 'hara.lang.model.spec-xtalk-typed-fixture)]
    {:ns (:ns analysis)
     :aliases (select-keys (:aliases analysis) '[k])
     :specs (mapv :name (:specs analysis))
     :functions (mapv (fn [fn-def]
                        {:name (:name fn-def)
                         :inputs (mapv (comp types/type->data :type) (:inputs fn-def))
                         :output (types/type->data (:output fn-def))})
                      (:functions analysis))})
  => '{:ns hara.lang.model.spec-xtalk-typed-fixture
       :aliases {k xt.lang.common-lib}
       :specs ["User" "UserMap" "find-user"]
       :functions [{:name "find-user"
                    :inputs [{:kind :named :name hara.lang.model.spec-xtalk-typed-fixture/UserMap}
                             {:kind :primitive :name :xt/str}]
                    :output {:kind :maybe
                             :item {:kind :named :name hara.lang.model.spec-xtalk-typed-fixture/User}}}
                   {:name "wrong-user-name"
                    :inputs [{:kind :primitive :name :xt/unknown}]
                    :output {:kind :named :name hara.lang.model.spec-xtalk-typed-fixture/User}}
                   {:name "find-user-wrong-key"
                    :inputs [{:kind :named :name hara.lang.model.spec-xtalk-typed-fixture/UserMap}
                             {:kind :primitive :name :xt/int}]
                    :output {:kind :maybe
                             :item {:kind :named :name hara.lang.model.spec-xtalk-typed-fixture/User}}}]})

^{:refer hara.lang.typed.xtalk-analysis/analyze-namespace-raw :added "4.1"}
(fact "exposes raw analysis without same-name spec attachment"
  (let [analysis (analyze-namespace-raw 'hara.lang.model.spec-xtalk-typed-fixture)
        fn-def (some #(when (= "find-user" (:name %)) %)
                     (:functions analysis))]
    {:inputs (mapv (comp types/type->data :type) (:inputs fn-def))
     :output (types/type->data (:output fn-def))})
  => '{:inputs [{:kind :primitive :name :xt/unknown}
                {:kind :primitive :name :xt/unknown}]
       :output {:kind :primitive :name :xt/unknown}})

^{:refer hara.lang.typed.xtalk-analysis/analyze-and-register! :added "4.1"}
(fact "registers analysis results"
  (do
    (types/clear-registry!)
    (analyze-and-register! 'hara.lang.model.spec-xtalk-typed-fixture)
    (some? (types/get-function 'hara.lang.model.spec-xtalk-typed-fixture/find-user)))
  => true)

^{:refer hara.lang.typed.xtalk-analysis/resolve-function-def :added "4.1"}
(fact "resolves function defs from symbols"
  (do
    (types/clear-registry!)
    (:name (resolve-function-def 'hara.lang.model.spec-xtalk-typed-fixture/find-user)))
  => "find-user")

^{:refer hara.lang.typed.xtalk-analysis/get-function-report :added "4.1"}
(fact "provides function report examples"
  (do
    (types/clear-registry!)
    {:ok (get-function-report 'hara.lang.model.spec-xtalk-typed-fixture/find-user)
     :bad (-> (get-function-report 'hara.lang.model.spec-xtalk-typed-fixture/wrong-user-name)
              (update :errors
                      (fn [errors]
                        (mapv #(select-keys % [:tag :expected :actual])
                              errors))))})
  => '{:ok {:function hara.lang.model.spec-xtalk-typed-fixture/find-user
            :declared {:inputs [{:name users
                                 :type {:kind :named :name hara.lang.model.spec-xtalk-typed-fixture/UserMap}}
                                {:name id
                                 :type {:kind :primitive :name :xt/str}}]
                       :output {:kind :maybe
                                :item {:kind :named :name hara.lang.model.spec-xtalk-typed-fixture/User}}}
            :return {:kind :maybe
                     :item {:kind :named :name hara.lang.model.spec-xtalk-typed-fixture/User}}
            :errors []}
       :bad {:function hara.lang.model.spec-xtalk-typed-fixture/wrong-user-name
             :declared {:inputs [{:name user
                                  :type {:kind :primitive :name :xt/unknown}}]
                        :output {:kind :named :name hara.lang.model.spec-xtalk-typed-fixture/User}}
             :return {:kind :primitive :name :xt/str}
             :errors [{:tag :return-type-mismatch
                       :expected {:kind :named :name hara.lang.model.spec-xtalk-typed-fixture/User}
                       :actual {:kind :primitive :name :xt/str}}]}})

^{:refer hara.lang.typed.xtalk-analysis/get-function-input-type :added "4.1"}
(fact "returns named input types as data"
  (do
    (types/clear-registry!)
    (get-function-input-type 'hara.lang.model.spec-xtalk-typed-fixture/find-user 'id))
  => '{:kind :primitive :name :xt/str})

^{:refer hara.lang.typed.xtalk-analysis/get-function-output-type :added "4.1"}
(fact "returns function output types as data"
  (do
    (types/clear-registry!)
    (get-function-output-type 'hara.lang.model.spec-xtalk-typed-fixture/find-user))
  => '{:kind :maybe :item {:kind :named :name hara.lang.model.spec-xtalk-typed-fixture/User}})

^{:refer hara.lang.typed.xtalk-analysis/check-namespace :added "4.1"}
(fact "provides namespace report examples"
  (do
    (types/clear-registry!)
    (let [report (check-namespace 'hara.lang.model.spec-xtalk-typed-fixture)]
      {:namespace (:namespace report)
       :functions (mapv (fn [{:keys [function return errors]}]
                          {:function function
                           :return return
                           :error-tags (mapv :tag errors)})
                        (:functions report))}))
  => '{:namespace hara.lang.model.spec-xtalk-typed-fixture
       :functions [{:function hara.lang.model.spec-xtalk-typed-fixture/find-user
                    :return {:kind :maybe
                             :item {:kind :named :name hara.lang.model.spec-xtalk-typed-fixture/User}}
                    :error-tags []}
                   {:function hara.lang.model.spec-xtalk-typed-fixture/wrong-user-name
                    :return {:kind :primitive :name :xt/str}
                    :error-tags [:return-type-mismatch]}
                   {:function hara.lang.model.spec-xtalk-typed-fixture/find-user-wrong-key
                    :return {:kind :maybe
                             :item {:kind :named :name hara.lang.model.spec-xtalk-typed-fixture/User}}
                    :error-tags [:call-arg-type-mismatch]}]})

^{:refer hara.lang.typed.xtalk-analysis/report-json :added "4.1"}
(fact "renders reports as json"
  [(report-json {:a 1})
   (boolean (re-find #"\n" (report-json {:a 1} true)))]
  => ["{\"a\":1}" true])