(ns melbourne.ui-chip-input-test
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
             [melbourne.ui-chip-input :as ui-chip-input]
             [js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [xt.lang.common-data :as data]
             [xt.lang.common-lib :as lib]]
   :export [MODULE]})

^{:refer melbourne.ui-chip-input/ChipInput :added "4.0"}
(fact "creates a static div"
  ^:hidden
  
  (defn.js ChipInputDemo
    []
    (var [values setValues] (r/local ["hello" "world"]))
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-chip-input/ChipInput"} 
[:% n/Row
       [:% ui-chip-input/ChipInput
        #{values setValues}]])))

  (def.js MODULE (!:module)))
