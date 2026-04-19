(ns rt.basic.docker.type-twostep-rustc-test
  (:use code.test)
  (:require [rt.basic.impl-annex.process-rust]
            [rt.basic.type-common :as common]
            [rt.basic.type-twostep :as twostep]
            [std.lang :as l]))

(l/script- :rust
  {:runtime :twostep
   :process {:force-container true
             :container {:image "foundation-base/rt-twostep-rust:latest"}
             :exec-fn #'twostep/sh-exec-portable}})

(def CANARY-DOCKER
  (and (common/program-exists? "docker")
       (some? (System/getenv "RT_TWOSTEP_DOCKER_TESTS"))))

(defn.rs ^{:- [:i32]}
  add-10
  [:i32 x]
  (return (+ x 10)))

(defn.rs ^{:- [:i32]}
  add-20
  [:i32 x]
  (return (+ x 20)))

(fact "rust twostep can return values in docker"
  (if CANARY-DOCKER
    [(!.rs
       (+ 1 2 3))

     (add-10 6)

     (!.rs
       (-/add-20 (-/add-10 6)))

     (!.rs
       (-/add-20 10))]
    :docker-unavailable)
  => (any [6 16 36 30]
          :docker-unavailable))
