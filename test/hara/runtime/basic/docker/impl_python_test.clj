tahto/runtime/basic/docker/impl_python_test.clj:1:(ns tahto.runtime.basic.docker.impl-python-test
tahto/runtime/basic/docker/impl_python_test.clj:2:  (:require [tahto.runtime.basic.docker.registry :as registry]
            [std.lib.env :as env]
            [tahto.core :as l]
            [tahto.core.script :as script])
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
;; Image: ghcr.io/zcaudate-xyz/foundation-base/rt-basic-python:latest
tahto/runtime/basic/docker/impl_python_test.clj:18:;; Run with: RT_BASIC_DOCKER_TESTS=true lein test :only tahto.runtime.basic.docker.impl-python-test
;;

(l/script- :python
  {:runtime :basic
   :config  (registry/registry-config :python)
   :test-mode true})

(fact:global
 {:skip (or (not (env/program-exists? "docker"))
            (not (env/docker-daemon-available?))
tahto/runtime/basic/docker/impl_python_test.clj:29:            (System/getenv "TAHTO_NO_DOCKER"))
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
