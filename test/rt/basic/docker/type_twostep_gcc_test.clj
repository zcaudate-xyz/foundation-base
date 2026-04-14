(ns rt.basic.docker.type-twostep-gcc-test
  (:use code.test)
  (:require [rt.basic.impl.process-c]
            [rt.basic.type-common :as common]
            [rt.basic.type-twostep :as twostep]
            [std.lang :as l]))

(l/script- :c
  {:runtime :twostep
   :process {:force-container true
             :container {:image "foundation-base/rt-twostep-c:latest"}
             :exec-fn #'twostep/sh-exec-portable}})

(def CANARY-DOCKER
  (and (common/program-exists? "docker")
       (some? (System/getenv "RT_TWOSTEP_DOCKER_TESTS"))))

(defn.c ^{:- [:int]}
  add-10
  [:int x]
  (return (+ x 10)))

(defn.c ^{:- [:int]}
  add-20
  [:int x]
  (return (+ x 20)))

(fact "gcc twostep can return values in docker"
  (if CANARY-DOCKER
    [(!.c
       (+ 1 2 3))

     (add-10 6)

     (!.c
       (-/add-20 (-/add-10 6)))

     (!.c
       (-/add-20 10))]
    :docker-unavailable)
  => (any [6 16 36 30]
          :docker-unavailable))
