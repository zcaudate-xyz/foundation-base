(ns rt.basic.type-twostep-ocaml-test
  (:use code.test)
  (:require [rt.basic.impl-annex.process-ocaml]
            [rt.basic.type-common :as common]
            [std.lang :as l]))

(do rt.basic.impl-annex.process-ocaml/+ocaml-twostep+)

(l/script- :ocaml
  {:runtime :twostep})

(def CANARY-OCAMLC
  (common/program-exists? "ocamlc"))

(fact "ocamlc twostep can return values"
  (if CANARY-OCAMLC
    [(!.ml
       (+ 1 2 3))

     (!.ml
       (* (+ 2 3) 4))]
    :ocamlc-unavailable)
  => (any [6 20]
          :ocamlc-unavailable))
