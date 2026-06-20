(ns hara.runtime.basic.impl-annex.process-circom-test
  (:require [hara.runtime.basic.impl-annex.process-circom :refer :all]
            [hara.runtime.basic.type-common :as common]
            [hara.lang.registry :as registry]
            [std.lib.os :as os])
  (:use code.test))

^{:refer hara.runtime.basic.impl-annex.process-circom/sh-exec-circom :added "4.1"}
(fact "executes circom compile pipeline"
  (with-redefs [os/sh (fn [_] :ok)]
    (sh-exec-circom ["circom"] "template Main {}"
                    {:extension "circom"}))
  => string?)

^{:refer hara.lang.registry/+registry+ :added "4.1"}
(fact "registers the circom twostep runtime"
  (get @registry/+registry+ [:circom :twostep])
  => 'hara.runtime.basic.impl-annex.process-circom)

^{:refer hara.runtime.basic.type-common/get-options :added "4.1"}
(fact "wires sh-exec-circom into the twostep context options"
  (get-in (common/get-options :circom :twostep :default)
          [:exec-fn])
  => #'hara.runtime.basic.impl-annex.process-circom/sh-exec-circom)
^{:refer hara.runtime.basic.impl-annex.process-circom/transform-form :added "4.1"}
(fact "TODO")
