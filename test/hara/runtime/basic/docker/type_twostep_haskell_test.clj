(ns hara.runtime.basic.docker.type-twostep-haskell-test
  (:use code.test)
  (:require [hara.runtime.basic.impl-annex.process-haskell]
            [hara.runtime.basic.type-twostep :as twostep]
            [hara.lang :as l]
            [std.lib.env :as env]))

(do hara.runtime.basic.impl-annex.process-haskell/+haskell-twostep+)

(l/script- :haskell
  {:runtime :twostep
   :process {:force-container true
             :container {:image "foundation-base/rt-twostep-haskell:latest"}
             :exec-fn #'twostep/sh-exec-portable}})

(fact:global {:skip (or (not (env/program-exists? "docker"))
                        (not (System/getenv "RT_TWOSTEP_DOCKER_TESTS")))})

(fact "ghc twostep can return values in docker"
  [(!.hs
     (+ 1 2 3))

   (!.hs
     (* (+ 2 3) 4))]
  => [6 20])
