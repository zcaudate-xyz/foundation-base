(ns js.react-native.physical-addon
  (:require [hara.lang :as l]))

(l/script :js
  {:runtime :websocket
   :config {:id :play/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:host "test.statstrade.io"}}
   :require [[js.react-native :as n]
              [xt.lang.common-data :as xtd]
              [xt.lang.common-lib :as k]]})

(defn.js tagBase
  "base for tag single and tag all"
  {:added "4.0"}
  [#{[style
      (:.. rprops)]}]
  (return
   #{[:component n/TextInput
      :editable false
      :style [{:cursor "default"
               :width 50
               :fontSize 12}
              (n/PlatformSelect {:ios {:fontFamily "Courier"}
                                 :default {:fontFamily "monospace"}})
	      (n/PlatformSelect {:web {:userSelect "none"}})
              (:.. (xtd/arrayify style))]
      (:.. rprops)]}))

(defn.js tagSingle
  "display a single indicator"
  {:added "4.0"}
  [#{[indicator
      transformations
      (:.. rprops)]}]
  (return (xt/x:obj-assign (-/tagBase rprops)
                    {:transformations
                     (xt/x:obj-assign {indicator (fn [v]
                                            (return {:value (. v (toFixed 4))}))}
                               transformations)})))

(defn.js tagAll
  "display all indicators"
  {:added "4.0"}
  [props]
  (var #{[transformations
          keys
          (:.. rprops)]} (or props {}))
  (return (xt/x:obj-assign (-/tagBase rprops)
                    {:multiline true
                     :transformations
                     (fn [m]
                       (var display (:? keys
                                        (xtd/obj-pick m keys)
                                        m))
                       (return {:value (n/format-entry display)}))})))
