(ns js.lib.ua-parser-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.string :as str]
            [xt.lang.base-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[js.lib.ua-parser :as ua]
             [js.core :as j]
             [xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:scaffold :js)]
  :teardown [(l/rt:stop)]})

^{:refer js.lib.ua-parser/parseString :added "4.0" :unchecked true}
(fact "gets information from the user agent on browser"
  ^:hidden

  (!.js
   (ua/parseString
    (@! (str/join
         "; "
         ["Mozilla/5.0 (Mobile"
          "Windows Phone 8.1"
          "Android 4.0"
          "ARM"
          "Trident/7.0"
          "Touch"
          "rv:11.0"
          "IEMobile/11.0"
          "NOKIA"
          "Lumia 635) like iPhone OS 7_0_3 Mac OS X AppleWebKit/537 (KHTML, like Gecko) Mobile Safari/537"]))))
  => map?
  #_
  {"device" {"vendor" "Nokia", "model" "Lumia 635", "type" "mobile"},
   "os" {"name" "Windows Phone", "version" "8.1"},
   "cpu" {"architecture" "arm"},
   "browser" {"major" "11", "name" "IEMobile", "version" "11.0"},
   "engine" {"name" "Trident", "version" "7.0"}})
