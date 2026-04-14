(ns rt.basic.docker.impl-r-test
  (:require [rt.basic.docker.registry :as registry]
            [rt.basic.impl-annex.process-r :as r]
            [rt.basic.type-common :as common]
            [std.lang :as l]
            [std.lang.base.script :as script])
  (:use code.test))

;;
;; R basic runtime in a Docker container.
;;
;; Uses the project-owned rt.basic R image.
;;
;; The canonical registry entry is based on `rocker/r-ver:4.3`,
;; which includes jsonlite and can connect back to the JVM host.
;;
;; Image: foundation-base/rt-basic-r:latest
;; Run with: RT_BASIC_DOCKER_TESTS=true lein test :only rt.basic.docker.impl-r-test
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

(def CANARY-DOCKER
  (and (common/program-exists? "docker")
       (some? (System/getenv "RT_BASIC_DOCKER_TESTS"))))

(when CANARY-DOCKER
  (script/script-ext [:r.docker :r]
    {:runtime :basic
     :config  (registry/registry-config :r)}))

(fact:global
 {:setup    [(when CANARY-DOCKER (l/annex:start-all))]
  :teardown [(when CANARY-DOCKER (l/annex:stop-all))]})

^{:refer rt.basic.docker.impl-r-test/CANARY-DOCKER :adopt true :added "4.0"}
(fact "r :basic evaluates arithmetic expressions in docker"
  (if CANARY-DOCKER
    [(l/! [:r.docker]
       (+ 1 2 3))

     (l/! [:r.docker]
       (* 6 7))

     (l/! [:r.docker]
       (- 100 1))]
    :docker-unavailable)
  => (any [6 42 99]
          :docker-unavailable))

^{:refer rt.basic.docker.impl-r-test/CANARY-DOCKER :adopt true :added "4.0"}
(fact "r docker container defines and calls inline functions"
  (if CANARY-DOCKER
    [(l/! [:r.docker]
       (do (var add-10 (fn [x] (return (+ x 10))))
           (add-10 5)))

     (l/! [:r.docker]
       (do (var mul-xy (fn [x y] (return (* x y))))
           (mul-xy 6 7)))]
    :docker-unavailable)
  => (any [15 42]
          :docker-unavailable))

^{:refer rt.basic.docker.impl-r-test/CANARY-DOCKER :adopt true :added "4.0"}
(fact "r docker container handles vector operations"
  (if CANARY-DOCKER
    (l/! [:r.docker]
      (sum [1 2 3 4 5]))
    :docker-unavailable)
  => (any 15 :docker-unavailable))

^{:refer rt.basic.docker.impl-r-test/r-docker-bootstrap :added "4.0"}
(fact "r docker bootstrap wraps default-basic-client with jsonlite install"
  (let [src (r-docker-bootstrap 9999 {})]
    [(clojure.string/includes? src "install.packages")
     (clojure.string/includes? src "jsonlite")
     (string? src)])
  => [true true true])

(comment
  (l/annex:start-all)
  (l/annex:stop-all)
  (l/! [:r.docker] (+ 1 2 3))
  )
