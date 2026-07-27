(ns tahto.runtime.basic.type-twostep-ocaml-test
  (:use code.test)
  (:require [std.lib.env :as env]
            [tahto.runtime.basic.impl-annex.process-ocaml]
            [tahto.core :as l]))

(do tahto.runtime.basic.impl-annex.process-ocaml/+ocaml-twostep+)

(l/script- :ocaml
  {:runtime :twostep :test-mode true})

(fact:global {:skip (not (env/program-exists? "ocamlc")) :setup [(l/rt:restart)] :teardown [(l/rt:stop)]})

(fact "ocamlc twostep can return values"
  [(!.ml
     (+ 1 2 3))

   (!.ml
     (* (+ 2 3) 4))]
  => [6 20])
