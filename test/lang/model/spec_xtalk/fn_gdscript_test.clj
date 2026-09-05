(ns lang.model.spec-xtalk.fn-gdscript-test
  (:use code.test)
  (:require [lang.model.spec-xtalk.fn-gdscript :refer :all]))

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-del :added "4.1"}
(fact "deletes values")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-del-key :added "4.1"}
(fact "deletes object key")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-cat :added "4.1"}
(fact "concatenates strings")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-len :added "4.1"}
(fact "gets length")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-get-key :added "4.1"}
(fact "gets object key")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-has-key? :added "4.1"}
(fact "checks object key")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-err :added "4.1"}
(fact "raises errors")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-ex-native? :added "4.1"}
(fact "checks native exceptions")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-ex-new :added "4.1"}
(fact "creates exceptions")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-ex-message :added "4.1"}
(fact "gets exception message")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-ex-data :added "4.1"}
(fact "gets exception data")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-eval :added "4.1"}
(fact "evaluates expressions")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-apply :added "4.1"}
(fact "applies arguments")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-random :added "4.1"}
(fact "generates random values")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-print :added "4.1"}
(fact "prints values")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-type-native :added "4.1"}
(fact "detects native type")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-global-has? :added "4.1"}
(fact "checks global variables")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-global-set :added "4.1"}
(fact "sets global variables")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-global-del :added "4.1"}
(fact "deletes global variables")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-abs :added "4.1"}
(fact "computes absolute value")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-acos :added "4.1"}
(fact "computes arc cosine")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-asin :added "4.1"}
(fact "computes arc sine")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-atan :added "4.1"}
(fact "computes arc tangent")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-ceil :added "4.1"}
(fact "computes ceiling")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-cos :added "4.1"}
(fact "computes cosine")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-cosh :added "4.1"}
(fact "computes hyperbolic cosine")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-exp :added "4.1"}
(fact "computes exponential")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-floor :added "4.1"}
(fact "computes floor")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-loge :added "4.1"}
(fact "computes natural logarithm")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-log10 :added "4.1"}
(fact "computes base-10 logarithm")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-max :added "4.1"}
(fact "computes maximum")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-min :added "4.1"}
(fact "computes minimum")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-mod :added "4.1"}
(fact "computes modulo")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-pow :added "4.1"}
(fact "computes power")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-quot :added "4.1"}
(fact "computes quotient")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-sin :added "4.1"}
(fact "computes sine")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-sinh :added "4.1"}
(fact "computes hyperbolic sine")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-sqrt :added "4.1"}
(fact "computes square root")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-tan :added "4.1"}
(fact "computes tangent")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-m-tanh :added "4.1"}
(fact "computes hyperbolic tangent")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-arr-push :added "4.1"}
(fact "pushes array elements")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-arr-pop :added "4.1"}
(fact "pops array elements")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-arr-slice :added "4.1"}
(fact "slices arrays")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-str-split :added "4.1"}
(fact "splits strings")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-str-replace :added "4.1"}
(fact "replaces substrings")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-str-trim :added "4.1"}
(fact "trims strings")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-str-to-lower :added "4.1"}
(fact "lowercases strings")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-str-to-upper :added "4.1"}
(fact "uppercases strings")

^{:refer lang.model.spec-xtalk.fn-gdscript/gdscript-tf-x-str-join :added "4.1"}
(fact "joins strings")
