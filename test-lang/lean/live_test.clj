(ns lean.live-test
  (:require [hara.lang :as l]
            [hara.model.annex.spec-lean]
            [lean.core :as y]
            [std.lib.env :as env])
  (:use code.test))

(fact:global
 {:skip (not (env/program-exists? "lean"))})

(l/script- :lean
  {:runtime :twostep
   :require [[lean.core :as y]]})

(fact "live lean prelude calls work"
  (!.lean (List.length [1 2 3 4]))
  => 4)
