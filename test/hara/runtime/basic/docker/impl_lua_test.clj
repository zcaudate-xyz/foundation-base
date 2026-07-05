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
;; Image: ghcr.io/zcaudate-xyz/foundation-base/rt-basic-lua:latest
;; Run with: RT_BASIC_DOCKER_TESTS=true lein test :only hara.runtime.basic.docker.impl-lua-test
;;

(l/script- :lua
  {:runtime :basic
   :config  (registry/registry-config :lua)
   :test-mode true})

(fact:global
 {:skip (or (not (env/program-exists? "docker"))
            (not (env/docker-daemon-available?))
            (System/getenv "HARA_NO_DOCKER"))
  :setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer lua/vectors :adopt true :added "4.0"}
(fact "lua :basic evaluates arithmetic expressions in docker"
  [(!.lua
     (+ 1 2 3))

   (!.lua
     (* 6 7))

   (!.lua
     (- 100 1))]
  => [6 42 99])

^{:refer lua/functions :adopt true :added "4.0"}
(fact "lua docker container defines and calls inline functions"
  [(!.lua
     (do (defn add-10 [x] (return (+ x 10)))
         (add-10 5)))

   (!.lua
     (do (defn mul-xy [x y] (return (* x y)))
         (mul-xy 6 7)))]
  => [15 42])

(comment
  (l/rt:restart)
  (l/rt:stop)
  (!.lua (+ 1 2 3))
  )
