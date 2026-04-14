(ns rt.basic.docker.registry
  (:require [clojure.java.io :as io]
            [rt.basic.impl.process-js      :as js]
            [rt.basic.impl.process-lua     :as lua]
            [rt.basic.impl.process-python  :as python]
            [rt.basic.impl.process-ruby    :as ruby]
            [rt.basic.impl-annex.process-erlang :as erlang]
            [rt.basic.impl-annex.process-julia  :as julia]
            [rt.basic.impl-annex.process-perl   :as perl]
            [rt.basic.impl-annex.process-php    :as php]
            [rt.basic.impl-annex.process-r      :as r]))

;;
;; Canonical Docker image + bootstrap registry for :basic runtimes.
;;
;; Each entry defines the minimal Docker image required to run the
;; language's socket-based eval loop (default-basic-client).
;;
;; Bootstrap notes:
;;
;;   :bootstrap  - custom fn if default-basic-client cannot be used as-is
;;   :notes      - requirements or caveats
;;   Dockerfile source lives under docker/rt.basic/<lang>/Dockerfile
;;
;; Usage in tests:
;;
;;   (l/script+ [:py.docker :python]
;;     {:runtime :basic
;;      :config  (get-in +registry+ [:python :config])})
;;

(defn r-bootstrap
  "wraps default-basic-client with inline jsonlite install for r-base image"
  {:added "4.0"}
  [port opts]
  (str "options(repos = c(CRAN = 'https://cran.r-project.org/'))\n"
       "if (!requireNamespace('jsonlite', quietly = TRUE)) {\n"
       "  install.packages('jsonlite')\n"
       "}\n\n"
       (r/default-basic-client port opts)))

(def +registry+
  "canonical docker images and bootstrap config for all :basic-capable languages"
   {:python
     {:image      "foundation-base/rt-basic-python:latest"
      :notes      "project-owned Python basic runtime image using the CPython slim base image"
      :config     {:container {:image "foundation-base/rt-basic-python:latest"}}}

     :js
     {:image      "foundation-base/rt-basic-js:latest"
      :notes      "project-owned Node.js basic runtime image using the Node alpine base image"
      :config     {:container {:image "foundation-base/rt-basic-js:latest"}}}

     :ruby
     {:image      "foundation-base/rt-basic-ruby:latest"
      :notes      "project-owned Ruby basic runtime image using the Ruby alpine base image"
      :config     {:container {:image "foundation-base/rt-basic-ruby:latest"}}}

     :php
     {:image      "foundation-base/rt-basic-php:latest"
      :notes      "project-owned PHP basic runtime image using the PHP CLI base image"
      :config     {:container {:image "foundation-base/rt-basic-php:latest"}}}

     :perl
     {:image      "foundation-base/rt-basic-perl:latest"
      :notes      "project-owned Perl basic runtime image using the Perl slim base image"
      :config     {:container {:image "foundation-base/rt-basic-perl:latest"}}}

     :lua
     {:image      "foundation-base/rt-basic-lua:latest"
      :notes      "project-owned OpenResty LuaJIT image with Nchan and LuaRocks packages preinstalled"
      :config     {:container {:image "foundation-base/rt-basic-lua:latest"}
                   :program :luajit}}

    :julia
     {:image      "foundation-base/rt-basic-julia:latest"
      :notes      "project-owned Julia basic runtime image with JSON preinstalled"
      :config     {:container {:image "foundation-base/rt-basic-julia:latest"}
                   :process   {:timeout 120000}}}

    :r
     {:image      "foundation-base/rt-basic-r:latest"
      :notes      "project-owned R basic runtime image based on rocker/r-ver with jsonlite available"
      :config     {:container {:image "foundation-base/rt-basic-r:latest"}
                   :process   {:timeout 30000}}
      :r-base-bootstrap #'r-bootstrap}

     :erlang
     {:image      "foundation-base/rt-basic-erlang:latest"
      :notes      "project-owned Erlang basic runtime image using OTP 27 alpine with shell bootstrap support"
      :config     {:container {:image "foundation-base/rt-basic-erlang:latest"
                               :exec  ["sh" "-c"]}}}})

(defn registry-dockerfile-path
  "returns the repo-local Dockerfile path for a language"
  {:added "4.1"}
  [lang]
  (str (io/file (System/getProperty "user.dir")
                "docker"
                "rt.basic"
                (name lang)
                "Dockerfile")))

(defn registry-config
  "returns the :config map for a language, suitable for use in l/script+"
  {:added "4.0"}
  [lang]
  (get-in +registry+ [lang :config]))

(defn registry-image
  "returns the canonical docker image for a language"
  {:added "4.0"}
  [lang]
  (get-in +registry+ [lang :image]))

(defn registry-dockerfile
  "returns Dockerfile source from the repo-owned docker/rt.basic directory"
  {:added "4.1"}
  [lang]
  (slurp (registry-dockerfile-path lang)))
