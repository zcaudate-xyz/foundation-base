(ns rt.chromedriver.util-test
  (:use code.test)
  (:require [rt.chromedriver.util :as util]
            [rt.chromedriver.connection :as conn]
            [rt.chromedriver.connection-test :as conn-test]))

(fact:global
 {:setup    [(conn-test/restart-scaffold)]
  :teardown [(conn-test/stop-scaffold)]})

^{:refer rt.chromedriver.util/runtime-evaluate :added "4.0"
  :setup [(def +conn+
            (conn/conn-create {:port (:port (conn-test/start-scaffold))}))]}
(fact "performs runtime eval on connection"

  @(util/runtime-evaluate +conn+ "1")
  => {"value" 1, "type" "number", "description" "1"})

^{:refer rt.chromedriver.util/page-navigate :added "4.0"}
(fact "navigates to a new url"

  @(util/page-navigate +conn+ "about:blank")
  => (contains {"frameId" string?,
                "loaderId" string?}))

^{:refer rt.chromedriver.util/page-capture-screenshot :added "4.0"}
(fact "captures a screenshot from the browser"

  @(util/page-capture-screenshot +conn+)
  => (any bytes?
          nil?))

^{:refer rt.chromedriver.util/target-info :added "4.0"}
(fact "gets the target info"

  @(util/target-info +conn+)
  => (contains-in {"targetInfo" {"attached" true, "url" string?}}))

^{:refer rt.chromedriver.util/target-create :added "4.0"}
(fact "creates a new target"

  @(util/target-create +conn+ "about:blank")
  => (contains {"targetId" string?}))

^{:refer rt.chromedriver.util/target-close :added "4.0"}
(fact "closes a current target"

  @(util/target-close +conn+
                      (get @(util/target-create +conn+ "about:blank")
                           "targetId"))
  => {"success" true})
