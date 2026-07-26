tahto/runtime/basic/docker/type_twostep_rustc_test.clj:1:(ns tahto.runtime.basic.docker.type-twostep-rustc-test
  (:use code.test)
tahto/runtime/basic/docker/type_twostep_rustc_test.clj:3:  (:require [tahto.runtime.basic.impl-annex.process-rust]
tahto/runtime/basic/docker/type_twostep_rustc_test.clj:4:            [tahto.runtime.basic.type-twostep :as twostep]
            [tahto.core :as l]
            [std.lib.env :as env]))

(l/script- :rust
  {:runtime :twostep
   :process {:force-container true
             :container {:image "ghcr.io/zcaudate-xyz/foundation-base/rt-twostep-rust:latest"}
             :exec-fn #'twostep/sh-exec-portable}
   :test-mode true})

(fact:global
 {:skip (or (not (env/program-exists? "docker"))
tahto/runtime/basic/docker/type_twostep_rustc_test.clj:17:            (System/getenv "TAHTO_NO_DOCKER"))
  :setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(defn.rs ^{:- [:i32]}
  add-10
  [:i32 x]
  (return (+ x 10)))

(defn.rs ^{:- [:i32]}
  add-20
  [:i32 x]
  (return (+ x 20)))


(fact "rust twostep can return values in docker"
  [(!.rs
     (+ 1 2 3))
   
   (add-10 6)
   
   (!.rs
     (-/add-20 (-/add-10 6)))
   
   (!.rs
     (-/add-20 10))]
  => [6 16 36 30])
