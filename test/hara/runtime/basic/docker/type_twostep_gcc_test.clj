tahto/runtime/basic/docker/type_twostep_gcc_test.clj:1:(ns tahto.runtime.basic.docker.type-twostep-gcc-test
  (:use code.test)
tahto/runtime/basic/docker/type_twostep_gcc_test.clj:3:  (:require [tahto.runtime.basic.impl.process-c]
tahto/runtime/basic/docker/type_twostep_gcc_test.clj:4:            [tahto.runtime.basic.type-twostep :as twostep]
            [tahto.core :as l]
            [std.lib.env :as env]))

(l/script- :c
  {:runtime :twostep
   :process {:force-container true
             :container {:image "ghcr.io/zcaudate-xyz/foundation-base/rt-twostep-c:latest"}
             :exec-fn #'twostep/sh-exec-portable}
   :test-mode true})

(fact:global
 {:skip (or (not (env/program-exists? "docker"))
tahto/runtime/basic/docker/type_twostep_gcc_test.clj:17:            (System/getenv "TAHTO_NO_DOCKER"))
  :setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(defn.c ^{:- [:int]}
  add-10
  [:int x]
  (return (+ x 10)))

(defn.c ^{:- [:int]}
  add-20
  [:int x]
  (return (+ x 20)))

(fact "gcc twostep can return values in docker"
  [(!.c
     (+ 1 2 3))
   
   (add-10 6)
   
   (!.c
     (-/add-20 (-/add-10 6)))

   (!.c
     (-/add-20 10))]
  => [6 16 36 30])
