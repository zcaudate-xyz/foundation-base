(ns hara.typed.xtalk-call-test
  (:use code.test)
  (:require [hara.typed.xtalk-call :refer :all]
            [hara.typed.xtalk-common :as types]
            [hara.typed.xtalk-compat :refer [result]]
            [hara.typed.xtalk-infer :refer [infer-type]]
            [hara.typed.xtalk-ops :as ops]))

(def +ctx+ {:ns 'sample.route :aliases '{k xt.lang.common-lib} :infer infer-type})

^{:refer hara.typed.xtalk-call/infer-op-spec-form :added "4.1"}
(fact "infers result type from builtin op-spec for a given call form"
  [(types/type->data
    (:type (infer-op-spec-form (ops/canonical-entry 'x:add)
                               '(x:add 1 2)
                               +ctx+)))
   (-> (infer-op-spec-form (ops/canonical-entry 'x:arr-push)
                           '(x:arr-push items "a")
                           {:env '{items {:kind :array
                                          :item {:kind :primitive :name :xt/str}}}
                            :ns 'sample.route
                            :aliases {} :infer infer-type})
       :type
       types/type->data)]
  => [{:kind :primitive :name :xt/num}
      '{:kind :array
        :item {:kind :primitive :name :xt/str}}])

^{:refer hara.typed.xtalk-call/wildcard-callable? :added "4.1"}
(fact "recognizes wildcard-callable types"
  [(wildcard-callable? types/+unknown-type+ +ctx+)
   (wildcard-callable? {:kind :maybe :item {:kind :primitive :name :xt/any}} +ctx+)
   (wildcard-callable? {:kind :primitive :name :xt/str} +ctx+)]
  => [true true false])

^{:refer hara.typed.xtalk-call/callable-types :added "4.1"}
(fact "extracts callable members from unions"
  (mapv types/type->data
        (callable-types {:kind :union
                         :types [{:kind :fn :inputs [types/+str-type+] :output types/+bool-type+}
                                 {:kind :primitive :name :xt/int}]}
                        +ctx+))
  => '[{:kind :fn
         :inputs [{:kind :primitive :name :xt/str}]
         :output {:kind :primitive :name :xt/bool}}])

^{:refer hara.typed.xtalk-call/call-arg-errors :added "4.1"}
(fact "reports call arg mismatches"
  (call-arg-errors [(result types/+int-type+) (result types/+str-type+)]
                   [types/+str-type+ types/+str-type+]
                   '[1 "x"]
                   +ctx+)
  => '[{:tag :call-arg-type-mismatch
         :form 1
         :expected {:kind :primitive :name :xt/str}
         :actual {:kind :primitive :name :xt/int}}])

^{:refer hara.typed.xtalk-call/optional-arity? :added "4.1"}
(fact "accepts missing optional trailing args"
  [(optional-arity? [types/+str-type+ {:kind :maybe :item types/+int-type+}] 1 +ctx+)
   (optional-arity? [types/+str-type+ types/+int-type+] 1 +ctx+)]
  => [true false])

^{:refer hara.typed.xtalk-call/infer-function-call :added "4.1"}
(fact "infers callable outputs and call errors"
  [(types/type->data (:type (infer-function-call '(handler "u1") {:env '{handler {:kind :fn :inputs [{:kind :primitive :name :xt/str}] :output {:kind :primitive :name :xt/bool}}}
                                                           :ns 'sample.route :aliases {} :infer infer-type})))
   (-> (infer-function-call '(value "u1") {:env '{value {:kind :primitive :name :xt/int}} :ns 'sample.route :aliases {} :infer infer-type}) :errors first :tag)]
  => '[{:kind :primitive :name :xt/bool}
        :not-callable])

^{:refer hara.typed.xtalk-call/infer-builtin-form :added "4.1"}
(fact "dispatches builtin inference rules"
  [(types/type->data (:type (infer-builtin-form (ops/canonical-entry 'x:get-key)
                                                '(x:get-key route "id")
                                                {:env '{route {:kind :record :fields [{:name "id" :type {:kind :primitive :name :xt/str} :optional? false}]}}
                                                 :ns 'sample.route :aliases {} :infer infer-type})))
   (types/type->data (:type (infer-builtin-form (ops/canonical-entry 'x:add)
                                                '(x:add 1 2)
                                                +ctx+)))
   (:tag (first (:errors (infer-builtin-form (ops/canonical-entry 'x:add)
                                             '(x:add 1)
                                             +ctx+))))]
  => '[{:kind :primitive :name :xt/str}
        {:kind :primitive :name :xt/num}
        :call-arity-mismatch])


^{:refer hara.typed.xtalk-call/rest-input-type? :added "4.1"}
(fact "recognizes only explicitly variadic input types"
  [(rest-input-type? {:kind :array :item types/+str-type+ :rest true})
   (rest-input-type? {:kind :array :item types/+str-type+})
   (rest-input-type? nil)]
  => [true false false])

^{:refer hara.typed.xtalk-call/split-call-inputs :added "4.1"}
(fact "separates the final variadic input from fixed inputs"
  (let [rest-type {:kind :array :item types/+str-type+ :rest true}]
    (split-call-inputs [types/+int-type+ rest-type]))
  => {:fixed [types/+int-type+]
      :rest {:kind :array :item types/+str-type+ :rest true}}
  (split-call-inputs [types/+int-type+ types/+str-type+])
  => {:fixed [types/+int-type+ types/+str-type+] :rest nil})

^{:refer hara.typed.xtalk-call/call-arity? :added "4.1"}
(fact "accepts required, optional, and variadic call arities"
  (let [optional {:kind :maybe :item types/+str-type+}
        rest-type {:kind :array :item types/+str-type+ :rest true}]
    [(call-arity? [types/+int-type+ optional] 1 +ctx+)
     (call-arity? [types/+int-type+ optional] 0 +ctx+)
     (call-arity? [types/+int-type+ rest-type] 3 +ctx+)
     (call-arity? [types/+int-type+] 2 +ctx+)])
  => [true false true false])

^{:refer hara.typed.xtalk-call/expected-call-inputs :added "4.1"}
(fact "expands a variadic item type for every extra argument"
  (let [rest-type {:kind :array :item types/+str-type+ :rest true}]
    (expected-call-inputs [types/+int-type+ rest-type] 4))
  => [types/+int-type+ types/+str-type+ types/+str-type+ types/+str-type+]
  (expected-call-inputs [types/+int-type+ types/+str-type+] 1)
  => [types/+int-type+])

^{:refer hara.typed.xtalk-call/expected-call-arity :added "4.1"}
(fact "reports fixed arity numerically and variadic arity structurally"
  (let [optional {:kind :maybe :item types/+str-type+}
        rest-type {:kind :array :item types/+str-type+ :rest true}]
    [(expected-call-arity [types/+int-type+ optional] +ctx+)
     (expected-call-arity [types/+int-type+ optional rest-type] +ctx+)])
  => [2 {:min 1 :fixed 2 :variadic true}])
