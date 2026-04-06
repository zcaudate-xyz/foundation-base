(ns std.lang.model.spec-xtalk.mixer-test
  (:use code.test)
  (:require [std.lang.model.spec-xtalk.mixer :as mixer]
             [std.lang.typed.xtalk-common :as types]))

(def +typescript-model-fixture+
  "test/std/lang/model/typescript_model_fixture.clj")

^{:refer std.lang.model.spec-xtalk.mixer/mix-namespace :added "4.1"}
(fact "mixes same-name callable specs for typed targets"
  (let [analysis (mixer/mix-namespace 'std.lang.model.spec-xtalk-typed-fixture)
        fn-def (some #(when (= "find-user" (:name %)) %)
                     (:functions analysis))]
    {:spec-count (count (:specs analysis))
     :input-types (mapv (comp types/type->data :type) (:inputs fn-def))
     :output (types/type->data (:output fn-def))})
  => '{:spec-count 3
       :input-types [{:kind :named :name std.lang.model.spec-xtalk-typed-fixture/UserMap}
                     {:kind :primitive :name :xt/str}]
       :output {:kind :maybe
                :item {:kind :named :name std.lang.model.spec-xtalk-typed-fixture/User}}})

^{:refer std.lang.model.spec-xtalk.mixer/mix-file :added "4.1"}
(fact "mixes analysis from file paths for sidecar emitters"
  (-> (mixer/mix-file +typescript-model-fixture+)
      :functions
      first
      :name)
  => "lookup-user")


^{:refer std.lang.model.spec-xtalk.mixer/mix-analysis :added "4.1"}
(fact "attaches specs to an analyzed namespace map"
  (let [analysis (mixer/mix-namespace 'std.lang.model.spec-xtalk-typed-fixture)]
    (map? analysis))
  => true

  (let [raw (mixer/mix-namespace 'std.lang.model.spec-xtalk-typed-fixture)]
    (contains? raw :functions))
  => true)