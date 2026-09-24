(ns melbourne.ui-color-input-test
  (:use code.test)
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :test/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.react-native :as n]
             [js.react :as r]
             [melbourne.ui-static :as ui-static]
             [melbourne.base-palette :as base-palette]
             [melbourne.ui-color-input :as ui-color-input]
             [js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [xt.lang.common-data :as data]
             [xt.lang.common-lib :as lib]]
   :export [MODULE]})

^{:refer melbourne.ui-color-input/ColorInput :added "4.0"}
(fact "creates a static div"
  ^:hidden

  (defn.js ColorInputDemo
    []
    (var [value setValue] (r/local "#ccc"))
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-color-input/ColorInput"} 
[:% n/Row
       [:% ui-color-input/ColorInput
        #{value setValue}]])))

  (def.js MODULE (!:module)))
