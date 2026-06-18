(ns hara.runtime.basic.docker.impl-js-test
  (:use code.test)
  (:require [hara.runtime.basic.docker.registry :as registry]
            [std.lib.env :as env]
            [hara.lang :as l]
            [hara.lang.script :as script]))

;;
;; Node.js basic runtime in a Docker container.
;;
;; Uses the builtin `net` module + `JSON` — no extra packages needed.
;; Bootstrap connects back to the JVM via host.docker.internal:<port>.
;;
;; Image: foundation-base/rt-basic-js:latest
;; Run with: RT_BASIC_DOCKER_TESTS=true lein test :only hara.runtime.basic.docker.impl-js-test
;;

(l/script- :js
  {:runtime :basic
   :config  (registry/registry-config :js)
   :test-mode true})

(fact:global
 {:skip   (or (not (env/program-exists? "docker"))
              (System/getenv "HARA_NO_DOCKER"))
  :setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer :js.docker :adopt true :added "4.0"}
(fact "js :basic evaluates arithmetic expressions in docker"

  [(!.js
     (+ 1 2 3))

   (!.js
     (* 6 7))

   (!.js
     (- 100 1))]
  => [6 42 99])

^{:refer :js.docker :adopt true :added "4.0"}
(fact "js docker container defines and calls inline functions"

  [(!.js
     (do (var add-10 (fn [x] (return (+ x 10))))
         (add-10 5)))

   (!.js
     (do (var mul-xy (fn [x y] (return (* x y))))
         (mul-xy 6 7)))]
  => [15 42])

^{:refer :js.docker :adopt true :added "4.0"}
(fact "js docker container handles string operations"

  (!.js
    (do (var greet (fn [name] (return (+ "hello " name))))
        (greet "world")))
  => "hello world")

^{:refer :js.docker :adopt true :added "4.0"}
(fact "js docker container handles array operations"

  (!.js
    (do (var nums [1 2 3 4 5])
        (nums.reduce (fn [acc x] (return (+ acc x))) 0)))
  => 15)
