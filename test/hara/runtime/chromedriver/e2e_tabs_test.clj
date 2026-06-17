(ns hara.runtime.chromedriver.e2e-tabs-test
  (:use code.test)
  (:require [hara.runtime.chromedriver :as chromedriver]
             [hara.lang :as l]
             [std.lib :as h]))

(l/script :js
  {:runtime :chromedriver.instance
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-resource :as rt]]})
