(ns lang.runtime.basic.impl-annex.process-circom-test
  (:require [lang.runtime.basic.impl-annex.process-circom :refer :all]
            [lang.runtime.basic.type-common :as common]
            [lang.core.registry :as registry]
            [std.lib.os :as os])
  (:use code.test))

^{:refer lang.runtime.basic.impl-annex.process-circom/sh-exec-circom :added "4.1"}
(fact "executes circom compile pipeline"
  (with-redefs [os/sh (fn [_] :ok)]
    (sh-exec-circom ["circom"] "template Main {}"
                    {:extension "circom"}))
  => string?)

^{:refer lang.core.registry/+registry+ :added "4.1"}
(fact "registers the circom twostep runtime"
  (get @registry/+registry+ [:circom :twostep])
  => 'lang.runtime.basic.impl-annex.process-circom)

^{:refer lang.runtime.basic.type-common/get-options :added "4.1"}
(fact "wires sh-exec-circom into the twostep context options"
  (get-in (common/get-options :circom :twostep :default)
          [:exec-fn])
  => #'lang.runtime.basic.impl-annex.process-circom/sh-exec-circom)
^{:refer lang.runtime.basic.impl-annex.process-circom/transform-form :added "4.1"}
(fact "wraps a bare expression as a circom component main template"
  (transform-form '[(signal x) (signal y)] nil)
  => '(:- "\ncomponent main = " (signal y) ";"))

^{:refer lang.runtime.basic.impl-annex.process-circom/transform-form :added "4.1"
  :id test-transform-form-existing-main-or-pragma}
(fact "keeps the program as-is when it already contains main or pragma"
  (transform-form '[(pragma circom "2.0.0") (main {public [x]})] nil)
  => '(do (pragma circom "2.0.0") (main {public [x]})))

^{:refer lang.runtime.basic.impl-annex.process-circom/transform-form :added "4.1"
  :id test-transform-form-single-circom-form}
(fact "normalizes a single form into a vector before wrapping"
  (transform-form '(signal x) nil)
  => '(:- "\ncomponent main = " (signal x) ";"))
