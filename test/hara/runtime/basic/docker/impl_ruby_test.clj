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

(l/script- :ruby
  {:runtime :basic
   :config  (registry/registry-config :ruby)
   :test-mode true})

(fact:global
 {:skip (or (not (env/program-exists? "docker"))
            (not (System/getenv "RT_BASIC_DOCKER_TESTS")))
  :setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer ruby/vectors :adopt true :added "4.0"}
(fact "ruby :basic evaluates arithmetic expressions in docker"
  [(!.ruby
     (+ 1 2 3))

   (!.ruby
     (* 6 7))

   (!.ruby
     (- 100 1))]
  => [6 42 99])

^{:refer ruby/functions :adopt true :added "4.0"}
(fact "ruby docker container defines and calls inline functions"
  [(!.ruby
     (do (defn add-10 [x] (return (+ x 10)))
         (add-10 5)))

   (!.ruby
     (do (defn mul-xy [x y] (return (* x y)))
         (mul-xy 6 7)))]
  => [15 42])

^{:refer ruby/strings :adopt true :added "4.0"}
(fact "ruby docker container handles string operations"
  [(!.ruby
     (+ "hello " "world"))

   (!.ruby
     (do (defn greet [name] (return (+ "hi " name)))
         (greet "ruby")))]
  => ["hello world" "hi ruby"])

(comment
  (l/rt:restart)
  (l/rt:stop)
  (!.ruby (+ 1 2 3))
  )
