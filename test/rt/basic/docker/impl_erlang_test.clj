(ns rt.basic.docker.impl-erlang-test
  (:require [rt.basic.docker.registry :as registry]
            [rt.basic.type-common :as common]
            [std.lang :as l]
            [std.lang.base.script :as script])
  (:use code.test))

;;
;; Erlang basic runtime in a Docker container.
;;
;; Uses the OTP 27+ stdlib `json` module (json:decode/json:encode).
;; Earlier OTP versions do NOT have the stdlib json module — use erlang:27+.
;; Bootstrap connects back to the JVM via host.docker.internal:<port>.
;;
;; Image: foundation-base/rt-basic-erlang:latest
;; Run with: RT_BASIC_DOCKER_TESTS=true lein test :only rt.basic.docker.impl-erlang-test
;;

(def CANARY-DOCKER
  (and (common/program-exists? "docker")
       (some? (System/getenv "RT_BASIC_DOCKER_TESTS"))))

(when CANARY-DOCKER
  (script/script-ext [:erl.docker :erlang]
    {:runtime :basic
     :config  (registry/registry-config :erlang)}))

(fact:global
 {:setup    [(when CANARY-DOCKER (l/annex:start-all))]
  :teardown [(when CANARY-DOCKER (l/annex:stop-all))]})

^{:refer rt.basic.docker.impl-erlang-test/CANARY-DOCKER :adopt true :added "4.0"}
(fact "erlang :basic evaluates arithmetic expressions in docker"
  (if CANARY-DOCKER
    [(l/! [:erl.docker]
       (+ 1 2 3))

     (l/! [:erl.docker]
       (* 6 7))

     (l/! [:erl.docker]
       (- 100 1))]
    :docker-unavailable)
  => (any [6 42 99]
          :docker-unavailable))

^{:refer rt.basic.docker.impl-erlang-test/CANARY-DOCKER :adopt true :added "4.0"}
(fact "erlang docker container evaluates list and stdlib operations"
  (if CANARY-DOCKER
    [(l/! [:erl.docker]
       (lists:sum [1 2 3 4 5]))

     (l/! [:erl.docker]
       (length [10 20 30 40 50 60 70]))]
    :docker-unavailable)
  => (any [15 7]
          :docker-unavailable))

(comment
  (l/annex:start-all)
  (l/annex:stop-all)
  (l/! [:erl.docker] (+ 1 2 3))
  )
