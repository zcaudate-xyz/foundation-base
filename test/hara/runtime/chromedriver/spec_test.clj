(ns hara.runtime.chromedriver.spec-test
  (:use code.test)
  (:require [hara.runtime.chromedriver.spec :as spec]
            [hara.runtime.chromedriver.util :as util]))

^{:refer hara.runtime.chromedriver.spec/spec-download :added "4.0"}
(fact "downloads the chrome devtools spec")

^{:refer hara.runtime.chromedriver.spec/list-domains :added "4.0"}
(fact "lists all domains"

  (count (spec/list-domains))
  => 54

  (let [domains (set (spec/list-domains))]
    (boolean (and (domains "Page")
                  (domains "Runtime")
                  (domains "Target"))))
  => true)

^{:refer hara.runtime.chromedriver.spec/get-domain-raw :added "4.0"}
(fact "gets the raw domain"

  (spec/get-domain-raw "Console")
  => (contains {"clearMessages" map?
                "disable" map?
                "enable" map?}))

^{:refer hara.runtime.chromedriver.spec/list-methods :added "4.0"}
(fact "lists all spec methods"

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

^{:refer hara.runtime.chromedriver.spec/get-method :added "4.0"}
(fact "gets the method"

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


^{:refer hara.runtime.chromedriver.spec/tmpl-connection :added "4.0"}
(fact "creates a connection form given template"

  (spec/tmpl-connection ['page-navigate ["Page" "navigate"]])
  => '(defn page-navigate
       [conn url & [{:keys [frame-id referrer referrer-policy transition-type], :as m} timeout opts]]
       (hara.runtime.chromedriver.connection/send
        conn
        "Page.navigate"
        (merge {"url" url} m)
        timeout
        opts))

  (spec/tmpl-connection ['page-capture-screenshot ["Page" "captureScreenshot"]])
  => '(defn page-capture-screenshot
       [conn & [{:keys [capture-beyond-viewport clip format from-surface optimize-for-speed quality], :as m} timeout opts]]
       (hara.runtime.chromedriver.connection/send
        conn
        "Page.captureScreenshot"
        (merge {} m)
        timeout
        opts)))

^{:refer hara.runtime.chromedriver.spec/tmpl-browser :added "4.0"}
(fact "constructs the browser template"

  (spec/tmpl-browser ['browser-eval 'util/runtime-evaluate])
  => '(def browser-eval
       (hara.runtime.chromedriver.impl/wrap-browser-state util/runtime-evaluate))

  (-> (spec/tmpl-browser ['browser-eval 'util/runtime-evaluate])
      second
      meta
      :arglists
      second
      ffirst)
  => 'browser)