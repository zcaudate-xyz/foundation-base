(ns rt.basic.docker.type-twostep-lean-test
  (:use code.test)
  (:require [rt.basic.impl-annex.process-lean]
            [rt.basic.type-common :as common]
            [std.lang :as l]))

(do rt.basic.impl-annex.process-lean/+lean-twostep+)

(l/script- :lean
  {:runtime :twostep
   :process {:force-container true
             :container {:image "foundation-base/rt-twostep-lean:latest"}
             :exec-fn #'rt.basic.impl-annex.process-lean/sh-exec-lean-portable}})

(def CANARY-DOCKER
  (and (common/program-exists? "docker")
       (some? (System/getenv "RT_TWOSTEP_DOCKER_TESTS"))))

(fact "lean twostep can return values in docker"
  (if CANARY-DOCKER
    [(!.lean
       (+ 1 2 3))

     (!.lean
       (* (+ 2 3) 4))]
    :docker-unavailable)
  => (any [6 20]
          :docker-unavailable))
