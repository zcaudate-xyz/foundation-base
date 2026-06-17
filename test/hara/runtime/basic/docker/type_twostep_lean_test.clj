(ns hara.runtime.basic.docker.type-twostep-lean-test
  (:use code.test)
  (:require [hara.runtime.basic.impl-annex.process-lean]
            [hara.lang :as l]
            [std.lib.env :as env]))

(do hara.runtime.basic.impl-annex.process-lean/+lean-twostep+)

(l/script- :lean
  {:runtime :twostep
   :process {:force-container true
             :container {:image "foundation-base/rt-twostep-lean:latest"}
             :exec-fn #'rt.basic.impl-annex.process-lean/sh-exec-lean-portable}})

(fact:global {:skip (or (not (env/program-exists? "docker"))
                        (not (System/getenv "RT_TWOSTEP_DOCKER_TESTS")))})

(fact "lean twostep can return values in docker"
  [(!.lean
     (+ 1 2 3))

   (!.lean
     (* (+ 2 3) 4))]
  => [6 20])
