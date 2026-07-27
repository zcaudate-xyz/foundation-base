(ns tahto.runtime.basic.docker.type-twostep-ocaml-test
  (:use code.test)
  (:require [tahto.runtime.basic.impl-annex.process-ocaml]
            [tahto.runtime.basic.type-twostep :as twostep]
            [tahto.core :as l]
            [std.lib.env :as env]))

(l/script- :ocaml
  {:runtime :twostep
   :process {:force-container true
             :container {:image "ghcr.io/zcaudate-xyz/foundation-base/rt-twostep-ocaml:latest"}
             :exec-fn #'twostep/sh-exec-portable}
   :test-mode true})

(fact:global
 {:skip (or (not (env/program-exists? "docker"))
            (not (env/docker-daemon-available?))
            (System/getenv "TAHTO_NO_DOCKER"))
  :setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(fact "ocamlc twostep can return values in docker"
  [(!.ml
     (+ 1 2 3))

   (!.ml
     (* (+ 2 3) 4))]
  => [6 20])
