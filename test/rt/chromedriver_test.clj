(ns rt.chromedriver-test
  (:use code.test)
  (:require [rt.chromedriver :as chromedriver]
            [std.lib :as h]))

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
