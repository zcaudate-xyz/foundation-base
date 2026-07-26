tahto/typed/xtalk_analysis_test.clj:1:(ns tahto.typed.xtalk-analysis-test
  (:use code.test)
tahto/typed/xtalk_analysis_test.clj:3:  (:require [tahto.typed.xtalk-analysis :refer :all]
tahto/typed/xtalk_analysis_test.clj:4:            [tahto.typed.xtalk-common :as types]))

tahto/typed/xtalk_analysis_test.clj:6:^{:refer tahto.typed.xtalk-analysis/analyze-file :added "4.1"}
(fact "analyzes files directly"
(count (:specs (analyze-file "test/tahto.core/model/spec_xtalk_typed_fixture.clj")))
  => 3)

tahto/typed/xtalk_analysis_test.clj:11:^{:refer tahto.typed.xtalk-analysis/analyze-file-raw :added "4.1"}
(fact "returns raw parsed analysis without spec attachment"
  (let [result (analyze-file-raw "test/tahto.core/model/spec_xtalk_typed_fixture.clj")]
    [(map? result)
     (contains? result :specs)
tahto/typed/xtalk_analysis_test.clj:16:     (= (:ns result) 'tahto.model.spec-xtalk-typed-fixture)])
  => [true true true])

tahto/typed/xtalk_analysis_test.clj:19:^{:refer tahto.typed.xtalk-analysis/analyze-namespace :added "4.1"}
(fact "provides attached namespace analysis examples"
tahto/typed/xtalk_analysis_test.clj:21:  (let [analysis (analyze-namespace 'tahto.model.spec-xtalk-typed-fixture)]
    {:ns (:ns analysis)
     :aliases (select-keys (:aliases analysis) '[k])
     :specs (mapv :name (:specs analysis))
     :functions (mapv (fn [fn-def]
                        {:name (:name fn-def)
                         :inputs (mapv (comp types/type->data :type) (:inputs fn-def))
                         :output (types/type->data (:output fn-def))})
                      (:functions analysis))})
tahto/typed/xtalk_analysis_test.clj:30:  => '{:ns tahto.model.spec-xtalk-typed-fixture
       :aliases {k xt.lang.common-lib}
       :specs ["User" "UserMap" "find-user"]
       :functions [{:name "find-user"
tahto/typed/xtalk_analysis_test.clj:34:                    :inputs [{:kind :named :name tahto.model.spec-xtalk-typed-fixture/UserMap}
                             {:kind :primitive :name :xt/str}]
                    :output {:kind :maybe
tahto/typed/xtalk_analysis_test.clj:37:                             :item {:kind :named :name tahto.model.spec-xtalk-typed-fixture/User}}}
                   {:name "wrong-user-name"
                    :inputs [{:kind :primitive :name :xt/unknown}]
tahto/typed/xtalk_analysis_test.clj:40:                    :output {:kind :named :name tahto.model.spec-xtalk-typed-fixture/User}}
                   {:name "find-user-wrong-key"
tahto/typed/xtalk_analysis_test.clj:42:                    :inputs [{:kind :named :name tahto.model.spec-xtalk-typed-fixture/UserMap}
                             {:kind :primitive :name :xt/int}]
                    :output {:kind :maybe
tahto/typed/xtalk_analysis_test.clj:45:                             :item {:kind :named :name tahto.model.spec-xtalk-typed-fixture/User}}}]})

tahto/typed/xtalk_analysis_test.clj:47:^{:refer tahto.typed.xtalk-analysis/analyze-namespace-raw :added "4.1"}
(fact "exposes raw analysis without same-name spec attachment"
tahto/typed/xtalk_analysis_test.clj:49:  (let [analysis (analyze-namespace-raw 'tahto.model.spec-xtalk-typed-fixture)
        fn-def (some #(when (= "find-user" (:name %)) %)
                     (:functions analysis))]
    {:inputs (mapv (comp types/type->data :type) (:inputs fn-def))
     :output (types/type->data (:output fn-def))})
  => '{:inputs [{:kind :primitive :name :xt/unknown}
                {:kind :primitive :name :xt/unknown}]
       :output {:kind :primitive :name :xt/unknown}})

tahto/typed/xtalk_analysis_test.clj:58:^{:refer tahto.typed.xtalk-analysis/analyze-and-register! :added "4.1"}
(fact "registers analysis results"
  (do
    (types/clear-registry!)
tahto/typed/xtalk_analysis_test.clj:62:    (analyze-and-register! 'tahto.model.spec-xtalk-typed-fixture)
tahto/typed/xtalk_analysis_test.clj:63:    (some? (types/get-function 'tahto.model.spec-xtalk-typed-fixture/find-user)))
  => true)

tahto/typed/xtalk_analysis_test.clj:66:^{:refer tahto.typed.xtalk-analysis/resolve-function-def :added "4.1"}
(fact "resolves function defs from symbols"
  (do
    (types/clear-registry!)
tahto/typed/xtalk_analysis_test.clj:70:    (:name (resolve-function-def 'tahto.model.spec-xtalk-typed-fixture/find-user)))
  => "find-user")

tahto/typed/xtalk_analysis_test.clj:73:^{:refer tahto.typed.xtalk-analysis/get-function-report :added "4.1"}
(fact "provides function report examples"
  (do
    (types/clear-registry!)
tahto/typed/xtalk_analysis_test.clj:77:    {:ok (get-function-report 'tahto.model.spec-xtalk-typed-fixture/find-user)
tahto/typed/xtalk_analysis_test.clj:78:     :bad (-> (get-function-report 'tahto.model.spec-xtalk-typed-fixture/wrong-user-name)
              (update :errors
                      (fn [errors]
                        (mapv #(select-keys % [:tag :expected :actual])
                              errors))))})
tahto/typed/xtalk_analysis_test.clj:83:  => '{:ok {:function tahto.model.spec-xtalk-typed-fixture/find-user
            :declared {:inputs [{:name users
tahto/typed/xtalk_analysis_test.clj:85:                                 :type {:kind :named :name tahto.model.spec-xtalk-typed-fixture/UserMap}}
                                {:name id
                                 :type {:kind :primitive :name :xt/str}}]
                       :output {:kind :maybe
tahto/typed/xtalk_analysis_test.clj:89:                                :item {:kind :named :name tahto.model.spec-xtalk-typed-fixture/User}}}
            :return {:kind :maybe
tahto/typed/xtalk_analysis_test.clj:91:                     :item {:kind :named :name tahto.model.spec-xtalk-typed-fixture/User}}
            :errors []}
tahto/typed/xtalk_analysis_test.clj:93:       :bad {:function tahto.model.spec-xtalk-typed-fixture/wrong-user-name
             :declared {:inputs [{:name user
                                  :type {:kind :primitive :name :xt/unknown}}]
tahto/typed/xtalk_analysis_test.clj:96:                        :output {:kind :named :name tahto.model.spec-xtalk-typed-fixture/User}}
             :return {:kind :primitive :name :xt/str}
             :errors [{:tag :return-type-mismatch
tahto/typed/xtalk_analysis_test.clj:99:                       :expected {:kind :named :name tahto.model.spec-xtalk-typed-fixture/User}
                       :actual {:kind :primitive :name :xt/str}}]}})

tahto/typed/xtalk_analysis_test.clj:102:^{:refer tahto.typed.xtalk-analysis/get-function-input-type :added "4.1"}
(fact "returns named input types as data"
  (do
    (types/clear-registry!)
tahto/typed/xtalk_analysis_test.clj:106:    (get-function-input-type 'tahto.model.spec-xtalk-typed-fixture/find-user 'id))
  => '{:kind :primitive :name :xt/str})

tahto/typed/xtalk_analysis_test.clj:109:^{:refer tahto.typed.xtalk-analysis/get-function-output-type :added "4.1"}
(fact "returns function output types as data"
  (do
    (types/clear-registry!)
tahto/typed/xtalk_analysis_test.clj:113:    (get-function-output-type 'tahto.model.spec-xtalk-typed-fixture/find-user))
tahto/typed/xtalk_analysis_test.clj:114:  => '{:kind :maybe :item {:kind :named :name tahto.model.spec-xtalk-typed-fixture/User}})

tahto/typed/xtalk_analysis_test.clj:116:^{:refer tahto.typed.xtalk-analysis/check-namespace :added "4.1"}
(fact "provides namespace report examples"
  (do
    (types/clear-registry!)
tahto/typed/xtalk_analysis_test.clj:120:    (let [report (check-namespace 'tahto.model.spec-xtalk-typed-fixture)]
      {:namespace (:namespace report)
       :functions (mapv (fn [{:keys [function return errors]}]
                          {:function function
                           :return return
                           :error-tags (mapv :tag errors)})
                        (:functions report))}))
tahto/typed/xtalk_analysis_test.clj:127:  => '{:namespace tahto.model.spec-xtalk-typed-fixture
tahto/typed/xtalk_analysis_test.clj:128:       :functions [{:function tahto.model.spec-xtalk-typed-fixture/find-user
                    :return {:kind :maybe
tahto/typed/xtalk_analysis_test.clj:130:                             :item {:kind :named :name tahto.model.spec-xtalk-typed-fixture/User}}
                    :error-tags []}
tahto/typed/xtalk_analysis_test.clj:132:                   {:function tahto.model.spec-xtalk-typed-fixture/wrong-user-name
                    :return {:kind :primitive :name :xt/str}
                    :error-tags [:return-type-mismatch]}
tahto/typed/xtalk_analysis_test.clj:135:                   {:function tahto.model.spec-xtalk-typed-fixture/find-user-wrong-key
                    :return {:kind :maybe
tahto/typed/xtalk_analysis_test.clj:137:                             :item {:kind :named :name tahto.model.spec-xtalk-typed-fixture/User}}
                    :error-tags [:call-arg-type-mismatch]}]})

tahto/typed/xtalk_analysis_test.clj:140:^{:refer tahto.typed.xtalk-analysis/report-json :added "4.1"}
(fact "renders reports as json"
  [(report-json {:a 1})
   (boolean (re-find #"\n" (report-json {:a 1} true)))]
  => ["{\"a\":1}" true])