tahto/runtime/basic/docker/type_twostep_haskell_test.clj:1:(ns tahto.runtime.basic.docker.type-twostep-haskell-test
  (:use code.test)
tahto/runtime/basic/docker/type_twostep_haskell_test.clj:3:  (:require [tahto.runtime.basic.impl-annex.process-haskell]
tahto/runtime/basic/docker/type_twostep_haskell_test.clj:4:            [tahto.runtime.basic.type-twostep :as twostep]
            [tahto.core :as l]
            [std.lib.env :as env]))

(l/script- :haskell
  {:runtime :twostep
   :process {:force-container true
             :container {:image "ghcr.io/zcaudate-xyz/foundation-base/rt-twostep-haskell:latest"}
             :exec-fn #'twostep/sh-exec-portable} :test-mode true})

(fact:global
 {:skip (or (not (env/program-exists? "docker"))
            (not (env/docker-daemon-available?))
tahto/runtime/basic/docker/type_twostep_haskell_test.clj:17:            (System/getenv "TAHTO_NO_DOCKER"))
  :setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(fact "ghc twostep can return values in docker"
  [(!.hs
     (+ 1 2 3))

   (!.hs
     (* (+ 2 3) 4))]
  => [6 20])
