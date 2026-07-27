(ns tahto.model.spec-xtalk.fn-gdscript-test
  (:use code.test)
  (:require [tahto.model.spec-xtalk.fn-gdscript :refer :all]))

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-del :added "4.1"}
(fact "deletes values")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-del-key :added "4.1"}
(fact "deletes object key")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-cat :added "4.1"}
(fact "concatenates strings")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-len :added "4.1"}
(fact "gets length")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-get-key :added "4.1"}
(fact "gets object key")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-has-key? :added "4.1"}
(fact "checks object key")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-err :added "4.1"}
(fact "raises errors")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-ex-native? :added "4.1"}
(fact "checks native exceptions")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-ex-new :added "4.1"}
(fact "creates exceptions")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-ex-message :added "4.1"}
(fact "gets exception message")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-ex-data :added "4.1"}
(fact "gets exception data")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-eval :added "4.1"}
(fact "evaluates expressions")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-apply :added "4.1"}
(fact "applies arguments")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-random :added "4.1"}
(fact "generates random values")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-print :added "4.1"}
(fact "prints values")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-type-native :added "4.1"}
(fact "detects native type")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-global-has? :added "4.1"}
(fact "checks global variables")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-global-set :added "4.1"}
(fact "sets global variables")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-global-del :added "4.1"}
(fact "deletes global variables")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-abs :added "4.1"}
(fact "computes absolute value")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-acos :added "4.1"}
(fact "computes arc cosine")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-asin :added "4.1"}
(fact "computes arc sine")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-atan :added "4.1"}
(fact "computes arc tangent")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-ceil :added "4.1"}
(fact "computes ceiling")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-cos :added "4.1"}
(fact "computes cosine")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-cosh :added "4.1"}
(fact "computes hyperbolic cosine")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-exp :added "4.1"}
(fact "computes exponential")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-floor :added "4.1"}
(fact "computes floor")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-loge :added "4.1"}
(fact "computes natural logarithm")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-log10 :added "4.1"}
(fact "computes base-10 logarithm")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-max :added "4.1"}
(fact "computes maximum")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-min :added "4.1"}
(fact "computes minimum")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-mod :added "4.1"}
(fact "computes modulo")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-pow :added "4.1"}
(fact "computes power")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-quot :added "4.1"}
(fact "computes quotient")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-sin :added "4.1"}
(fact "computes sine")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-sinh :added "4.1"}
(fact "computes hyperbolic sine")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-sqrt :added "4.1"}
(fact "computes square root")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-tan :added "4.1"}
(fact "computes tangent")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-tanh :added "4.1"}
(fact "computes hyperbolic tangent")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-arr-push :added "4.1"}
(fact "pushes array elements")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-arr-pop :added "4.1"}
(fact "pops array elements")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-arr-slice :added "4.1"}
(fact "slices arrays")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-str-split :added "4.1"}
(fact "splits strings")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-str-replace :added "4.1"}
(fact "replaces substrings")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-str-trim :added "4.1"}
(fact "trims strings")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-str-to-lower :added "4.1"}
(fact "lowercases strings")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-str-to-upper :added "4.1"}
(fact "uppercases strings")

^{:refer tahto.model.spec-xtalk.fn-gdscript/gdscript-tf-x-str-join :added "4.1"}
(fact "joins strings")
