(ns tahto.runtime.chromedriver.impl-test
  (:use code.test)
  (:require [tahto.runtime.chromedriver.impl :as impl]
            [std.lib :as h]
            [tahto.runtime.basic.type-bench :as bench]
            [tahto.runtime.chromedriver.util :as util]))

(defonce +browser+ (atom nil))

(fact:global
 {:setup    [(reset! +browser+ (impl/browser {:lang :js}))]
  :teardown [(h/stop @+browser+)]})

^{:refer tahto.runtime.chromedriver.impl/start-browser-bench :added "4.0"
  :setup [(def +rt+ (impl/browser:create {:lang :js}))]
  :teardown (bench/stop-bench-process (:port +rt+))}
(fact "starts the browser bench"

  (impl/start-browser-bench +rt+)
  => (contains {:type :bench/basic}))

^{:refer tahto.runtime.chromedriver.impl/start-browser-container :added "4.0"}
(fact "starts a browser container")

^{:refer tahto.runtime.chromedriver.impl/start-browser :added "4.0"
  :setup [(def +rt+ (impl/browser:create {:lang :js}))]
  :teardown [(h/stop +rt+)]}
(fact "starts the browser bench and connection"
  (impl/start-browser +rt+)
  => +rt+)

^{:refer tahto.runtime.chromedriver.impl/stop-browser-raw :added "4.0"}
(fact "stops the browser")

^{:refer tahto.runtime.chromedriver.impl/raw-eval-browser :added "4.0"}
(fact "evaluates the browser"
  (impl/raw-eval-browser @+browser+ "1 + 1")
  => 2)

^{:refer tahto.runtime.chromedriver.impl/invoke-ptr-browser :added "4.0"}
(fact "invokes the browser pointer"
  (impl/invoke-ptr-browser @+browser+
                           identity
                           [1])
  => 1)

^{:refer tahto.runtime.chromedriver.impl/browser:create :added "4.0"}
(fact "creates a browser")

^{:refer tahto.runtime.chromedriver.impl/browser :added "4.0"}
(fact "starts the browser")

^{:refer tahto.runtime.chromedriver.impl/wrap-browser-state :added "4.0"}
(fact "wrapper for the browser"

  @((impl/wrap-browser-state util/target-info)
    @+browser+)
  => (contains-in {"targetInfo" {"attached" true, "url" string?}}))
