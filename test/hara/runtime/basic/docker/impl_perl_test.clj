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
;; Image: ghcr.io/zcaudate-xyz/foundation-base/rt-basic-perl:latest
;; Run with: RT_BASIC_DOCKER_TESTS=true lein test :only hara.runtime.basic.docker.impl-perl-test
;;

(l/script- :perl
  {:runtime :basic
   :config  (registry/registry-config :perl)
   :test-mode true})

(fact:global
 {:skip (or (not (env/program-exists? "docker"))
            (not (env/docker-daemon-available?))
            (System/getenv "HARA_NO_DOCKER"))
  :setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer perl/vectors :adopt true :added "4.0"}
(fact "perl :basic evaluates arithmetic expressions in docker"

  [(!.pl
    (+ 1 2 3))
   
   (!.pl
    (* 6 7))

   (!.pl
    (- 100 1))]
  => [6 42 99])

^{:refer perl/functions :adopt true :added "4.0"}
(fact "perl docker container defines and calls inline functions"
  [(!.pl
     (do (defn add-10 [x] (return (+ x 10)))
         (add-10 5)))

   (!.pl
     (do (defn mul-xy [x y] (return (* x y)))
         (mul-xy 6 7)))]
  => [15 42])

(comment
  (l/rt:restart)
  (l/rt:stop)
  (!.pl (+ 1 2 3))
  )
