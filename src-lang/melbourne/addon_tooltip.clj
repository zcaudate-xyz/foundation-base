(ns melbourne.addon-tooltip
  (:use code.test)
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :playground/web-basic
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.react-native :as n]
             [melbourne.ui-static :as ui-static]]
   :export [MODULE]})

(defn.js addonTooltip
  "creates an addon tooltip"
  {:added "4.0"}
  [hostRef visible
   #{[design
      variant
      (:= tooltip {})]}]
  (var #{[text]} tooltip)
  (return
   {:component n/View
    :children
    [[:% ui-static/TextTooltip
      #{[:key "tooltip"
         hostRef
         visible
         design
         variant
         text
         (:.. tooltip)]}]]}))

(def.js MODULE (!:module))

