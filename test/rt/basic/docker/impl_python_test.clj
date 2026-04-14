(ns rt.basic.docker.impl-python-test
  (:require [rt.basic.docker.registry :as registry]
            [rt.basic.type-common :as common]
            [std.lang :as l]
            [std.lang.base.script :as script])
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
;; Run with: RT_BASIC_DOCKER_TESTS=true lein test :only rt.basic.docker.impl-python-test
;;

(def CANARY-DOCKER
  (and (common/program-exists? "docker")
       (some? (System/getenv "RT_BASIC_DOCKER_TESTS"))))

(when CANARY-DOCKER
  (script/script-ext [:py.docker :python]
    {:runtime :basic
     :config  (registry/registry-config :python)}))

(fact:global
 {:setup    [(when CANARY-DOCKER (l/annex:start-all))]
  :teardown [(when CANARY-DOCKER (l/annex:stop-all))]})

^{:refer rt.basic.docker.impl-python-test/CANARY-DOCKER :adopt true :added "4.0"}
(fact "python :basic evaluates arithmetic expressions in docker"
  (if CANARY-DOCKER
    [(l/! [:py.docker]
       (+ 1 2 3))

     (l/! [:py.docker]
       (* 6 7))

     (l/! [:py.docker]
       (- 100 1))]
    :docker-unavailable)
  => (any [6 42 99]
          :docker-unavailable))

^{:refer rt.basic.docker.impl-python-test/CANARY-DOCKER :adopt true :added "4.0"}
(fact "python docker container defines and calls inline functions"
  (if CANARY-DOCKER
    [(l/! [:py.docker]
       (do (var add-10 (fn [x] (return (+ x 10))))
           (add-10 5)))

     (l/! [:py.docker]
       (do (var mul-xy (fn [x y] (return (* x y))))
           (mul-xy 6 7)))]
    :docker-unavailable)
  => (any [15 42]
          :docker-unavailable))

^{:refer rt.basic.docker.impl-python-test/CANARY-DOCKER :adopt true :added "4.0"}
(fact "python docker container handles string operations"
  (if CANARY-DOCKER
    (l/! [:py.docker]
      (do (var greet (fn [name] (return (+ "hello " name))))
          (greet "world")))
    :docker-unavailable)
  => (any "hello world" :docker-unavailable))

(comment
  (l/annex:start-all)
  (l/annex:stop-all)
  (l/! [:py.docker] (+ 1 2 3))
  )
