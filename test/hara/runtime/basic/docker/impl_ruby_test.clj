(ns hara.runtime.basic.docker.impl-ruby-test
  (:require [hara.runtime.basic.docker.registry :as registry]
            [std.lib.env :as env]
            [hara.lang :as l]
            [hara.lang.script :as script])
  (:use code.test))

;;
;; Ruby basic runtime in a Docker container.
;;
;; Uses stdlib `socket` + `json` — no extra packages needed.
;; Bootstrap connects back to the JVM via host.docker.internal:<port>.
;;
;; Image: foundation-base/rt-basic-ruby:latest
;; Run with: RT_BASIC_DOCKER_TESTS=true lein test :only hara.runtime.basic.docker.impl-ruby-test
;;

(when (and (env/program-exists? "docker")
           (System/getenv "RT_BASIC_DOCKER_TESTS"))
  (script/script-ext [:rb.docker :ruby]
    {:runtime :basic
     :config  (registry/registry-config :ruby)}))

(fact:global
 {:skip (or (not (env/program-exists? "docker"))
            (not (System/getenv "RT_BASIC_DOCKER_TESTS")))
  :setup [(l/annex:start-all)]
  :teardown [(l/annex:stop-all)]})

^{:refer :rb.docker :adopt true :added "4.0"}
(fact "ruby :basic evaluates arithmetic expressions in docker"
  [(l/! [:rb.docker]
     (+ 1 2 3))

   (l/! [:rb.docker]
     (* 6 7))

   (l/! [:rb.docker]
     (- 100 1))]
  => [6 42 99])

^{:refer :rb.docker :adopt true :added "4.0"}
(fact "ruby docker container defines and calls inline functions"
  [(l/! [:rb.docker]
     (do (defn add-10 [x] (return (+ x 10)))
         (add-10 5)))

   (l/! [:rb.docker]
     (do (defn mul-xy [x y] (return (* x y)))
         (mul-xy 6 7)))]
  => [15 42])

^{:refer :rb.docker :adopt true :added "4.0"}
(fact "ruby docker container handles string operations"
  [(l/! [:rb.docker]
     (+ "hello " "world"))

   (l/! [:rb.docker]
     (do (defn greet [name] (return (+ "hi " name)))
         (greet "ruby")))]
  => ["hello world" "hi ruby"])

(comment
  (l/annex:start-all)
  (l/annex:stop-all)
  (l/! [:rb.docker] (+ 1 2 3))
  )
