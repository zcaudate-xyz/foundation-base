(ns hara.runtime.basic.type-twostep-ocaml-test
  (:use code.test)
  (:require [std.lib.env :as env]
            [hara.runtime.basic.impl-annex.process-ocaml]
            [hara.lang :as l]))

(do hara.runtime.basic.impl-annex.process-ocaml/+ocaml-twostep+)

(l/script- :ocaml
  {:runtime :twostep})

(fact:global
 {:skip (not (env/program-exists? "ocamlc"))})

(fact "ocamlc twostep can return values"
  [(!.ml
     (+ 1 2 3))

   (!.ml
     (* (+ 2 3) 4))]
  => [6 20])
