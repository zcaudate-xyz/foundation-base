(ns hara.runtime.basic.docker.impl-julia-test
  (:require [hara.runtime.basic.docker.registry :as registry]
            [std.lib.env :as env]
            [hara.lang :as l]
            [hara.lang.script :as script])
  (:use code.test))

;;
;; Julia basic runtime in a Docker container.
;;
;; Uses the project-owned hara.runtime.basic Julia image with JSON preinstalled.
;;
;; The bootstrap connects back to the JVM socket server via
;; host.docker.internal:<port>, running the eval loop with JSON + Sockets.
;;
;; Image: ghcr.io/zcaudate-xyz/foundation-base/rt-basic-julia:latest
;; Run with: RT_BASIC_DOCKER_TESTS=true lein test :only hara.runtime.basic.docker.impl-julia-test
;;

(l/script- :julia
  {:runtime :basic
   :config  (registry/registry-config :julia)
   :test-mode true})

(fact:global
 {:skip (or (not (env/program-exists? "docker"))
            (not (env/docker-daemon-available?))
            (System/getenv "HARA_NO_DOCKER"))
  :setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer julia/vectors :adopt true :added "4.0"}
(fact "julia :basic evaluates arithmetic expressions in docker"
  [(!.jl
     (+ 1 2 3))

   (!.jl
     (pow 3 4))

   (!.jl
     (* 6 7))]
  => [6 81 42])

^{:refer julia/functions :adopt true :added "4.0"}
(fact "julia docker container defines and calls inline functions"
  [(!.jl
     (do (var add-10 (fn [x] (return (+ x 10))))
         (add-10 5)))

   (!.jl
     (do (var mul-xy (fn [x y] (return (* x y))))
         (mul-xy 6 7)))]
  => [15 42])

^{:refer julia/recursive :adopt true :added "4.0"}
(fact "julia docker container handles recursive inline functions"
  (!.jl
    (do (var fib (fn [n]
                   (if (<= n 1)
                     (return n)
                     (return (+ (fib (- n 1))
                                (fib (- n 2)))))))
        (fib 10)))
  => 55)

(comment
  (l/rt:restart)
  (l/rt:stop)
  (!.jl (+ 1 2 3))
  )
