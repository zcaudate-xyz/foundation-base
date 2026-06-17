(ns hara.runtime.basic.docker.impl-php-test
  (:require [hara.runtime.basic.docker.registry :as registry]
            [std.lib.env :as env]
            [hara.lang :as l]
            [hara.lang.script :as script])
  (:use code.test))

;;
;; PHP basic runtime in a Docker container.
;;
;; Uses the project-owned hara.runtime.basic PHP CLI image.
;; Bootstrap connects back to the JVM via host.docker.internal:<port>.
;;
;; Image: foundation-base/rt-basic-php:latest
;; Run with: RT_BASIC_DOCKER_TESTS=true lein test :only hara.runtime.basic.docker.impl-php-test
;;

(when (and (env/program-exists? "docker")
           (System/getenv "RT_BASIC_DOCKER_TESTS"))
  (script/script-ext [:php.docker :php]
    {:runtime :basic
     :config  (registry/registry-config :php)}))

(fact:global
 {:skip (or (not (env/program-exists? "docker"))
            (not (System/getenv "RT_BASIC_DOCKER_TESTS")))
  :setup [(l/annex:start-all)]
  :teardown [(l/annex:stop-all)]})

^{:refer :php.docker :adopt true :added "4.0"}
(fact "php :basic evaluates arithmetic expressions in docker"
  [(l/! [:php.docker]
     (+ 1 2 3))

   (l/! [:php.docker]
     (* 6 7))

   (l/! [:php.docker]
     (- 100 1))]
  => [6 42 99])

^{:refer :php.docker :adopt true :added "4.0"}
(fact "php docker container defines and calls inline functions"
  [(l/! [:php.docker]
     (do (defn add-10 [x] (return (+ x 10)))
         (add-10 5)))

   (l/! [:php.docker]
     (do (defn mul-xy [x y] (return (* x y)))
         (mul-xy 6 7)))]
  => [15 42])

^{:refer :php.docker :adopt true :added "4.0"}
(fact "php docker container handles string operations"
  [(l/! [:php.docker]
     (concat "hello " "world"))

   (l/! [:php.docker]
     (do (defn greet [name] (return name))
         (greet "hello php")))]
  => ["hello world" "hello php"])

(comment
  (l/annex:start-all)
  (l/annex:stop-all)
  (l/! [:php.docker] (+ 1 2 3))
  )
