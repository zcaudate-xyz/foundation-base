(ns std.lang.model.spec-xtalk.mixer-test
  (:use code.test)
  (:require [std.lang.model.spec-xtalk.mixer :as mixer]
            [std.lang.typed.xtalk-common :as types]))

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
  (-> (mixer/mix-file "src-play/demo/typescript_model.clj")
      :functions
      first
      :name)
  => "lookup-user")
