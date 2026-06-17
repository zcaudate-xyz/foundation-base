(ns hara.runtime.basic.docker.impl-python-test
  (:require [hara.runtime.basic.docker.registry :as registry]
            [std.lib.env :as env]
            [hara.lang :as l]
            [hara.lang.script :as script])
  (:use code.test))

;;
;; Python basic runtime in a Docker container.
;;
;; The JVM opens a socket server on a random port. The container runs
;; `python3 -c <bootstrap>` where bootstrap connects back to the JVM
;; via host.docker.internal:<port> and enters the eval loop.
;;
;; No extra packages required — stdlib `socket` + `json` only.
;;
;; Image: foundation-base/rt-basic-python:latest
;; Run with: RT_BASIC_DOCKER_TESTS=true lein test :only hara.runtime.basic.docker.impl-python-test
;;

(when (and (env/program-exists? "docker")
           (System/getenv "RT_BASIC_DOCKER_TESTS"))
  (script/script-ext [:py.docker :python]
    {:runtime :basic
     :config  (registry/registry-config :python)}))

(fact:global
 {:skip (or (not (env/program-exists? "docker"))
            (not (System/getenv "RT_BASIC_DOCKER_TESTS")))
  :setup [(l/annex:start-all)]
  :teardown [(l/annex:stop-all)]})

^{:refer :py.docker :adopt true :added "4.0"}
(fact "python :basic evaluates arithmetic expressions in docker"
  [(l/! [:py.docker]
     (+ 1 2 3))

   (l/! [:py.docker]
     (* 6 7))

   (l/! [:py.docker]
     (- 100 1))]
  => [6 42 99])

^{:refer :py.docker :adopt true :added "4.0"}
(fact "python docker container defines and calls inline functions"
  [(l/! [:py.docker]
     (do (var add-10 (fn [x] (return (+ x 10))))
         (add-10 5)))

   (l/! [:py.docker]
     (do (var mul-xy (fn [x y] (return (* x y))))
         (mul-xy 6 7)))]
  => [15 42])

^{:refer :py.docker :adopt true :added "4.0"}
(fact "python docker container handles string operations"
  (l/! [:py.docker]
    (do (var greet (fn [name] (return (+ "hello " name))))
        (greet "world")))
  => "hello world")

(comment
  (l/annex:start-all)
  (l/annex:stop-all)
  (l/! [:py.docker] (+ 1 2 3))
  )
