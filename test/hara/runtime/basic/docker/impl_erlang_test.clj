(ns hara.runtime.basic.docker.impl-erlang-test
  (:use code.test)
  (:require [hara.runtime.basic.docker.registry :as registry]
            [std.lib.env :as env]
            [hara.lang :as l]
            [hara.lang.script :as script]))

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

(l/script- :erlang
  {:runtime :basic
   :config  (registry/registry-config :erlang)
   :test-mode true})

(fact:global
 {:skip (or (not (env/program-exists? "docker"))
            (not (env/docker-daemon-available?))
            (System/getenv "HARA_NO_DOCKER"))
  :setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer erlang/vectors :adopt true :added "4.0"}
(fact "erlang :basic evaluates arithmetic expressions in docker"
  [(!.erl
     (+ 1 2 3))
   
   (!.erl
     (* 6 7))

   (!.erl
     (- 100 1))]
  => [6 42 99])

^{:refer erlang/lists :adopt true :added "4.0"}
(fact "erlang docker container evaluates list and stdlib operations"
  [(!.erl
     (lists:sum [1 2 3 4 5]))

   (!.erl
     (length [10 20 30 40 50 60 70]))]
  => [15 7])

(comment
  (l/rt:restart)
  (l/rt:stop)
  (!.erl (+ 1 2 3))
  )
