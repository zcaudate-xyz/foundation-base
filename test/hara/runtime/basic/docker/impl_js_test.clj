(ns hara.runtime.basic.docker.impl-js-test
  (:require [hara.runtime.basic.docker.registry :as registry]
            [std.lib.env :as env]
            [hara.lang :as l]
            [hara.lang.script :as script])
  (:use code.test))

;;
;; Node.js basic runtime in a Docker container.
;;
;; Uses the builtin `net` module + `JSON` — no extra packages needed.
;; Bootstrap connects back to the JVM via host.docker.internal:<port>.
;;
;; Image: foundation-base/rt-basic-js:latest
;; Run with: RT_BASIC_DOCKER_TESTS=true lein test :only hara.runtime.basic.docker.impl-js-test
;;

(when (and (env/program-exists? "docker")
           (System/getenv "RT_BASIC_DOCKER_TESTS"))
  (script/script-ext [:js.docker :js]
    {:runtime :basic
     :config  (registry/registry-config :js)}))

(fact:global
 {:skip (or (not (env/program-exists? "docker"))
            (not (System/getenv "RT_BASIC_DOCKER_TESTS")))
  :setup [(l/annex:start-all)]
  :teardown [(l/annex:stop-all)]})

^{:refer :js.docker :adopt true :added "4.0"}
(fact "js :basic evaluates arithmetic expressions in docker"
  [(l/! [:js.docker]
     (+ 1 2 3))

   (l/! [:js.docker]
     (* 6 7))

   (l/! [:js.docker]
     (- 100 1))]
  => [6 42 99])

^{:refer :js.docker :adopt true :added "4.0"}
(fact "js docker container defines and calls inline functions"
  [(l/! [:js.docker]
     (do (var add-10 (fn [x] (return (+ x 10))))
         (add-10 5)))

   (l/! [:js.docker]
     (do (var mul-xy (fn [x y] (return (* x y))))
         (mul-xy 6 7)))]
  => [15 42])

^{:refer :js.docker :adopt true :added "4.0"}
(fact "js docker container handles string operations"
  (l/! [:js.docker]
    (do (var greet (fn [name] (return (+ "hello " name))))
        (greet "world")))
  => "hello world")

^{:refer :js.docker :adopt true :added "4.0"}
(fact "js docker container handles array operations"
  (l/! [:js.docker]
    (do (var nums [1 2 3 4 5])
        (nums.reduce (fn [acc x] (return (+ acc x))) 0)))
  => 15)

(comment
  (l/annex:start-all)
  (l/annex:stop-all)
  (l/! [:js.docker] (+ 1 2 3))
  )
