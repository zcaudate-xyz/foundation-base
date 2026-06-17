(ns hara.runtime.chromedriver.e2e-tabs-test
  (:use code.test)
  (:require [hara.runtime.chromedriver :as chromedriver]
            [hara.lang :as l]))

(l/script :js
  {:runtime :chromedriver.instance
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-resource :as rt]]})

(fact:global
 {:setup [(l/rt:restart :js)]
  :teardown [(l/rt:stop)]})

^{:refer hara.runtime.chromedriver.e2e-tabs-test/workflow-basic-switch :added "4.0"
  :setup [(chromedriver/goto "data:text/html,<title>Home</title>" 4000 (l/rt :js))
          (def +home+  (chromedriver/current-tab (l/rt :js)))
          (def +other+ (chromedriver/tab-create (l/rt :js) "data:text/html,<title>Other</title>"))]
  :teardown [(chromedriver/tab-close (l/rt :js) +other+)
             (chromedriver/tab-switch (l/rt :js) +home+ {:bootstrap false})]}
(fact "switches to another tab and back with with-tab"

  (!.js document.title)
  => "Home"

  (chromedriver/with-tab (l/rt :js) +other+
    (!.js document.title))
  => "Other"

  (!.js document.title)
  => "Home")

^{:refer hara.runtime.chromedriver.e2e-tabs-test/workflow-sequential-tabs :added "4.0"
  :setup [(chromedriver/goto "data:text/html,<title>Base</title>" 4000 (l/rt :js))
          (def +base+ (chromedriver/current-tab (l/rt :js)))
          (def +tabs+
            (mapv (fn [n]
                    (chromedriver/tab-create
                     (l/rt :js)
                     (str "data:text/html,<title>Tab " n "</title>")))
                  [1 2 3]))]
  :teardown [(doseq [t +tabs+]
               (chromedriver/tab-close (l/rt :js) t))
             (chromedriver/tab-switch (l/rt :js) +base+ {:bootstrap false})]}
(fact "processes several tabs in sequence and collects their titles"

  (mapv (fn [tab]
          (chromedriver/with-tab (l/rt :js) tab
            (!.js document.title)))
        +tabs+)
  => ["Tab 1" "Tab 2" "Tab 3"])

^{:refer hara.runtime.chromedriver.e2e-tabs-test/workflow-tab-isolation :added "4.0"
  :setup [(chromedriver/goto "data:text/html,<title>Alpha</title>" 4000 (l/rt :js))
          (def +alpha+ (chromedriver/current-tab (l/rt :js)))
          (def +beta+
            (chromedriver/tab-create (l/rt :js) "data:text/html,<title>Beta</title>"))]
  :teardown [(chromedriver/tab-close (l/rt :js) +beta+)
             (chromedriver/tab-switch (l/rt :js) +alpha+ {:bootstrap false})]}
(fact "keeps document state isolated between tabs"

  (!.js document.title)
  => "Alpha"

  (chromedriver/with-tab (l/rt :js) +beta+
    (!.js document.title))
  => "Beta"

  (chromedriver/tab-switch (l/rt :js) +beta+ {:bootstrap false})
  @(chromedriver/evaluate (l/rt :js) "document.title = 'Beta-Changed'")

  (chromedriver/tab-switch (l/rt :js) +alpha+ {:bootstrap false})
  (!.js document.title)
  => "Alpha"

  (chromedriver/tab-switch (l/rt :js) +beta+ {:bootstrap false})
  (!.js document.title)
  => "Beta-Changed")

^{:refer hara.runtime.chromedriver.e2e-tabs-test/workflow-temporary-tab :added "4.0"
  :setup [(chromedriver/goto "data:text/html,<title>Main</title>" 4000 (l/rt :js))
          (def +main+ (chromedriver/current-tab (l/rt :js)))]
  :teardown [(chromedriver/tab-switch (l/rt :js) +main+ {:bootstrap false})]}
(fact "opens a temporary tab, evaluates, closes it, and resumes on the main tab"

  (def +temp+
    (chromedriver/tab-create (l/rt :js) "data:text/html,<title>Temporary</title>"))

  (chromedriver/with-tab (l/rt :js) +temp+
    (!.js document.title))
  => "Temporary"

  (chromedriver/tab-close (l/rt :js) +temp+)

  (!.js document.title)
  => "Main")

^{:refer hara.runtime.chromedriver.e2e-tabs-test/workflow-compare-tabs :added "4.0"
  :setup [(chromedriver/goto "data:text/html,<title>Left</title>" 4000 (l/rt :js))
          (def +left+ (chromedriver/current-tab (l/rt :js)))
          (def +right+
            (chromedriver/tab-create (l/rt :js) "data:text/html,<title>Right</title>"))]
  :teardown [(chromedriver/tab-close (l/rt :js) +right+)
             (chromedriver/tab-switch (l/rt :js) +left+ {:bootstrap false})]}
(fact "compares values across two tabs without mutating the active tab"

  (!.js document.title)
  => "Left"

  (chromedriver/with-tab (l/rt :js) +right+
    (!.js document.title))
  => "Right"

  (!.js document.title)
  => "Left")
