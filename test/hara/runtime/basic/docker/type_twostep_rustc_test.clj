(ns hara.runtime.basic.docker.type-twostep-rustc-test
  (:use code.test)
  (:require [hara.runtime.basic.impl-annex.process-rust]
            [hara.runtime.basic.type-twostep :as twostep]
            [hara.lang :as l]
            [std.lib.env :as env]))

(l/script- :rust
  {:runtime :twostep
   :process {:force-container true
             :container {:image "ghcr.io/zcaudate-xyz/foundation-base/rt-twostep-rust:latest"}
             :exec-fn #'twostep/sh-exec-portable}
   :test-mode true})

(fact:global
 {:skip (or (not (env/program-exists? "docker"))
            (System/getenv "HARA_NO_DOCKER"))
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
