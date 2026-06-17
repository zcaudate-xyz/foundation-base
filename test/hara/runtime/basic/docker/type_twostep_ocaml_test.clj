(ns hara.runtime.basic.docker.type-twostep-ocaml-test
  (:use code.test)
  (:require [hara.runtime.basic.impl-annex.process-ocaml]
            [hara.runtime.basic.type-twostep :as twostep]
            [hara.lang :as l]
            [std.lib.env :as env]))

(do hara.runtime.basic.impl-annex.process-ocaml/+ocaml-twostep+)

(l/script- :ocaml
  {:runtime :twostep
   :process {:force-container true
             :container {:image "foundation-base/rt-twostep-ocaml:latest"}
             :exec-fn #'twostep/sh-exec-portable}})

(fact:global {:skip (or (not (env/program-exists? "docker"))
                        (not (System/getenv "RT_TWOSTEP_DOCKER_TESTS")))})

(fact "ocamlc twostep can return values in docker"
  [(!.ml
     (+ 1 2 3))

   (!.ml
     (* (+ 2 3) 4))]
  => [6 20])
