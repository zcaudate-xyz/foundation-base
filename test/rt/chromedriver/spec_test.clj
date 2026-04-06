(ns rt.chromedriver.spec-test
  (:use code.test)
  (:require [rt.chromedriver.spec :as spec]
            [rt.chromedriver.util :as util]))

^{:refer rt.chromedriver.spec/spec-download :added "4.0"}
(fact "downloads the chrome devtools spec")

^{:refer rt.chromedriver.spec/list-domains :added "4.0"}
(fact "lists all domains"
  ^:hidden
  
  (count (spec/list-domains))
  => 54

  (let [domains (set (spec/list-domains))]
    (boolean (and (domains "Page")
                  (domains "Runtime")
                  (domains "Target"))))
  => true)

^{:refer rt.chromedriver.spec/get-domain-raw :added "4.0"}
(fact "gets the raw domain"
  ^:hidden
  
  (spec/get-domain-raw "Console")
  => (contains {"clearMessages" map?
                "disable" map?
                "enable" map?}))

^{:refer rt.chromedriver.spec/list-methods :added "4.0"}
(fact "lists all spec methods"
  ^:hidden
  
  (let [methods (set (spec/list-methods "Runtime"))]
    (boolean (and (methods "evaluate")
                  (methods "getProperties")
                  (methods "releaseObject"))))
  => true

  (let [methods (set (spec/list-methods "Page"))]
    (boolean (and (methods "captureScreenshot")
                  (methods "navigate")
                  (methods "reload"))))
  => true)

^{:refer rt.chromedriver.spec/get-method :added "4.0"}
(fact "gets the method"
  ^:hidden

  (-> (spec/get-method "Page" "captureScreenshot")
      (update "parameters"
              (fn [parameters]
                (mapv (fn [m]
                        (select-keys m ["name" "optional"]))
                      parameters))))
  => (contains {"name" "captureScreenshot"})

  (-> (spec/get-method "Runtime" "evaluate")
      (update "parameters"
              (fn [parameters]
                (mapv (fn [m]
                        (select-keys m ["name" "optional"]))
                      parameters))))
  => (contains {"name" "evaluate"}))


^{:refer rt.chromedriver.spec/tmpl-connection :added "4.1"}
(fact "creates a connection wrapper defn form for a domain/method"
  ^:hidden

  (let [form (spec/tmpl-connection '[navigate-page ["Page" "navigate"]])]
    [(first form)
     (second form)])
  => '[defn navigate-page])

^{:refer rt.chromedriver.spec/tmpl-browser :added "4.1"}
(fact "constructs a def form that wraps a function with browser state"
  ^:hidden

  (let [form (spec/tmpl-browser '[my-browser rt.chromedriver.impl/start-browser])]
    [(first form)
     (second form)])
  => '[def my-browser])