(ns rt.basic.impl-annex.process-circom-test
  (:require [rt.basic.impl-annex.process-circom :refer :all]
            [std.lib.os :as os])
  (:use code.test))

^{:refer rt.basic.impl-annex.process-circom/sh-exec-circom :added "4.1"}
(fact "executes circom compile pipeline"
  (with-redefs [os/sh (fn [_] :ok)]
    (sh-exec-circom ["circom"] "template Main {}"
                    {:extension "circom"}))
  => string?)