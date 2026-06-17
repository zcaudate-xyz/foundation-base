(ns hara.runtime.basic.docker.impl-erlang-test
  (:require [hara.runtime.basic.docker.registry :as registry]
            [std.lib.env :as env]
            [hara.lang :as l]
            [hara.lang.script :as script])
  (:use code.test))

;;
;; Erlang basic runtime in a Docker container.
;;
;; Uses the OTP 27+ stdlib `json` module (json:decode/json:encode).
;; Earlier OTP versions do NOT have the stdlib json module — use erlang:27+.
;; Bootstrap connects back to the JVM via host.docker.internal:<port>.
;;
;; Image: foundation-base/rt-basic-erlang:latest
;; Run with: RT_BASIC_DOCKER_TESTS=true lein test :only hara.runtime.basic.docker.impl-erlang-test
;;

(when (and (env/program-exists? "docker")
           (System/getenv "RT_BASIC_DOCKER_TESTS"))
  (script/script-ext [:erl.docker :erlang]
    {:runtime :basic
     :config  (registry/registry-config :erlang)}))

(fact:global
 {:skip (or (not (env/program-exists? "docker"))
            (not (System/getenv "RT_BASIC_DOCKER_TESTS")))
  :setup [(l/annex:start-all)]
  :teardown [(l/annex:stop-all)]})

^{:refer :erl.docker :adopt true :added "4.0"}
(fact "erlang :basic evaluates arithmetic expressions in docker"
  [(l/! [:erl.docker]
     (+ 1 2 3))

   (l/! [:erl.docker]
     (* 6 7))

   (l/! [:erl.docker]
     (- 100 1))]
  => [6 42 99])

^{:refer :erl.docker :adopt true :added "4.0"}
(fact "erlang docker container evaluates list and stdlib operations"
  [(l/! [:erl.docker]
     (lists:sum [1 2 3 4 5]))

   (l/! [:erl.docker]
     (length [10 20 30 40 50 60 70]))]
  => [15 7])

(comment
  (l/annex:start-all)
  (l/annex:stop-all)
  (l/! [:erl.docker] (+ 1 2 3))
  )
