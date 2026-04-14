(ns rt.basic.docker.impl-js-test
  (:require [rt.basic.docker.registry :as registry]
            [rt.basic.type-common :as common]
            [std.lang :as l]
            [std.lang.base.script :as script])
  (:use code.test))

;;
;; Node.js basic runtime in a Docker container.
;;
;; Uses the builtin `net` module + `JSON` — no extra packages needed.
;; Bootstrap connects back to the JVM via host.docker.internal:<port>.
;;
;; Image: foundation-base/rt-basic-js:latest
;; Run with: RT_BASIC_DOCKER_TESTS=true lein test :only rt.basic.docker.impl-js-test
;;

(def CANARY-DOCKER
  (and (common/program-exists? "docker")
       (some? (System/getenv "RT_BASIC_DOCKER_TESTS"))))

(when CANARY-DOCKER
  (script/script-ext [:js.docker :js]
    {:runtime :basic
     :config  (registry/registry-config :js)}))

(fact:global
 {:setup    [(when CANARY-DOCKER (l/annex:start-all))]
  :teardown [(when CANARY-DOCKER (l/annex:stop-all))]})

^{:refer rt.basic.docker.impl-js-test/CANARY-DOCKER :adopt true :added "4.0"}
(fact "js :basic evaluates arithmetic expressions in docker"
  (if CANARY-DOCKER
    [(l/! [:js.docker]
       (+ 1 2 3))

     (l/! [:js.docker]
       (* 6 7))

     (l/! [:js.docker]
       (- 100 1))]
    :docker-unavailable)
  => (any [6 42 99]
          :docker-unavailable))

^{:refer rt.basic.docker.impl-js-test/CANARY-DOCKER :adopt true :added "4.0"}
(fact "js docker container defines and calls inline functions"
  (if CANARY-DOCKER
    [(l/! [:js.docker]
       (do (var add-10 (fn [x] (return (+ x 10))))
           (add-10 5)))

     (l/! [:js.docker]
       (do (var mul-xy (fn [x y] (return (* x y))))
           (mul-xy 6 7)))]
    :docker-unavailable)
  => (any [15 42]
          :docker-unavailable))

^{:refer rt.basic.docker.impl-js-test/CANARY-DOCKER :adopt true :added "4.0"}
(fact "js docker container handles string operations"
  (if CANARY-DOCKER
    (l/! [:js.docker]
      (do (var greet (fn [name] (return (+ "hello " name))))
          (greet "world")))
    :docker-unavailable)
  => (any "hello world" :docker-unavailable))

^{:refer rt.basic.docker.impl-js-test/CANARY-DOCKER :adopt true :added "4.0"}
(fact "js docker container handles array operations"
  (if CANARY-DOCKER
    (l/! [:js.docker]
      (do (var nums [1 2 3 4 5])
          (nums.reduce (fn [acc x] (return (+ acc x))) 0)))
    :docker-unavailable)
  => (any 15 :docker-unavailable))

(comment
  (l/annex:start-all)
  (l/annex:stop-all)
  (l/! [:js.docker] (+ 1 2 3))
  )
