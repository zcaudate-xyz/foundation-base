tahto/typed/xtalk_infer_test.clj:1:(ns tahto.typed.xtalk-infer-test
  (:use code.test)
tahto/typed/xtalk_infer_test.clj:3:  (:require [tahto.typed.xtalk-common :as types]
tahto/typed/xtalk_infer_test.clj:4:            [tahto.typed.xtalk-compat :refer :all]
tahto/typed/xtalk_infer_test.clj:5:            [tahto.typed.xtalk-env :refer :all]
tahto/typed/xtalk_infer_test.clj:6:            [tahto.typed.xtalk-form :refer :all]
tahto/typed/xtalk_infer_test.clj:7:            [tahto.typed.xtalk-call :refer :all]
tahto/typed/xtalk_infer_test.clj:8:            [tahto.typed.xtalk-infer :refer :all]
tahto/typed/xtalk_infer_test.clj:9:            [tahto.typed.xtalk-ops :as ops]
tahto/typed/xtalk_infer_test.clj:10:            [tahto.typed.xtalk-parse :as parse]))

(def +ctx+ {:ns 'sample.route :aliases '{k xt.lang.common-lib} :infer infer-type})

(defn fixture-register! []
  (types/clear-registry!)
tahto/typed/xtalk_infer_test.clj:16:  (parse/register-types! (parse/analyze-namespace 'tahto.model.spec-xtalk-typed-fixture)))

(fact "attaches source locations to inference errors"
  (let [loc (-> (infer-type '(value "u1")
                            {:env '{value {:kind :primitive :name :xt/int}}
                             :ns 'sample.route
                             :aliases {}
                             :infer infer-type
                             :loc {:file "sample.clj" :line 5 :column 6}})
                :errors
                first
                :loc)]
    [(= "sample.clj" (:file loc))
     (integer? (:line loc))
     (integer? (:column loc))])
  => [true true true])

tahto/typed/xtalk_infer_test.clj:33:^{:refer tahto.typed.xtalk-infer/intrinsic-callbacks :added "4.1"}
(fact "exposes callback hooks for intrinsic dispatch"
  (set (keys (intrinsic-callbacks)))
  => '#{:result :infer-type :resolve-type :arrayify-type :infer-get-key :infer-get-path :infer-obj-assign :infer-make-container :infer-blank-container})

tahto/typed/xtalk_infer_test.clj:38:^{:refer tahto.typed.xtalk-infer/infer-type :added "4.1"}
(fact "dispatches across literals lowered forms and calls"
  [(types/type->data (:type (infer-type '(x:get-key route "id") {:env '{route {:kind :record :fields [{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}]}}
                                                           :ns 'sample.route :aliases '{k xt.lang.common-lib} :infer infer-type})))
   (types/type->data (:type (infer-type '(if true 1 2) +ctx+)))]
  => '[{:kind :primitive :name :xt/str}
        {:kind :primitive :name :xt/int}])
