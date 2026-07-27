(ns tahto.runtime.basic.impl_annex.process-circom-verify-test
  (:require [tahto.core :as l]
            [std.lib.context.space :as space]
            [std.lib.env :as env])
  (:use code.test))

(l/script :circom
  tahto.runtime.basic.impl_annex.process-circom-verify-test
  {:runtime :verify})

^{:refer tahto.runtime.basic.impl_annex.process-circom/CANARY :added "4.0"}
(fact "starts the circom verify runtime in the test namespace"
  (space/space:rt-active (env/ns-sym))
  => (contains [:lang/circom]))

(fact:global
 {:skip (not (env/program-exists? "circom"))})

^{:refer tahto.runtime.basic.impl_annex.process-circom/!.circom :added "4.0"}
(fact "validates a simple circom expression through the runtime"
  (do (defn.circom Multiplier [] (signal input a) (signal input b) (signal output c) (<== c (* a b)))
      (string? (!.circom (Multiplier))))
  => true)
