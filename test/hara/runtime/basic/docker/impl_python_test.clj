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

(l/script- :python
  {:runtime :basic
   :config  (registry/registry-config :python)
   :test-mode true})

(fact:global
 {:skip (or (not (env/program-exists? "docker"))
            (System/getenv "HARA_NO_DOCKER"))
  :setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer python/vectors :adopt true :added "4.0"}
(fact "python :basic evaluates arithmetic expressions in docker"
  [(!.py
     (+ 1 2 3))

   (!.py
     (* 6 7))

   (!.py
     (- 100 1))]
  => [6 42 99])

^{:refer python/functions :adopt true :added "4.0"}
(fact "python docker container defines and calls inline functions"
  [(!.py
     (do (var add-10 (fn [x] (return (+ x 10))))
         (add-10 5)))

   (!.py
     (do (var mul-xy (fn [x y] (return (* x y))))
         (mul-xy 6 7)))]
  => [15 42])

^{:refer python/strings :adopt true :added "4.0"}
(fact "python docker container handles string operations"
  (!.py
    (do (var greet (fn [name] (return (+ "hello " name))))
        (greet "world")))
  => "hello world")

(comment
  (l/rt:restart)
  (l/rt:stop)
  (!.py (+ 1 2 3))
  )
