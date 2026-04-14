(ns rt.basic.docker.impl-julia-test
  (:require [rt.basic.docker.registry :as registry]
            [rt.basic.impl-annex.process-julia]
            [rt.basic.type-common :as common]
            [std.lang :as l]
            [std.lang.base.script :as script])
  (:use code.test))

;;
;; Julia basic runtime in a Docker container.
;;
;; Uses the project-owned rt.basic Julia image with JSON preinstalled.
;;
;; The bootstrap connects back to the JVM socket server via
;; host.docker.internal:<port>, running the eval loop with JSON + Sockets.
;;
;; Image: foundation-base/rt-basic-julia:latest
;; Run with: RT_BASIC_DOCKER_TESTS=true lein test :only rt.basic.docker.impl-julia-test
;;

(def CANARY-DOCKER
  (and (common/program-exists? "docker")
       (some? (System/getenv "RT_BASIC_DOCKER_TESTS"))))

(when CANARY-DOCKER
  (script/script-ext [:jl.docker :julia]
    {:runtime :basic
     :config  (registry/registry-config :julia)}))

(fact:global
 {:setup    [(when CANARY-DOCKER (l/annex:start-all))]
  :teardown [(when CANARY-DOCKER (l/annex:stop-all))]})

^{:refer rt.basic.docker.impl-julia-test/CANARY-DOCKER :adopt true :added "4.0"}
(fact "julia :basic evaluates arithmetic expressions in docker"
  (if CANARY-DOCKER
    [(l/! [:jl.docker]
       (+ 1 2 3))

     (l/! [:jl.docker]
       (pow 3 4))

     (l/! [:jl.docker]
       (* 6 7))]
    :docker-unavailable)
  => (any [6 81 42]
          :docker-unavailable))

^{:refer rt.basic.docker.impl-julia-test/CANARY-DOCKER :adopt true :added "4.0"}
(fact "julia docker container defines and calls inline functions"
  (if CANARY-DOCKER
    [(l/! [:jl.docker]
       (do (var add-10 (fn [x] (return (+ x 10))))
           (add-10 5)))

     (l/! [:jl.docker]
       (do (var mul-xy (fn [x y] (return (* x y))))
           (mul-xy 6 7)))]
    :docker-unavailable)
  => (any [15 42]
          :docker-unavailable))

^{:refer rt.basic.docker.impl-julia-test/CANARY-DOCKER :adopt true :added "4.0"}
(fact "julia docker container handles recursive inline functions"
  (if CANARY-DOCKER
    (l/! [:jl.docker]
      (do (var fib (fn [n]
                     (if (<= n 1)
                       (return n)
                       (return (+ (fib (- n 1))
                                  (fib (- n 2)))))))
          (fib 10)))
    :docker-unavailable)
  => (any 55 :docker-unavailable))

(comment
  (l/annex:start-all)
  (l/annex:stop-all)
  (l/! [:jl.docker] (+ 1 2 3))
  )
