(ns hara.runtime.chromedriver-test
  (:use code.test)
  (:require [hara.runtime.chromedriver :as chromedriver]
             [hara.lang :as l]
             [std.lib :as h]))

(l/script :js
  {:runtime :chromedriver.instance
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-resource :as rt]]})

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer hara.runtime.chromedriver-test/browser-eval :added "4.0"}
(fact "chromedriver evaluates !.js expressions"

  (!.js (+ 1 2 3))
  => 6)

^{:refer hara.runtime.chromedriver/goto :added "4.0"
  :setup [(def +browser+ (chromedriver/browser {:port 19222}))]
  :teardown [(h/stop +browser+)]}
(fact "goto a given page"

  (chromedriver/goto "https://www.baidu.com/" 4000 +browser+)
  => (contains-in {"targetInfo" {"attached" true, "url" "https://www.baidu.com/"}})

  @(chromedriver/evaluate +browser+ "1+1")
  => {"value" 2, "type" "number", "description" "2"}

  (h/p:rt-raw-eval +browser+ "1+1")
  => 2)
