(ns ocaml.live-test
  (:require [hara.lang :as l]
            [hara.model.annex.spec-ocaml]
            [ocaml.core :as y]
            [std.lib.env :as env])
  (:use code.test))

(fact:global
 {:skip (not (env/program-exists? "ocaml"))})

(l/script- :ocaml
  {:runtime :twostep
   :require [[ocaml.core :as y]]})

(fact "live ocaml prelude calls work"
  (!.ocaml (List.length [1 2 3 4]))
  => 4)
