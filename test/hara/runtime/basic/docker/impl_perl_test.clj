(ns hara.runtime.basic.docker.impl-perl-test
  (:require [hara.runtime.basic.docker.registry :as registry]
            [std.lib.env :as env]
            [hara.lang :as l]
            [hara.lang.script :as script])
  (:use code.test))

;;
;; Perl basic runtime in a Docker container.
;;
;; Uses IO::Socket::INET + JSON::PP — both in Perl core (5.14+), no CPAN needed.
;; Bootstrap connects back to the JVM via host.docker.internal:<port>.
;;
;; Image: foundation-base/rt-basic-perl:latest
;; Run with: RT_BASIC_DOCKER_TESTS=true lein test :only hara.runtime.basic.docker.impl-perl-test
;;

(when (and (env/program-exists? "docker")
           (System/getenv "RT_BASIC_DOCKER_TESTS"))
  (script/script-ext [:pl.docker :perl]
    {:runtime :basic
     :config  (registry/registry-config :perl)}))

(fact:global
 {:skip (or (not (env/program-exists? "docker"))
            (not (System/getenv "RT_BASIC_DOCKER_TESTS")))
  :setup [(l/annex:start-all)]
  :teardown [(l/annex:stop-all)]})

^{:refer :pl.docker :adopt true :added "4.0"}
(fact "perl :basic evaluates arithmetic expressions in docker"
  [(l/! [:pl.docker]
     (+ 1 2 3))

   (l/! [:pl.docker]
     (* 6 7))

   (l/! [:pl.docker]
     (- 100 1))]
  => [6 42 99])

^{:refer :pl.docker :adopt true :added "4.0"}
(fact "perl docker container defines and calls inline functions"
  [(l/! [:pl.docker]
     (do (defn add-10 [x] (return (+ x 10)))
         (add-10 5)))

   (l/! [:pl.docker]
     (do (defn mul-xy [x y] (return (* x y)))
         (mul-xy 6 7)))]
  => [15 42])

(comment
  (l/annex:start-all)
  (l/annex:stop-all)
  (l/! [:pl.docker] (+ 1 2 3))
  )
