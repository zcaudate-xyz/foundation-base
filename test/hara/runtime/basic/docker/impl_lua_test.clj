(ns hara.runtime.basic.docker.impl-lua-test
  (:require [hara.runtime.basic.docker.registry :as registry]
            [std.lib.env :as env]
            [hara.lang :as l]
            [hara.lang.script :as script])
  (:use code.test))

;;
;; Lua basic runtime in a Docker container.
;;
;; Uses the project-owned hara.runtime.basic OpenResty LuaJIT image with Nchan and
;; LuaRocks packages preinstalled.
;;
;; Bootstrap connects back to the JVM via host.docker.internal:<port>.
;;
;; Image: foundation-base/rt-basic-lua:latest
;; Run with: RT_BASIC_DOCKER_TESTS=true lein test :only hara.runtime.basic.docker.impl-lua-test
;;

(when (and (env/program-exists? "docker")
           (System/getenv "RT_BASIC_DOCKER_TESTS"))
  (script/script-ext [:lua.docker :lua]
    {:runtime :basic
     :config  (registry/registry-config :lua)}))

(fact:global
 {:skip (or (not (env/program-exists? "docker"))
            (not (System/getenv "RT_BASIC_DOCKER_TESTS")))
  :setup [(l/annex:start-all)]
  :teardown [(l/annex:stop-all)]})

^{:refer :lua.docker :adopt true :added "4.0"}
(fact "lua :basic evaluates arithmetic expressions in docker"
  [(l/! [:lua.docker]
     (+ 1 2 3))

   (l/! [:lua.docker]
     (* 6 7))

   (l/! [:lua.docker]
     (- 100 1))]
  => [6 42 99])

^{:refer :lua.docker :adopt true :added "4.0"}
(fact "lua docker container defines and calls inline functions"
  [(l/! [:lua.docker]
     (do (defn add-10 [x] (return (+ x 10)))
         (add-10 5)))

   (l/! [:lua.docker]
     (do (defn mul-xy [x y] (return (* x y)))
         (mul-xy 6 7)))]
  => [15 42])

(comment
  (l/annex:start-all)
  (l/annex:stop-all)
  (l/! [:lua.docker] (+ 1 2 3))
  )
