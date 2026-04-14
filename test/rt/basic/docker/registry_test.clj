(ns rt.basic.docker.registry-test
  (:use code.test)
  (:require [clojure.string :as str]
            [rt.basic.docker.registry :refer :all]))

^{:refer rt.basic.docker.registry/r-bootstrap :added "4.1"}
(fact "wraps the R bootstrap with jsonlite installation"
  (str/includes? (r-bootstrap 1234 {:host "host.docker.internal"})
                 "install.packages('jsonlite')")
  => true)

^{:refer rt.basic.docker.registry/registry-config :added "4.1"}
(fact "returns runtime config for canonical basic images"
  [(-> (registry-config :python) :container :image)
   (-> (registry-config :lua) :container :image)
   (-> (registry-config :lua) :program)
   (-> (registry-config :julia) :process :timeout)
   (-> (registry-config :erlang) :container :exec)]
  => ["foundation-base/rt-basic-python:latest"
      "foundation-base/rt-basic-lua:latest"
      :luajit
      120000
      ["sh" "-c"]])

^{:refer rt.basic.docker.registry/registry-image :added "4.1"}
(fact "returns canonical repo-owned images for all basic runtimes"
  (mapv registry-image [:python :js :ruby :php :perl :lua :julia :r :erlang])
  => ["foundation-base/rt-basic-python:latest"
      "foundation-base/rt-basic-js:latest"
      "foundation-base/rt-basic-ruby:latest"
      "foundation-base/rt-basic-php:latest"
      "foundation-base/rt-basic-perl:latest"
      "foundation-base/rt-basic-lua:latest"
      "foundation-base/rt-basic-julia:latest"
      "foundation-base/rt-basic-r:latest"
      "foundation-base/rt-basic-erlang:latest"])

^{:refer rt.basic.docker.registry/registry-dockerfile :added "4.1"}
(fact "returns dockerfile source for each registered language"
  (every? #(str/includes? (registry-dockerfile %) "FROM")
          [:python
           :js
           :ruby
           :php
           :perl
           :lua
           :julia
           :r
           :erlang])
  => true)

^{:refer rt.basic.docker.registry/registry-dockerfile-path :added "4.1"}
(fact "resolves repo-local Dockerfile paths for each registered language"
  (every? #(str/ends-with? (registry-dockerfile-path %) "/Dockerfile")
          [:python :js :ruby :php :perl :lua :julia :r :erlang])
  => true)
