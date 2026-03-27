(ns rt.chromedriver.impl-test
  (:use code.test)
  (:require [rt.chromedriver.impl :as impl]
            [std.lib :as h]
            [rt.basic.type-bench :as bench]
            [rt.chromedriver.util :as util]))

(defonce +browser+ (atom nil))

(fact:global
 {:setup    [(reset! +browser+ (impl/browser {:lang :js}))]
  :teardown [(h/stop @+browser+)]})

^{:refer rt.chromedriver.impl/start-browser-bench :added "4.0"
  :setup [(def +rt+ (impl/browser:create {:lang :js}))]
  :teardown (bench/stop-bench-process (:port +rt+))}
(fact "starts the browser bench"
  ^:hidden

  (impl/start-browser-bench +rt+)
  => (contains {:type :bench/basic}))

^{:refer rt.chromedriver.impl/start-browser-container :added "4.0"}
(fact "starts a browser container")

^{:refer rt.chromedriver.impl/start-browser :added "4.0"
  :setup [(def +rt+ (impl/browser:create {:lang :js}))]
  :teardown [(h/stop +rt+)]}
(fact "starts the browser bench and connection"
  ^:hidden
  (impl/start-browser +rt+)
  => +rt+)

^{:refer rt.chromedriver.impl/stop-browser-raw :added "4.0"}
(fact "stops the browser")

^{:refer rt.chromedriver.impl/raw-eval-browser :added "4.0"}
(fact "evaluates the browser"
  ^:hidden
  (impl/raw-eval-browser @+browser+ "1 + 1")
  => 2)

^{:refer rt.chromedriver.impl/invoke-ptr-browser :added "4.0"}
(fact "invokes the browser pointer"
  ^:hidden
  (impl/invoke-ptr-browser @+browser+
                           identity
                           [1])
  => nil?)

^{:refer rt.chromedriver.impl/browser:create :added "4.0"}
(fact "creates a browser")

^{:refer rt.chromedriver.impl/browser :added "4.0"}
(fact "starts the browser")

^{:refer rt.chromedriver.impl/wrap-browser-state :added "4.0"}
(fact "wrapper for the browser"
  ^:hidden
  
  @((impl/wrap-browser-state util/target-info)
    @+browser+)
  => (contains-in {"targetInfo" {"attached" true, "url" string?}}))
