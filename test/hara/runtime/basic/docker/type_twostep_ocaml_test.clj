(ns hara.runtime.basic.docker.type-twostep-ocaml-test
  (:use code.test)
  (:require [hara.runtime.basic.impl-annex.process-ocaml]
            [hara.runtime.basic.type-twostep :as twostep]
            [hara.lang :as l]
            [std.lib.env :as env]))

(l/script- :ocaml
  {:runtime :twostep
   :process {:force-container true
             :container {:image "foundation-base/rt-twostep-ocaml:latest"}
             :exec-fn #'twostep/sh-exec-portable}
   :test-mode true})

(fact:global
 {:skip (or (not (env/program-exists? "docker"))
            (System/getenv "HARA_NO_DOCKER"))
  :setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(fact "ocamlc twostep can return values in docker"
  [(!.ml
     (+ 1 2 3))

   (!.ml
     (* (+ 2 3) 4))]
  => [6 20])
