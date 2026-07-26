tahto/runtime/basic/docker/impl_r_test.clj:1:(ns tahto.runtime.basic.docker.impl-r-test
tahto/runtime/basic/docker/impl_r_test.clj:2:  (:require [tahto.runtime.basic.docker.registry :as registry]
tahto/runtime/basic/docker/impl_r_test.clj:3:            [tahto.runtime.basic.impl-annex.process-r :as r]
            [std.lib.env :as env]
            [tahto.core :as l]
            [tahto.core.script :as script]
            [clojure.string :as str])
  (:use code.test))

;;
;; R basic runtime in a Docker container.
;;
tahto/runtime/basic/docker/impl_r_test.clj:13:;; Uses the project-owned tahto.runtime.basic R image.
;; The canonical registry entry is based on `rocker/r-ver:4.3`,
;; which includes jsonlite and can connect back to the JVM host.
;;
;; Image: ghcr.io/zcaudate-xyz/foundation-base/rt-basic-r:latest
tahto/runtime/basic/docker/impl_r_test.clj:18:;; Run with: RT_BASIC_DOCKER_TESTS=true lein test :only tahto.runtime.basic.docker.impl-r-test
;;

(defn r-docker-bootstrap
  "r bootstrap that pre-installs jsonlite before connecting"
  {:added "4.0"}
  [port opts]
  (str "options(repos = c(CRAN = 'https://cran.r-project.org/'))\n"
       "if (!requireNamespace('jsonlite', quietly = TRUE)) {\n"
       "  install.packages('jsonlite')\n"
       "}\n\n"
       (r/default-basic-client port opts)))

(l/script- :r
  {:runtime :basic
   :config  (registry/registry-config :r)
   :test-mode true})

(fact:global
 {:skip (or (not (env/program-exists? "docker"))
            (not (env/docker-daemon-available?))
tahto/runtime/basic/docker/impl_r_test.clj:39:            (System/getenv "TAHTO_NO_DOCKER"))
  :setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer r/vectors :adopt true :added "4.0"}
(fact "r :basic evaluates arithmetic expressions in docker"
  [(!.R
     (+ 1 2 3))

   (!.R
     (* 6 7))

   (!.R
     (- 100 1))]
  => [6 42 99])

^{:refer r/functions :adopt true :added "4.0"}
(fact "r docker container defines and calls inline functions"
  [(!.R
     (do (var add-10 (fn [x] (return (+ x 10))))
         (add-10 5)))

   (!.R
     (do (var mul-xy (fn [x y] (return (* x y))))
         (mul-xy 6 7)))]
  => [15 42])

^{:refer r/vector-ops :adopt true :added "4.0"}
(fact "r docker container handles vector operations"
  (!.R
    (sum [1 2 3 4 5]))
  => 15)

tahto/runtime/basic/docker/impl_r_test.clj:72:^{:refer tahto.runtime.basic.docker.impl-r-test/r-docker-bootstrap :added "4.0"}
(fact "r docker bootstrap wraps default-basic-client with jsonlite install"
  (let [src (r-docker-bootstrap 9999 {})]
    [(str/includes? src "install.packages")
     (str/includes? src "jsonlite")
     (string? src)])
  => [true true true])

(comment
  (l/rt:restart)
  (l/rt:stop)
  (!.R (+ 1 2 3))
  )
