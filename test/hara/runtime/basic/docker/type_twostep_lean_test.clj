(ns hara.runtime.basic.docker.type-twostep-lean-test
  (:use code.test)
  (:require [hara.runtime.basic.impl-annex.process-lean]
            [hara.lang :as l]
            [std.lib.env :as env]))

(l/script- :lean
  {:runtime :twostep
   :process {:force-container true
             :container {:image "foundation-base/rt-twostep-lean:latest"}
             :exec-fn #'rt.basic.impl-annex.process-lean/sh-exec-lean-portable}
   :test-mode true})


(fact:global
 {:skip (or (not (env/program-exists? "docker"))
            (not (env/docker-daemon-available?))
            (System/getenv "HARA_NO_DOCKER"))
  :setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})


(fact "lean twostep can return values in docker"
  [(!.lean
     (+ 1 2 3))

   (!.lean
     (* (+ 2 3) 4))]
  => [6 20])
