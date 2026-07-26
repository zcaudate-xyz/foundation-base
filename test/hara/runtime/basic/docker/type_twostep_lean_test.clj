tahto/runtime/basic/docker/type_twostep_lean_test.clj:1:(ns tahto.runtime.basic.docker.type-twostep-lean-test
  (:use code.test)
tahto/runtime/basic/docker/type_twostep_lean_test.clj:3:  (:require [tahto.runtime.basic.impl-annex.process-lean]
            [tahto.core :as l]
            [std.lib.env :as env]))

(l/script- :lean
  {:runtime :twostep
   :process {:force-container true
             :container {:image "ghcr.io/zcaudate-xyz/foundation-base/rt-twostep-lean:latest"}
             :exec-fn #'rt.basic.impl-annex.process-lean/sh-exec-lean-portable}
   :test-mode true})


(fact:global
 {:skip (or (not (env/program-exists? "docker"))
            (not (env/docker-daemon-available?))
tahto/runtime/basic/docker/type_twostep_lean_test.clj:18:            (System/getenv "TAHTO_NO_DOCKER"))
  :setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})


^{:timeout 60000}
(fact "lean twostep can return values in docker"
  [(!.lean
     (+ 1 2 3))

   (!.lean
     (* (+ 2 3) 4))]
  => [6 20])
