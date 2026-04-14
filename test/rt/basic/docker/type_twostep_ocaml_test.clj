(ns rt.basic.docker.type-twostep-ocaml-test
  (:use code.test)
  (:require [rt.basic.impl-annex.process-ocaml]
            [rt.basic.type-common :as common]
            [rt.basic.type-twostep :as twostep]
            [std.lang :as l]))

(do rt.basic.impl-annex.process-ocaml/+ocaml-twostep+)

(l/script- :ocaml
  {:runtime :twostep
   :process {:force-container true
             :container {:image "foundation-base/rt-twostep-ocaml:latest"}
             :exec-fn #'twostep/sh-exec-portable}})

(def CANARY-DOCKER
  (and (common/program-exists? "docker")
       (some? (System/getenv "RT_TWOSTEP_DOCKER_TESTS"))))

(fact "ocamlc twostep can return values in docker"
  (if CANARY-DOCKER
    [(!.ml
       (+ 1 2 3))

     (!.ml
       (* (+ 2 3) 4))]
    :docker-unavailable)
  => (any [6 20]
          :docker-unavailable))
