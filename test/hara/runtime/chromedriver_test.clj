tahto/runtime/chromedriver_test.clj:1:(ns tahto.runtime.chromedriver-test
  (:use code.test)
tahto/runtime/chromedriver_test.clj:3:  (:require [tahto.runtime.chromedriver :as chromedriver]
             [tahto.core :as l]
             [std.lib :as h]
             [std.lib.env :as env]))

(l/script :js
  {:runtime :chromedriver.instance
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-resource :as rt]]})

(fact:global
 {:skip (not (or (env/program-exists? "google-chrome-stable")
                 (env/program-exists? "google-chrome")
                 (env/program-exists? "chromium")
                 (env/program-exists? "chromium-browser")
                 (env/program-exists? "chrome-headless-shell")))
  :setup [(l/rt:restart :js)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

tahto/runtime/chromedriver_test.clj:24:^{:refer tahto.runtime.chromedriver-test/browser-eval :added "4.0"}
(fact "chromedriver evaluates !.js expressions"

  (!.js (+ 1 2 3))
  => 6)

tahto/runtime/chromedriver_test.clj:30:^{:refer tahto.runtime.chromedriver/goto :added "4.0"
  :setup [(def +browser+ (chromedriver/browser {:port 19222}))]
  :teardown [(h/stop +browser+)]}
(fact "goto a given page"

  (chromedriver/goto "https://www.baidu.com/" 4000 +browser+)
  => (contains-in {"targetInfo" {"attached" true, "url" "https://www.baidu.com/"}})

  @(chromedriver/evaluate +browser+ "1+1")
  => {"value" 2, "type" "number", "description" "2"}

  (h/p:rt-raw-eval +browser+ "1+1")
  => 2)

tahto/runtime/chromedriver_test.clj:44:^{:refer tahto.runtime.chromedriver/tab-create :added "4.0"
  :setup [(def +browser+ (chromedriver/browser {:port 19223}))]
  :teardown [(h/stop +browser+)]}
(fact "creates, switches, and closes tabs"

  (chromedriver/goto "data:text/html,<title>Tab One</title>" 4000 +browser+)
  (def +one+ (chromedriver/current-tab +browser+))
  (def +two+ (chromedriver/tab-create +browser+ "data:text/html,<title>Tab Two</title>"))

  (chromedriver/tab-switch +browser+ +two+)
  @(chromedriver/evaluate +browser+ "document.title")
  => (contains {"value" "Tab Two"})

  (chromedriver/with-tab +browser+ +one+
    @(chromedriver/evaluate +browser+ "document.title"))
  => (contains {"value" "Tab One"})

  (chromedriver/tab-switch +browser+ +one+ {:bootstrap false})
  (chromedriver/tab-close +browser+ +two+)

  (chromedriver/tab-list +browser+)
  => vector?)

tahto/runtime/chromedriver_test.clj:67:^{:refer tahto.runtime.chromedriver/with-tab :added "4.0"
  :setup [(def +main+
            (do (chromedriver/goto "data:text/html,<title>Main</title>" 4000 (l/rt :js))
                (chromedriver/current-tab (l/rt :js))))
          (def +other+
            (chromedriver/tab-create (l/rt :js) "data:text/html,<title>Other</title>"))]
  :teardown [(chromedriver/tab-close (l/rt :js) +other+)
             (chromedriver/tab-switch (l/rt :js) +main+ {:bootstrap false})]}
(fact "with-tab works with !.js forms"

  (!.js document.title)
  => "Main"

  (chromedriver/with-tab (l/rt :js) +other+
    (!.js document.title))
  => "Other"

  (!.js document.title)
  => "Main")


tahto/runtime/chromedriver_test.clj:88:^{:refer tahto.runtime.chromedriver/current-tab :added "4.0"
  :setup [(def +browser+ (chromedriver/browser {:port 19224}))
          (chromedriver/goto "data:text/html,<title>Current Tab</title>" 4000 +browser+)]
  :teardown [(h/stop +browser+)]}
(fact "returns the active tab handle"

  (chromedriver/current-tab +browser+)
  => (contains {:target-id string?
                :session-id string?}))

tahto/runtime/chromedriver_test.clj:98:^{:refer tahto.runtime.chromedriver/tab-list :added "4.0"
  :setup [(def +browser+ (chromedriver/browser {:port 19225}))
          (chromedriver/goto "data:text/html,<title>Tab List</title>" 4000 +browser+)]
  :teardown [(h/stop +browser+)]}
(fact "lists all open tabs"

  (chromedriver/tab-list +browser+)
  => vector?)

tahto/runtime/chromedriver_test.clj:107:^{:refer tahto.runtime.chromedriver/tab-switch :added "4.0"
  :setup [(def +browser+ (chromedriver/browser {:port 19226}))
          (chromedriver/goto "data:text/html,<title>One</title>" 4000 +browser+)]
  :teardown [(h/stop +browser+)]}
(fact "switches the active tab"

  (def +one+ (chromedriver/current-tab +browser+))
  (def +two+ (chromedriver/tab-create +browser+ "data:text/html,<title>Two</title>"))

  (chromedriver/tab-switch +browser+ +two+)
  @(chromedriver/evaluate +browser+ "document.title")
  => (contains {"value" "Two"})

  (chromedriver/tab-switch +browser+ +one+ {:bootstrap false})
  @(chromedriver/evaluate +browser+ "document.title")
  => (contains {"value" "One"}))

tahto/runtime/chromedriver_test.clj:124:^{:refer tahto.runtime.chromedriver/tab-close :added "4.0"
  :setup [(def +browser+ (chromedriver/browser {:port 19227}))
          (chromedriver/goto "data:text/html,<title>Main</title>" 4000 +browser+)]
  :teardown [(h/stop +browser+)]}
(fact "closes the given tab"

  (def +tab+ (chromedriver/tab-create +browser+ "data:text/html,<title>Close Me</title>"))

  (chromedriver/tab-close +browser+ +tab+)
  => (contains {"success" true}))