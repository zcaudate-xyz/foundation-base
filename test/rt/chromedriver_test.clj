(ns rt.chromedriver-test
  (:use code.test)
  (:require [rt.chromedriver :as chromedriver]
            [std.lang :as l]
            [std.lib :as h]
            [xt.lang.base-lib :as k]
            [xt.lang.base-repl :as repl]
            [js.cell.kernel :as cl]
            [js.cell.kernel.base-link-local :as base-link-local]
            [js.cell.runtime.link :as runtime-link]))

(l/script :js
  {:runtime :chromedriver.instance
   :require [[xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [xt.lang.base-runtime :as rt]
             [js.cell.kernel :as cl]
             [js.cell.kernel.base-link-local :as base-link-local]
             [js.cell.runtime.link :as runtime-link]]})

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer rt.chromedriver-test/browser-eval :added "4.0"}
(fact "chromedriver evaluates !.js expressions"
  ^:hidden

  (!.js (+ 1 2 3))
  => 6)

^{:refer rt.chromedriver/goto :added "4.0"
  :setup [(def +browser+ (chromedriver/browser {:port 19222}))]
  :teardown [(h/stop +browser+)]}
(fact "goto a given page"
  ^:hidden
  
  (chromedriver/goto "https://www.baidu.com/" 4000 +browser+)
  => (contains-in {"targetInfo" {"attached" true, "url" "https://www.baidu.com/"}})
  
  @(chromedriver/evaluate +browser+ "1+1")
  => {"value" 2, "type" "number", "description" "2"}

  (h/p:rt-raw-eval +browser+ "1+1")
  => 2)
