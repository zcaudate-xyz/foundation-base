(ns rt.basic.docker.impl-lua-test
  (:require [rt.basic.docker.registry :as registry]
            [rt.basic.type-common :as common]
            [std.lang :as l]
            [std.lang.base.script :as script])
  (:use code.test))

;;
;; Lua basic runtime in a Docker container.
;;
;; Uses the project-owned rt.basic OpenResty LuaJIT image with Nchan and
;; LuaRocks packages preinstalled.
;;
;; Bootstrap connects back to the JVM via host.docker.internal:<port>.
;;
;; Image: foundation-base/rt-basic-lua:latest
;; Run with: RT_BASIC_DOCKER_TESTS=true lein test :only rt.basic.docker.impl-lua-test
;;

(def CANARY-DOCKER
  (and (common/program-exists? "docker")
       (some? (System/getenv "RT_BASIC_DOCKER_TESTS"))))

(when CANARY-DOCKER
  (script/script-ext [:lua.docker :lua]
    {:runtime :basic
     :config  (registry/registry-config :lua)}))

(fact:global
 {:setup    [(when CANARY-DOCKER (l/annex:start-all))]
  :teardown [(when CANARY-DOCKER (l/annex:stop-all))]})

^{:refer rt.basic.docker.impl-lua-test/CANARY-DOCKER :adopt true :added "4.0"}
(fact "lua :basic evaluates arithmetic expressions in docker"
  (if CANARY-DOCKER
    [(l/! [:lua.docker]
       (+ 1 2 3))

     (l/! [:lua.docker]
       (* 6 7))

     (l/! [:lua.docker]
       (- 100 1))]
    :docker-unavailable)
  => (any [6 42 99]
          :docker-unavailable))

^{:refer rt.basic.docker.impl-lua-test/CANARY-DOCKER :adopt true :added "4.0"}
(fact "lua docker container defines and calls inline functions"
  (if CANARY-DOCKER
    [(l/! [:lua.docker]
       (do (defn add-10 [x] (return (+ x 10)))
           (add-10 5)))

     (l/! [:lua.docker]
       (do (defn mul-xy [x y] (return (* x y)))
           (mul-xy 6 7)))]
    :docker-unavailable)
  => (any [15 42]
          :docker-unavailable))

(comment
  (l/annex:start-all)
  (l/annex:stop-all)
  (l/! [:lua.docker] (+ 1 2 3))
  )
