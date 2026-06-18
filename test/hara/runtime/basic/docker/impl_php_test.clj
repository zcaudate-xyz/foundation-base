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

(l/script- :php
  {:runtime :basic
   :config  (registry/registry-config :php)
   :test-mode true})

(fact:global
 {:skip (or (not (env/program-exists? "docker"))
            (not (System/getenv "RT_BASIC_DOCKER_TESTS")))
  :setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer php/vectors :adopt true :added "4.0"}
(fact "php :basic evaluates arithmetic expressions in docker"
  [(!.php
     (+ 1 2 3))

   (!.php
     (* 6 7))

   (!.php
     (- 100 1))]
  => [6 42 99])

^{:refer php/functions :adopt true :added "4.0"}
(fact "php docker container defines and calls inline functions"
  [(!.php
     (do (defn add-10 [x] (return (+ x 10)))
         (add-10 5)))

   (!.php
     (do (defn mul-xy [x y] (return (* x y)))
         (mul-xy 6 7)))]
  => [15 42])

^{:refer php/strings :adopt true :added "4.0"}
(fact "php docker container handles string operations"
  [(!.php
     (concat "hello " "world"))

   (!.php
     (do (defn greet [name] (return name))
         (greet "hello php")))]
  => ["hello world" "hello php"])

(comment
  (l/rt:restart)
  (l/rt:stop)
  (!.php (+ 1 2 3))
  )
