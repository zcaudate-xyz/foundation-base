(ns hara.runtime.basic.docker.impl-julia-test
  (:require [hara.runtime.basic.docker.registry :as registry]
            [hara.runtime.basic.impl-annex.process-julia]
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
;; Image: foundation-base/rt-basic-julia:latest
;; Run with: RT_BASIC_DOCKER_TESTS=true lein test :only hara.runtime.basic.docker.impl-julia-test
;;

(when (and (env/program-exists? "docker")
           (System/getenv "RT_BASIC_DOCKER_TESTS"))
  (script/script-ext [:jl.docker :julia]
    {:runtime :basic
     :config  (registry/registry-config :julia)}))

(fact:global
 {:skip (or (not (env/program-exists? "docker"))
            (not (System/getenv "RT_BASIC_DOCKER_TESTS")))
  :setup [(l/annex:start-all)]
  :teardown [(l/annex:stop-all)]})

^{:refer :jl.docker :adopt true :added "4.0"}
(fact "julia :basic evaluates arithmetic expressions in docker"
  [(l/! [:jl.docker]
     (+ 1 2 3))

   (l/! [:jl.docker]
     (pow 3 4))

   (l/! [:jl.docker]
     (* 6 7))]
  => [6 81 42])

^{:refer :jl.docker :adopt true :added "4.0"}
(fact "julia docker container defines and calls inline functions"
  [(l/! [:jl.docker]
     (do (var add-10 (fn [x] (return (+ x 10))))
         (add-10 5)))

   (l/! [:jl.docker]
     (do (var mul-xy (fn [x y] (return (* x y))))
         (mul-xy 6 7)))]
  => [15 42])

^{:refer :jl.docker :adopt true :added "4.0"}
(fact "julia docker container handles recursive inline functions"
  (l/! [:jl.docker]
    (do (var fib (fn [n]
                   (if (<= n 1)
                     (return n)
                     (return (+ (fib (- n 1))
                                (fib (- n 2)))))))
        (fib 10)))
  => 55)

(comment
  (l/annex:start-all)
  (l/annex:stop-all)
  (l/! [:jl.docker] (+ 1 2 3))
  )
